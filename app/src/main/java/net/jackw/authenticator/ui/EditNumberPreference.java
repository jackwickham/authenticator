package net.jackw.authenticator.ui;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class EditNumberPreference extends EditTextPreference {
	public EditNumberPreference(Context context) {
		super(context);
	}

	public EditNumberPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditNumberPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		return String.valueOf(getPersistedInt(0));
	}

	@Override
	protected boolean persistString(String value) {
		return persistInt(Integer.valueOf(value));
	}
}
