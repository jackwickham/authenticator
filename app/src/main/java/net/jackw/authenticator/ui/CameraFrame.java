package net.jackw.authenticator.ui;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import net.jackw.authenticator.ErrorCallback;

import java.io.IOException;

/**
 * Based on https://developer.android.com/guide/topics/media/camera.html
 */

@SuppressWarnings("deprecation")
public class CameraFrame extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder holder;
	private ErrorCallback<Exception> errorHandler = null;
	private Activity context;

	// Using the Camera class over camera2.* because camera2 was only added in API21, and at time of
	// writing this app has a min supported version of 16
	private Camera camera;

	private static final String LOG_TAG = "CameraFrame";

	public CameraFrame(Activity context, @NonNull Camera camera) {
		super(context);
		this.camera = camera;
		this.context = context;

		// Set up event listeners for the surface being created/destroyed
		holder = getHolder();
		holder.setFixedSize(getWidth(), getHeight());
		holder.addCallback(this);
	}

	public CameraFrame(Activity context, @NonNull Camera camera, ErrorCallback<Exception> errorCallback) {
		this(context, camera);

		this.errorHandler = errorCallback;
	}

	/**
	 * This is called immediately after the surface is first created.
	 * Implementations of this should start up whatever rendering code
	 * they desire.  Note that only one thread can ever draw into
	 * a {@link Surface}, so you should not draw into the Surface here
	 * if your normal rendering will be in another thread.
	 *
	 * @param holder The SurfaceHolder whose surface is being created.
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// Connect the camera to the surface
		try {
			setCameraOrientation();
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			Log.w(LOG_TAG, "Error setting camera preview: " + e.getMessage(), e);

			if (errorHandler != null) {
				// Let the parent give up if they want
				errorHandler.onError(e);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Critical error setting camera preview: " + e.getMessage(), e);

			if (errorHandler != null) {
				// Let the parent give up if they want
				errorHandler.onError(e);
			}
		}
	}

	/**
	 * This is called immediately after any structural changes (format or
	 * size) have been made to the surface.  You should at this point update
	 * the imagery in the surface.  This method is always called at least
	 * once, after {@link #surfaceCreated}.
	 *
	 * @param holder The SurfaceHolder whose surface has changed.
	 * @param format The new PixelFormat of the surface.
	 * @param width  The new width of the surface.
	 * @param height The new height of the surface.
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Stop the preview, then start it again with the new holder
		if (holder.getSurface() == null) {
			return;
		}

		try {
			camera.stopPreview();
		} catch (Exception e) {
			// Failing to stop the preview isn't a problem, probably means we weren't previewing in
			// the first place
		}
		setCameraOrientation();
		camera.startPreview();
	}

	/**
	 * This is called immediately before a surface is being destroyed. After
	 * returning from this call, you should no longer try to access this
	 * surface.  If you have a rendering thread that directly accesses
	 * the surface, you must ensure that thread is no longer touching the
	 * Surface before returning from this function.
	 *
	 * @param holder The SurfaceHolder whose surface is being destroyed.
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Camera should have been released by the parent that destroyed the surface
	}

	private void setCameraOrientation() {
		int orientation = 0;
		switch (context.getWindowManager().getDefaultDisplay().getRotation()) {
			case Surface.ROTATION_0:
				orientation = 90;
				break;
			case Surface.ROTATION_90:
				orientation = 0;
				break;
			case Surface.ROTATION_180:
				orientation = 90;
				break;
			case Surface.ROTATION_270:
				orientation = 180;
				break;
		}
		Log.d(LOG_TAG, Integer.toString(context.getWindowManager().getDefaultDisplay().getRotation()));
		camera.setDisplayOrientation(orientation);
	}
}
