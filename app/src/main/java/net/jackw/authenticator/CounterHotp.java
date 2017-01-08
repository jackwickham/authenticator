package net.jackw.authenticator;

public class CounterHotp extends HotpGenerator {
	private long counter = 0;
	private String cachedCode = "------";

	private static final int DEFAULT_COUNTER = 0;

	public CounterHotp (byte[] secret) {
		super(secret);

		this.counter = DEFAULT_COUNTER;
	}

	public CounterHotp (byte[] secret, HashAlgorithm hashAlgorithm, int length, long counter) {
		super(secret, hashAlgorithm, length);

		if (counter < 0) {
			throw new IllegalArgumentException("Counter must be positive");
		}

		this.counter = counter;
	}
	public CounterHotp (String extra) throws CodeGeneratorConstructionException {
		super(extra);

		try {
			this.counter = Long.parseLong(extra.split(",")[3]);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			throw new CodeGeneratorConstructionException("Extra string was invalid", e);
		}
	}

	public void setCounter (long v) {
		if (v < 0) {
			throw new IllegalArgumentException("The new counter value must be positive");
		}
		counter = v;
	}

	@Override
	public String generateCode() {
		return generateHotp(counter);
	}

	@Override
	public String getExtra () {
		String result = super.getExtra();
		result += "," + Long.toString(counter);
		return result;
	}

	@Override
	public Type getType () {
		return Type.HOTP;
	}

	@Override
	public String getCodeForDisplay () {
		// Will display ------ if uninitialised, or the last generated code if available
		return cachedCode;
	}

	public String updateCode () {
		cachedCode = generateCode();
		return cachedCode;
	}
}
