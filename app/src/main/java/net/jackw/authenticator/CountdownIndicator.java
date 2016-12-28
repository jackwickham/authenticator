package net.jackw.authenticator;

import android.content.Context;
import android.graphics.*;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;


public class CountdownIndicator extends View {
	private float phase;
	private Paint paintOuter;
	private Paint paintInner;
	private Path clipPath;
	private float centerX;
	private float centerY;

	public CountdownIndicator (Context context) {
		this(context, null);
	}

	public CountdownIndicator (Context context, AttributeSet attrs) {
		super(context, attrs);

		this.paintOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.paintInner = new Paint(Paint.ANTI_ALIAS_FLAG);

		paintInner.setColor(ContextCompat.getColor(context, R.color.account_background));
		paintOuter.setColor(ContextCompat.getColor(context, R.color.colorAccent));

		clipPath = new Path();
	}

	public void setPhase (float phase) {
		if (phase < 0 || phase > 1) {
			throw new IllegalArgumentException("phase must be between 0 and 1");
		}
		this.phase = phase;
	}

	@Override
	public void onMeasure (int widthAvailable, int heightAvailable) {
		int dim = Math.min(widthAvailable, heightAvailable);
		setMeasuredDimension(dim, dim);
	}

	@Override
	public void onDraw (Canvas canvas) {
		generateClip();
		canvas.clipPath(clipPath);
		canvas.drawRGB(0, 0, 0);
		RectF drawArea = new RectF(0, 0, getWidth(), getHeight());
		canvas.drawOval(drawArea, paintOuter);

	}

	/**
	 * Generate the clip path for the circle
	 * Based on http://stackoverflow.com/a/22568222/2826188
	 */
	private void generateClip () {
		float angle = 360 - phase * 360;
		clipPath.reset();
		calcCenter();

		RectF drawArea = new RectF(0, 0, getWidth(), getHeight());

		clipPath.moveTo(centerX, centerY);
		clipPath.addArc(drawArea, 270.0f, angle);
		clipPath.lineTo(centerX, centerY);

		invalidate();
	}

	private void calcCenter () {
		centerX = getWidth() / 2;
		centerY = getHeight() / 2;
	}
}
