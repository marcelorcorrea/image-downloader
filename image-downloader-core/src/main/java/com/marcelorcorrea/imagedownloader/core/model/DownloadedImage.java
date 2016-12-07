package com.marcelorcorrea.imagedownloader.core.model;

import java.io.File;

/**
 * @author Marcelo
 */
public class DownloadedImage {

    private final byte[] bytes;
    private final String name;
    private final String extension;
    private File file;

    public DownloadedImage(byte[] bytes, String name) {
        this.bytes = bytes;
        this.name = name;
        extension = (name.substring(name.indexOf(".") + 1)).toLowerCase();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return name;
    }
}
