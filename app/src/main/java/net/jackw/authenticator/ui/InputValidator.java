package net.jackw.authenticator.ui;

import android.support.annotation.Nullable;

public interface InputValidator {
	/**
	 * Validate the input
	 *
	 * @param value The input to validate
	 * @return Error message, or null if no error
	 */
	@Nullable
	String validate (String value);
}
