package com.marcelorcorrea.imagedownloader.core.exception;

import java.io.IOException;

public class ImageDownloaderException extends IOException {

	public ImageDownloaderException(String message) {
		super(message);
	}

	public ImageDownloaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageDownloaderException(Throwable cause) {
		super(cause);
	}

}
