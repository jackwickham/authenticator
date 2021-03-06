package net.jackw.authenticator;


public class Totp extends HotpGenerator {
	private int interval = 30;
	private String cachedCode;
	private long cachedSeed = 0;

	private static final int DEFAULT_INTERVAL = 30;

	public Totp (byte[] secret) {
		super(secret);

		this.interval = DEFAULT_INTERVAL;
	}

	public Totp (byte[] secret, HashAlgorithm hashAlgorithm, int length, int interval) {
		super(secret, hashAlgorithm, length);

		if (interval < 10 || interval > 180) {
			throw new IllegalArgumentException("Interval must be between 10 and 180 seconds");
		}

		this.interval = interval;
	}

	public Totp (String extra) throws CodeGeneratorConstructionException {
		super(extra);

		try {
			this.interval = Integer.parseInt(extra.split(",")[3]);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			throw new CodeGeneratorConstructionException("Extra string was invalid", e);
		}
	}

	@Override
	public String generateCode() {
		// Convert the time in milliseconds to the number of 30s intervals since the epoch
		long seed = TimeProvider.getInstance().now() / (interval * 1000);
		return generateHotp(seed);
	}

	@Override
	public String getExtra () {
		String result = super.getExtra();
		result += "," + Long.toString(interval);
		return result;
	}

	@Override
	public Type getType () {
		return Type.TOTP;
	}

	@Override
	public String getCodeForDisplay () {
		// Check if the code needs regenerating
		if (cachedSeed != TimeProvider.getInstance().now() / (interval * 1000)) {
			cachedCode = generateCode();
			cachedSeed = TimeProvider.getInstance().now() / (interval * 1000);
		}
		return cachedCode;
	}

	public float getTimeRemainingFraction () {
		return (TimeProvider.getInstance().now() % (interval * 1000)) / (interval * 1000.0f);
	}

	public void setInterval (int interval) {
		this.interval = interval;
	}
}
