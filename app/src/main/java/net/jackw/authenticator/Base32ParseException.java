package net.jackw.authenticator;

public class Base32ParseException extends RuntimeException {
	public Base32ParseException(String message) {
		super(message);
	}
	public Base32ParseException() {
		super();
	}
}
