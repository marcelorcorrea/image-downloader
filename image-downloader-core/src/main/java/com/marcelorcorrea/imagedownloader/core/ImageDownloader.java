package com.marcelorcorrea.imagedownloader.core;

import com.marcelorcorrea.imagedownloader.core.exception.ImageDownloaderException;
import com.marcelorcorrea.imagedownloader.core.exception.UnknownContentTypeException;
import com.marcelorcorrea.imagedownloader.core.fetcher.ImageFetcher;
import com.marcelorcorrea.imagedownloader.core.listener.ImageDownloaderListener;
import com.marcelorcorrea.imagedownloader.core.model.DownloadedImage;
import com.marcelorcorrea.imagedownloader.core.parser.HTMLParser;
import com.marcelorcorrea.imagedownloader.core.model.ContentType;

import java.io.File;
import java.util.List;

public interface ImageDownloader {

    void download(String path, ContentType extension, int width, int height) throws ImageDownloaderException, UnknownContentTypeException;

    boolean writeToDisk(List<DownloadedImage> images, File directory);

    boolean writeToDisk(DownloadedImage image, File directory);

    void setHTMLParser(HTMLParser parser);

    void setImageFetcher(ImageFetcher imageFetcher);

    void setOnDownloadListener(ImageDownloaderListener imageDownloaderListener);
}
