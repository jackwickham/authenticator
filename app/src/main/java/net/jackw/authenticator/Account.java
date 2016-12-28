package net.jackw.authenticator;

public class Account {
	private CodeGenerator codeGenerator;
	private String issuer;
	private String username;
	private int id;
	private boolean fromDb = true;

	public Account (CodeGenerator generator, String username, String issuer) {
		this(generator, username, issuer, 0);
		this.fromDb = false;
	}
	public Account (CodeGenerator generator, String username, String issuer, int id) {
		this.codeGenerator = generator;
		this.username = username;
		this.issuer = issuer;
		this.id = id;
	}

	public CodeGenerator getCodeGenerator () {
		return codeGenerator;
	}
	public String getIssuer () {
		return issuer;
	}
	public String getUsername () {
		return username;
	}
}
