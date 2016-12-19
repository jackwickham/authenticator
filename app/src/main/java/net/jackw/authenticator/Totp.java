package net.jackw.authenticator;

/**
 * Created by jack on 19/12/16.
 */

public class Totp extends HotpGenerator {
	private int interval = 30;

	public Totp (byte[] secret, HashAlgorithm hashAlgorithm, int length) {
		super(secret, hashAlgorithm, length);
	}

	@Override
	public String generateCode() {
		// Convert the time in milliseconds to the number of 30s intervals since the epoch
		long seed = TimeProvider.getInstance().now() / (interval * 1000);
		return generateHotp(seed);
	}
}
