package net.jackw.authenticator;

public class CodeGeneratorConstructionException extends Exception {
	public CodeGeneratorConstructionException() {
		super();
	}
	public CodeGeneratorConstructionException (String message) {
		super(message);
	}
	public CodeGeneratorConstructionException (String message, Throwable prev) {
		super(message, prev);
	}
}
