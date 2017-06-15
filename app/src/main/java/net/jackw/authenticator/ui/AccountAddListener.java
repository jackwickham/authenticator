package net.jackw.authenticator.ui;

import android.support.annotation.Nullable;

import net.jackw.authenticator.HotpGenerator;

public interface AccountAddListener {
	/**
	 * Add a new totp account
	 *
	 * @param secret The account secret
	 * @param issuer The account issuer
	 * @param username The username, or null
	 * @param digits The number of digits the code should have, default 6
	 * @param period The time that each code should last for in seconds, default 30
	 * @param algorithm The algorithm to use, default SHA1
	 */
	void addTotpAccount (byte[] secret, String issuer, @Nullable String username, @Nullable Integer digits,
						 @Nullable Integer period, @Nullable HotpGenerator.HashAlgorithm algorithm);

	/**
	 * Add a new htop account
	 *
	 * @param secret The account secret
	 * @param issuer The account issuer
	 * @param username The username, or null
	 * @param digits The number of digits the code should have, default 6
	 * @param counter The initial counter value for this account
	 * @param algorithm The algorithm to use, default SHA1
	 */
	void addHotpAccount (byte[] secret, String issuer, @Nullable String username, @Nullable Integer digits,
						 @Nullable Long counter, @Nullable HotpGenerator.HashAlgorithm algorithm);
}
