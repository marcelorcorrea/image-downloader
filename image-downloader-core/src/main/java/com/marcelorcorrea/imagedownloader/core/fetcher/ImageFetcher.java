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

    byte[] downloadImage(String url, ContentType selectedContentType, int width, int height) throws IOException;

    ContentType getContentType(String url);

    void setListener(ImageFetcherCallback fetcherCallback);
}
