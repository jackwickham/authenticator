package net.jackw.authenticator.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.jackw.authenticator.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountAddListener} interface
 * to handle interaction events.
 * Use the {@link AccountAddQr#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountAddQr extends AccountAddFragment {
	private static final int PERM_REQ_CAMERA = 1;

	private AccountAddListener mListener;
	private Camera cam;
	FrameLayout frame;

	public AccountAddQr() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment AccountAddQr.
	 */
	public static AccountAddQr newInstance() {
		AccountAddQr fragment = new AccountAddQr();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_account_add_qr, container, false);

		frame = (FrameLayout) view.findViewById(R.id.camera_preview);

		// Make the container fill the whole page
		container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

		if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			// Request permission
			requestPermissions(new String[]{Manifest.permission.CAMERA}, PERM_REQ_CAMERA);
		} else {
			attachCamera();
		}

		return view;
	}

	private Camera getCameraInstance() {
		if (cam != null) return cam;
		try {
			return Camera.open();
		} catch (RuntimeException e) {
			// Camera not available
		}

		// unsupported - todo
		return null;
	}

	private void attachCamera() {
		if (cam == null) {
			cam = getCameraInstance();
		}
		if (cam != null) {
			CameraFrame preview = new CameraFrame(getActivity(), cam);
			frame.addView(preview);
		}
	}


	@Override
	protected void attachContext(Context context) {
		if (context instanceof AccountAddListener) {
			mListener = (AccountAddListener) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement AccountAddListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;

		if (cam != null) {
			cam.release();
			cam = null;
		}
	}

	/**
	 * Called when the fragment is visible to the user and actively running.
	 * This is generally
	 * tied to {@link Activity#onResume() Activity.onResume} of the containing
	 * Activity's lifecycle.
	 */
	@Override
	public void onResume() {
		super.onResume();
		//cam = getCameraInstance();
	}

	/**
	 * Called when the Fragment is no longer resumed.  This is generally
	 * tied to {@link Activity#onPause() Activity.onPause} of the containing
	 * Activity's lifecycle.
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (cam != null) {
			cam.release();
			cam = null;
		}
	}

	/**
	 * Called when the Fragment is no longer started.  This is generally
	 * tied to {@link Activity#onStop() Activity.onStop} of the containing
	 * Activity's lifecycle.
	 */
	@Override
	public void onStop() {
		super.onStop();
		if (cam != null) {
			cam.release();
			cam = null;
		}
	}

	@Override
	public AddActivity.AddMethod getType() {
		return AddActivity.AddMethod.QR;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		// \o/
		if (requestCode == PERM_REQ_CAMERA) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				attachCamera();
			}
		}
	}
}
