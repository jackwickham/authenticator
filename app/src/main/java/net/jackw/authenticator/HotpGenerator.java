package net.jackw.authenticator;

import java.nio.ByteBuffer;
import java.util.Locale;

import org.apache.commons.codec.digest.HmacUtils;


public abstract class HotpGenerator extends CodeGenerator {
    private byte[] secret;
	private HashAlgorithm hashAlgorithm;
	private int len = 6;

	public enum HashAlgorithm {
		SHA1(0),
		SHA256(1),
		SHA384(2),
		SHA512(3);

		public final int value;
		private HashAlgorithm (int value) {
			this.value = value;
		}

		public static HashAlgorithm get (int i) {
			for (HashAlgorithm algo : HashAlgorithm.values()) {
				if (algo.value == i) {
					return algo;
				}
			}
			return null;
		}
	}

	private static final HashAlgorithm DEFAULT_ALGORITHM = HashAlgorithm.SHA1;
	private static final int DEFAULT_LENGTH = 6;

	/**
	 * Precomputed powers of 10 for performance
	 */
	private final int[] POWERS_OF_TEN = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

	/**
	 * Construct using default options
	 */
	public HotpGenerator (byte[] secret) {
		this (secret, DEFAULT_ALGORITHM, DEFAULT_LENGTH);
	}

	public HotpGenerator (byte[] secret, HashAlgorithm hashAlgorithm, int length) {
		this.secret = secret;
		this.hashAlgorithm = hashAlgorithm;
		this.len = length;

		if (length < 3 || length > 10) {
			throw new IllegalArgumentException("length must be in the range 3 - 10");
		}
	}
	public HotpGenerator (String extra) throws CodeGeneratorConstructionException {
		try {
			String[] parts = extra.split(",");
			this.secret = Utils.base32Decode(parts[0]);
			this.hashAlgorithm = HashAlgorithm.get(Integer.parseInt(parts[1]));
			this.len = Integer.parseInt(parts[2]);
		} catch (Base32ParseException e) {
			throw new CodeGeneratorConstructionException(e.getMessage(), e);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			throw new CodeGeneratorConstructionException("Extra string was invalid", e);
		}
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
			case SHA1:
				buff = HmacUtils.hmacSha1(secret, buff);
				break;
			case SHA256:
				buff = HmacUtils.hmacSha256(secret, buff);
				break;
			case SHA384:
				buff = HmacUtils.hmacSha384(secret, buff);
				break;
			case SHA512:
				buff = HmacUtils.hmacSha512(secret, buff);
				break;
			default:
				throw new RuntimeException("Hash algorithm not yet implemented");
		}

		// Use the last nibble as an offset
		int offset = buff[buff.length - 1] & 0x0F;

		// Take the 4 bytes starting at that offset, and remove the first bit
		// Working around Java's annoying lack of unsigned types
		long result = 0;
		int multiplier = 1;
		for (int i = 3; i != 0; i--, multiplier *= 256) {
			result += (buff[offset + i] & 0xFF) * multiplier;
		}
		result += (buff[offset] & 0x7F) * multiplier;

		return result;
    }

	/**
	 * Generate code for the user to enter
	 * @param seed The seed to hash from, either counter or time
	 * @return The HOTP code for the user to enter
	 */
	protected final String generateHotp (long seed) {
		// Generate the code
		long code = generateHotpLong(seed);

		// Shorten it using mod 10^length
		code = code % POWERS_OF_TEN[len];

		return String.format("%0" + Integer.toString(len) + "d", code);
	}

	public abstract String generateCode ();

	/**
	 * Get the extra data for the DB
	 * Child classes can override to add more things to the end
	 *
	 * @return The data to store in the DB
	 */
	@Override
	public String getExtra () {
		return String.format(Locale.UK, "%s,%d,%d", Utils.base32Encode(this.secret, false), hashAlgorithm.value, len);
	}

	public void setLength (int length) {
		this.len = length;
	}

	public void setAlgorithm (HashAlgorithm algorithm) {
		this.hashAlgorithm = algorithm;
	}
}
