package net.jackw.authenticator;

import java.nio.ByteBuffer;
import org.apache.commons.codec.digest.HmacUtils;

/**
 * Created by jack on 18/12/16.
 */

public abstract class HotpGenerator extends CodeGenerator {
    private byte[] secret;
	private HashAlgorithm hashAlgorithm;
	private int len = 6;

	public enum HashAlgorithm {
		sha1,
		sha256,
		sha384,
		sha512
	}

	/**
	 * Precomputed powers of 10 for performance
	 */
	private final int[] POWERS_OF_TEN = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

	public HotpGenerator (byte[] secret, HashAlgorithm hashAlgorithm, int length) {
		this.secret = secret;
		this.hashAlgorithm = hashAlgorithm;
		this.len = length;
	}

	/**
	 * Generate the HOTP code from the seed and secret
	 * @param seed The seed to hash from, either counter or time
	 * @return The HOTP code to be truncated for display
	 */
    protected final long generateHotpLong (long seed) {
		// Convert the seed int to a buffer
		byte[] buff = ByteBuffer.allocate(8).putLong(seed).array();

		// HMAC
		switch (hashAlgorithm) {
			case sha1:
				buff = HmacUtils.hmacSha1(secret, buff);
				break;
			case sha256:
				buff = HmacUtils.hmacSha256(secret, buff);
				break;
			case sha384:
				buff = HmacUtils.hmacSha384(secret, buff);
				break;
			case sha512:
				buff = HmacUtils.hmacSha512(secret, buff);
				break;
			default:
				throw new RuntimeException("Hash algorithm not yet implemented");
		}

		// Use the last nibble as an offset
		int offset = buff[buff.length - 1] & 0x0F;

		// Take the 4 bytes starting at that offset, and remove the first bit
		long result = 0;
		int multiplier = 1;
		for (int i = 3; i != 0; i--, multiplier *= 256) {
			result += buff[offset + i] * multiplier;
		}
		result += (buff[offset] & 0x7F) * multiplier;

		return result;
    }

	/**
	 * Generate code for the user to enter
	 * @param seed The seed to hash from, either counter or time
	 * @return The HTOP code for the user to enter
	 */
	protected final String generateHotp (long seed) {
		// Generate the code
		long code = generateHotpLong(seed);

		// Shorten it using mod 10^length
		code = code % POWERS_OF_TEN[len];

		return String.format("%0" + Integer.toString(len) + "d", code);
	}

	public abstract String generateCode ();
}
