package com.example.app.exceptions;

public class DecryptionFailedException extends Exception {
	public DecryptionFailedException(String message) {
		super(message);
	}

	public DecryptionFailedException(Throwable cause) {
		super(cause);
	}
}
