package net.jackw.authenticator.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.jackw.authenticator.Account;
import net.jackw.authenticator.QRDecoder;
import net.jackw.authenticator.R;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountAddListener} interface
 * to handle interaction events.
 * Use the {@link AccountAddQr#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountAddQr extends AccountAddFragment implements Camera.PreviewCallback {
	private static final int PERM_REQ_CAMERA = 1;

	private static final int MSG_FRAME = 1;
	private static final int MSG_STOP = 2;
	private static final int MSG_SUCCESS = 3;
	private static final int MSG_FAIL = 4;

	private static final String LOG_TAG = "QrFragment";

	private AccountAddListener mListener;
	private Camera cam;
	private FrameLayout frame;

	private CameraWorkerThread workerThread;
	private Handler mainHandler;

	public AccountAddQr() {
		setUpWorker();
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

	@SuppressWarnings("deprecation")
	private Camera getCameraInstance() {
		if (cam != null) return cam;
		try {
			Camera camera = Camera.open();

			Camera.Parameters params = camera.getParameters();
			params.setPreviewFormat(ImageFormat.YV12);

			// Set the preview size to the best available
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			Camera.Size bestSize = params.getPreviewSize();
			for (Camera.Size size : sizes) {
				if (size.width < bestSize.width) {
					bestSize = size;
				}
			}
			//params.setPreviewSize(bestSize.width, bestSize.height);
			params.setPreviewSize(1280, 720); // todo

			camera.setParameters(params);

			return camera;
		} catch (RuntimeException e) {
			// Camera not available
			Log.w(LOG_TAG, e);
		}

		// unsupported - todo
		return null;
	}

	private void attachCamera() {
		if (cam == null) {
			cam = getCameraInstance();
			requestNewFrame();
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

		if (workerThread.isAlive()) {
			Message.obtain(workerThread.getHandler(), MSG_STOP).sendToTarget();
		}
	}

	/**
	 * Called when the fragment is visible to the user and actively running.
	 * This is generally tied to Activity.onResume of the containing
	 * Activity's lifecycle.
	 */
	@Override
	public void onResume() {
		super.onResume();
		//cam = getCameraInstance();
	}

	/**
	 * Called when the Fragment is no longer resumed.  This is generally
	 * tied to Activity.onPause of the containing Activity's lifecycle.
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
	 * tied to Activity.onStop of the containing Activity's lifecycle.
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

	private void setUpWorker() {
		workerThread = new CameraWorkerThread();
		workerThread.start();

		mainHandler = new Handler(Looper.getMainLooper()) {
			/**
			 * Subclasses must implement this to receive messages.
			 *
			 * @param msg
			 */
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_SUCCESS:
						// \o/
						mListener.addAccount((Account) msg.obj);
						break;
					case MSG_FAIL:
						// Frame not decoded, find another one
						requestNewFrame();
						break;
				}
			}
		};
	}

	private void requestNewFrame() {
		if (cam != null) {
			cam.setOneShotPreviewCallback(this);
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Camera.Size previewSize = camera.getParameters().getPreviewSize();
		workerThread.getHandler().removeMessages(MSG_FRAME); // We only want one at a time
		Message msg = Message.obtain(workerThread.getHandler(), MSG_FRAME, previewSize.width, previewSize.height, data);
		msg.sendToTarget();
	}


	private class CameraWorkerThread extends Thread {
		private Handler handler;
		private QRDecoder decoder;

		public CameraWorkerThread() {
			super();

			decoder = new QRDecoder();
		}

		@Override
		public void run() {
			Looper.prepare();

			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case MSG_FRAME:
							Log.d(LOG_TAG, "Received new frame");
							Account acc = decoder.detectAccount((byte[]) msg.obj, msg.arg1, msg.arg2);
							Message reply = Message.obtain(AccountAddQr.this.mainHandler);
							if (acc == null) {
								reply.what = MSG_FAIL;
							} else {
								reply.what = MSG_SUCCESS;
								reply.obj = acc;

								close();
							}
							reply.sendToTarget();
							Log.d(LOG_TAG, "Processed frame");
							break;
						case MSG_STOP:
							close();
							return;
					}
				}
			};

			Looper.loop();
		}

		public Handler getHandler() {
			return handler;
		}

		private void close() {
			Looper.myLooper().quit();
		}
	}
}
