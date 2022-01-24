package com.example.app.exceptions;

public class PasswordCheckFailedException extends Exception {

	public PasswordCheckFailedException(String message) {
		super(message);
	}

	public PasswordCheckFailedException(Throwable cause) {
		super(cause);
	}

	public PasswordCheckFailedException() {}
}
