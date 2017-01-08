package net.jackw.authenticator;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;


public abstract class BaseFragment extends Fragment {
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		attachContext(context);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			attachContext(activity);
		}
	}

	/**
	 * Do whatever is needed to attach the context to this fragment
	 * Called once per attach event, regardless of SDK version
	 *
	 * @link http://stackoverflow.com/questions/32077086/android-onattachcontext-not-called-for-api-23
	 */
	protected void attachContext (Context context) { }
}
