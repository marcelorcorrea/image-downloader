package com.marcelorcorrea.imagedownloader.core.http;

import com.marcelorcorrea.imagedownloader.core.fetcher.ImageFetcher;
import com.marcelorcorrea.imagedownloader.core.model.ContentType;
import com.marcelorcorrea.imagedownloader.core.util.SimpleImageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by marcelo on 10/3/16.
 */
public class DownloadManager implements ImageFetcher {

    private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
    private static final int BUF_SIZE = 0x1000; // 4K
    private ImageFetcherCallback listener;

    public byte[] downloadImage(final String link, ContentType selectedContentType, int width, int height) throws IOException {
        HttpURLConnection request = (HttpURLConnection) new URL(link).openConnection();
        request.setRequestMethod("GET");
        request.setRequestProperty("User-Agent", USER_AGENT);
        request.connect();
        if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
            logger.info("Downloading: " + link);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(request.getInputStream());
            bufferedInputStream.mark(1000);
            SimpleImageInfo simpleImageInfo = new SimpleImageInfo(bufferedInputStream);
            bufferedInputStream.reset();

            ContentType imageContentType = ContentType.getContentType(simpleImageInfo.getMimeType());
            if (isContentTypeSupported(imageContentType, selectedContentType)) {
                if (simpleImageInfo.getWidth() > width && simpleImageInfo.getHeight() > height) {
                    int contentLength = request.getContentLength();
                    listener.notifyDownloadStart(link, contentLength);
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[BUF_SIZE];
                        long total = 0;
                        while (true) {
                            int r = bufferedInputStream.read(buf);
                            if (r == -1) {
                                break;
                            }
                            out.write(buf, 0, r);
                            total += r;
                            listener.notifyDownloadProgress(link, (int) total);
                        }
                        return out.toByteArray();
                    } finally {
                        request.disconnect();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void setListener(ImageFetcherCallback fetcherCallback) {
        listener = fetcherCallback;
    }

    private boolean isContentTypeSupported(ContentType imageContentType, ContentType selectedContentType) {
        return imageContentType != null &&
                (imageContentType.equals(selectedContentType) ||
                        (imageContentType.isJPG() && selectedContentType.isJPG()));
    }

    public ContentType getContentType(String url) {
        ContentType contentType = null;
        try {
            HttpURLConnection connection = openConnection(url, "HEAD");
            contentType = ContentType.getContentType(connection.getContentType());
            if (contentType == null) {
                logger.warn("Could not retrieve content type using HEAD method. Attempting to retrieve it with GET method");
                connection = openConnection(url, "GET");
                contentType = ContentType.getContentType(connection.getContentType());
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentType;
    }

    private HttpURLConnection openConnection(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        return connection;
    }
}
