package net.jackw.authenticator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TimeProvider {
	private static TimeProvider instance = null;

	private int timeOffset = 0;

	private TimeProvider (int offset) {
		timeOffset = offset;
	}

	public TimeProvider (Context context) {
		setOffsetFromPref(context);

		instance = this;
	}

	public static TimeProvider getInstance () {
		if (instance == null) {
			// Hasn't been initialised yet - initialise by default with an offset of 0
			instance = new TimeProvider(0);
		}
		return instance;
	}

	public static TimeProvider getInstance (Context context) {
		if (instance == null) {
			instance = new TimeProvider(context);
		} else {
			instance.setOffsetFromPref(context);
		}
		return instance;
	}


	public long now () {
		return System.currentTimeMillis() + timeOffset*1000;
	}

	public void updateOffset (int offset) {
		timeOffset = offset;
	}

	private void setOffsetFromPref (Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		timeOffset = sharedPreferences.getInt("timeOffset", 0);
	}
}
