package com.example.app.exceptions;

public class PasswordSetFailedException extends Exception {
	public PasswordSetFailedException(String message) {
		super(message);
	}

	public PasswordSetFailedException(Throwable cause) {
		super(cause);
	}
}
