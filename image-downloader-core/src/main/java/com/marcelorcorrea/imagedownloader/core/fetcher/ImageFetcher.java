package com.marcelorcorrea.imagedownloader.core.fetcher;

import com.marcelorcorrea.imagedownloader.core.model.ContentType;

import java.io.IOException;

/**
 * Created by marcelo on 10/31/16.
 */
public interface ImageFetcher {

    interface ImageFetcherCallback {

        void notifyDownloadStart(String link, int contentLength);

        void notifyDownloadProgress(String link, int total);
    }

    ContentType getContentType(String url);

    boolean isContentTypeSupported(String url, ContentType contentType);

    byte[] downloadImage(String url) throws IOException;

    void setListener(ImageFetcherCallback fetcherCallback);
}
