package com.marcelorcorrea.imagedownloader.core.model;

/**
 * @author Marcelo
 */
public enum ContentType {

    JPG("image/jpg", "jpg"), GIF("image/gif", "gif"), PNG("image/png", "png"), JPEG("image/jpeg", "jpg"), PJPEG("image/pjpeg", "jpg"), HTML("text/html", "html");

    private final String value;
    private final String rawValue;

    ContentType(String contentType, String rawValue) {
        value = contentType;
        this.rawValue = rawValue;
    }

    public String getValue() {
        return value;
    }

    public String getRawValue() {
        return rawValue;
    }

    public boolean isHTML() {
        return equals(HTML);
    }

    public boolean isJPG() {
        return equals(JPG) || equals(JPEG) || equals(PJPEG);
    }

    public static ContentType getContentType(String targetContentType) {
        if (targetContentType != null && !targetContentType.isEmpty()) {
            for (ContentType contentType : values()) {
                if (targetContentType.contains(contentType.getValue())) {
                    return contentType;
                }
            }
        }
        return null;
    }
}