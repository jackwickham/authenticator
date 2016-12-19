package net.jackw.authenticator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jack on 19/12/16.
 */

public class TimeProvider {
	private static TimeProvider instance = null;

	private int timeOffset = 0;

	private TimeProvider (int offset) {
		timeOffset = offset;
	}

	public TimeProvider (Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		timeOffset = sharedPreferences.getInt("timeOffset", 0);

		instance = this;
	}

	public static TimeProvider getInstance () {
		if (instance == null) {
			// Hasn't been initialised yet - initialise by default with an offset of 0
			instance = new TimeProvider(0);
		}
		return instance;
	}


	public long now () {
		return System.currentTimeMillis() + timeOffset;
	}

	public void updateOffset (int offset, Context context) {
		timeOffset = offset;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt("timeOffset", offset);
		editor.apply();
	}
}
