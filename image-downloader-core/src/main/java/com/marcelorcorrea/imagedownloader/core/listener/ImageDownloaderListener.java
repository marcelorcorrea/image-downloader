package com.marcelorcorrea.imagedownloader.core.listener;

import com.marcelorcorrea.imagedownloader.core.model.DownloadedImage;

/**
 * @author Marcelo Correa
 */
public interface ImageDownloaderListener {

    void onTaskStart(int numberOfImages);

    void onDownloadStart(String url, int contentLength);

    void onDownloadInProgress(String url, int progress);

    void onDownloadComplete(String url, DownloadedImage downloadedImage);

    void onDownloadFail(String imgSource);

    void onTaskFinished();
}
