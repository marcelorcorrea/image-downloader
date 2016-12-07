package com.marcelorcorrea.imagedownloader.core.parser;

import com.marcelorcorrea.imagedownloader.core.exception.ImageDownloaderException;
import com.marcelorcorrea.imagedownloader.core.model.ContentType;

import java.util.Set;

public interface HTMLParser {

    /**
     * The HTML Parser implementation should retrieves all image links from a given URL using an  of HTML Parser.
     * @param url The URL to retrieve images.
     * @param contentType Content Type of images to be retrieved.
     * @return List of Elements containing the images retrieved from the URL.
     * @throws ImageDownloaderException
     */
    Set<String> parse(String url, ContentType contentType) throws ImageDownloaderException;
}
