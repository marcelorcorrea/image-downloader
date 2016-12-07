package com.marcelorcorrea.imagedownloader.core.listener;

import com.marcelorcorrea.imagedownloader.core.model.DownloadedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by marcelo on 11/1/16.
 */
public class LogImageDownloaderListener implements ImageDownloaderListener {

    private static final Logger logger = LoggerFactory.getLogger(LogImageDownloaderListener.class);

    @Override
    public void onTaskStart(int numberOfImages) {
        logger.info("Task has started with {} images to download.", numberOfImages);
    }

    @Override
    public void onDownloadStart(String url, int contentLength) {
        logger.info("Download has started, URL: {}, contentLength: {}", url, contentLength);
    }

    @Override
    public void onDownloadInProgress(String url, int progress) {

    }

    @Override
    public void onDownloadComplete(String url, DownloadedImage downloadedImage) {
        logger.info("Download {} COMPLETE! Filename: {}", url, downloadedImage.getName());
    }

    @Override
    public void onDownloadFail(String imgSource) {
        logger.info("Something went wrong with download {} ", imgSource);
    }

    @Override
    public void onTaskFinished() {
        logger.info("Task has been completed!");
    }
}
