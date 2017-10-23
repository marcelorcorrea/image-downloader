package com.marcelorcorrea.imagedownloader.core.imp;

import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.common.util.concurrent.*;
import com.marcelorcorrea.imagedownloader.core.ImageDownloader;
import com.marcelorcorrea.imagedownloader.core.exception.ImageDownloaderException;
import com.marcelorcorrea.imagedownloader.core.exception.UnknownContentTypeException;
import com.marcelorcorrea.imagedownloader.core.fetcher.ImageFetcher;
import com.marcelorcorrea.imagedownloader.core.http.DownloadManager;
import com.marcelorcorrea.imagedownloader.core.listener.ImageDownloaderListener;
import com.marcelorcorrea.imagedownloader.core.listener.LogImageDownloaderListener;
import com.marcelorcorrea.imagedownloader.core.model.ContentType;
import com.marcelorcorrea.imagedownloader.core.model.DownloadedImage;
import com.marcelorcorrea.imagedownloader.core.parser.HTMLParser;
import com.marcelorcorrea.imagedownloader.core.parser.ParserFactory;
import com.marcelorcorrea.imagedownloader.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * @author Marcelo Correa
 */
public class GenericImageDownloader implements ImageDownloader, ImageFetcher.ImageFetcherCallback {

    private static final Logger logger = LoggerFactory.getLogger(GenericImageDownloader.class);

    private static final String CONTENT_TYPE_ERROR_MESSAGE = "Could not retrieve content type of %s";

    private final ListeningExecutorService pool;
    private HTMLParser mParser;
    private ImageDownloaderListener mListener;
    private ImageFetcher mImageFetcher;


    public GenericImageDownloader() {
        pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    }

    private void initialize(String url) {
        if (mListener == null) {
            mListener = new LogImageDownloaderListener();
        }
        if (mImageFetcher == null) {
            mImageFetcher = new DownloadManager();
            mImageFetcher.setListener(this);
        }
        //Look for best mParser (currently there are two parsers)
        mParser = ParserFactory.getParser(url);
        logger.info("Using " + mParser.getClass().getSimpleName());
    }

    @Override
    public void download(String url, ContentType selectedContentType, int width, int height) throws ImageDownloaderException, UnknownContentTypeException {
        try {
            if (url == null || url.isEmpty()) {
                logger.error("URL is a required field.");
                return;
            }
            String scheme = new URI(url).getScheme();
            if (scheme == null) {
                url = "http://" + url;
            } else if (!scheme.equals("http") && !scheme.equals("https")) {
                logger.error("Only protocols HTTP and HTTPS are supported.");
                return;
            }

            //if everything looks okay, then we can initialize or verify parsers and listeners
            initialize(url);

            logger.info("Connecting to: " + url);
            ContentType urlContentType = mImageFetcher.getContentType(url);
            if (urlContentType == null) {
                logger.error(String.format(CONTENT_TYPE_ERROR_MESSAGE, url));
                throw new UnknownContentTypeException(String.format(CONTENT_TYPE_ERROR_MESSAGE, url));
            }
            boolean isContentTypeHTML = urlContentType.isHTML();
            logger.info("URL Content Type: " + urlContentType + " (" + urlContentType.getValue() + ")");
            logger.info("Selected Extension: " + selectedContentType + " (" + selectedContentType.getValue() + ")");
            logger.info("URL Content Type is a HTML Content Type? " + isContentTypeHTML);


            if (isContentTypeHTML) {
                downloadImagesFromURL(url, selectedContentType, width, height);
            } else {
                downloadSingleImageFromURL(url, selectedContentType, width, height);
            }
        } catch (UnknownContentTypeException e) {
            throw e;
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
            throw new ImageDownloaderException(e);
        }
    }

    private void downloadImagesFromURL(String url, ContentType selectedContentType, int width, int height) {
        try {
            // Get image sources using mParser
            Set<String> imgSources = mParser.parse(url, selectedContentType);
            // Fire OnTaskStart Callback with the number of links to download.
            mListener.onTaskStart(imgSources.size());

            List<ListenableFuture<DownloadedImage>> listenableFutures = new ArrayList<>();
            for (String source : imgSources) {
                ListenableFuture<DownloadedImage> listenableFuture = createAndSubmitWork(source, selectedContentType, width, height);
                listenableFutures.add(listenableFuture);
            }
            ListenableFuture<List<DownloadedImage>> listListenableFuture = Futures.successfulAsList(listenableFutures);
            Futures.addCallback(listListenableFuture, new FutureCallback<List<DownloadedImage>>() {
                @Override
                public void onSuccess(List<DownloadedImage> result) {
                    logger.info("Finished processing {} elements", Iterables.size(result));
                    mListener.onTaskFinished();
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.info("Failed because of :: {}", t);
                }
            });
        } catch (Exception ex) {
            logger.error("Error while downloading image: " + ex.getClass());
        }
    }

    private void downloadSingleImageFromURL(String imgSource, ContentType selectedContentType, int width, int height) throws IOException {
        mListener.onTaskStart(1);
        ListenableFuture<DownloadedImage> listenableFuture = createAndSubmitWork(imgSource, selectedContentType, width, height);
        Futures.addCallback(listenableFuture, new FutureCallback<DownloadedImage>() {
            @Override
            public void onSuccess(DownloadedImage result) {
                logger.info("Finished processing single image {}", result);
                mListener.onTaskFinished();
            }

            @Override
            public void onFailure(Throwable t) {
                logger.info("Failed because of :: {}", t);
            }
        });
    }

    private ListenableFuture<DownloadedImage> createAndSubmitWork(final String imgSource, ContentType selectedContentType, int width, int height) {
        Callable<DownloadedImage> callable = createDownloadedImageCallable(imgSource, selectedContentType, width, height);
        ListenableFuture<DownloadedImage> future = pool.submit(callable);
        Futures.addCallback(future, new FutureCallback<DownloadedImage>() {
            @Override
            public void onSuccess(DownloadedImage downloadedImage) {
                if (downloadedImage != null) {
                    mListener.onDownloadComplete(imgSource, downloadedImage);
                } else {
                    logger.error("Error while downloading image: {}", imgSource);
                    mListener.onDownloadFail(imgSource);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                mListener.onDownloadFail(imgSource);
                logger.error(throwable.toString());
            }
        });
        return future;
    }

    private Callable<DownloadedImage> createDownloadedImageCallable(final String imgSource, final ContentType selectedContentType,
                                                                    final int width, final int height) {
        return new Callable<DownloadedImage>() {
            @Override
            public DownloadedImage call() throws Exception {
                byte[] imageInBytes = mImageFetcher.downloadImage(imgSource, selectedContentType, width, height);
                if (imageInBytes != null) {
                    String name = Util.getFilename(imgSource, selectedContentType.getRawValue());
                    return new DownloadedImage(imageInBytes, name);
                }
                return null;
            }
        };
    }

    @Override
    public boolean writeToDisk(List<DownloadedImage> images, File directory) {
        logger.debug("Downloaded Images size: " + images.size());
        for (DownloadedImage downloadedImage : images) {
            writeToDisk(downloadedImage, directory);
        }
        return true;
    }

    @Override
    public boolean writeToDisk(DownloadedImage image, File directory) {
        try {
            File file = new File(directory, image.getName());
            Files.write(image.getBytes(), file);
            image.setFile(file);
        } catch (IOException ex) {
            logger.error("Exception: " + ex + " -- message: " + ex.getMessage());
            return false;
        }
        logger.debug("File saved successfully!");
        return true;
    }

    @Override
    public void setHTMLParser(HTMLParser parser) {
        this.mParser = parser;
    }

    @Override
    public void setImageFetcher(ImageFetcher imageFetcher) {
        this.mImageFetcher = imageFetcher;
    }

    @Override
    public void setOnDownloadListener(ImageDownloaderListener imageDownloaderListener) {
        this.mListener = imageDownloaderListener;
    }

    @Override
    public void notifyDownloadStart(String link, int contentLength) {
        mListener.onDownloadStart(link, contentLength);
    }

    @Override
    public void notifyDownloadProgress(String link, int total) {
        mListener.onDownloadInProgress(link, total);
    }
}
