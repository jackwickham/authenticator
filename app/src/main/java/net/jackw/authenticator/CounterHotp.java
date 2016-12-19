package net.jackw.authenticator;

/**
 * Created by jack on 19/12/16.
 */

public class CounterHotp extends HotpGenerator {
	private long counter = 0;

	public CounterHotp (byte[] secret, HashAlgorithm hashAlgorithm, int length, long counter) {
		super(secret, hashAlgorithm, length);

		this.counter = counter;
	}

	@Override
	public String generateCode() {
		return generateHotp(counter);
	}
}
