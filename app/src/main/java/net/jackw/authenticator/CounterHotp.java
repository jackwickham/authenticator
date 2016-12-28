package net.jackw.authenticator;

public class CounterHotp extends HotpGenerator {
	private long counter = 0;
	private String cachedCode = "------";

	public CounterHotp (byte[] secret, HashAlgorithm hashAlgorithm, int length, long counter) {
		super(secret, hashAlgorithm, length);

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
}
