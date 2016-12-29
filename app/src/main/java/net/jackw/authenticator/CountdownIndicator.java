package net.jackw.authenticator;

import android.content.Context;
import android.graphics.*;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;


public class CountdownIndicator extends View {
	private float phase = 0.7f;
	private Paint paint;
	private Path clipPath;
	private float centerX;
	private float centerY;
	private RectF drawArea;

	public CountdownIndicator (Context context) {
		this(context, null);
	}

	public CountdownIndicator (Context context, AttributeSet attrs) {
		super(context, attrs);

		this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(ContextCompat.getColor(context, R.color.colorAccent));

		clipPath = new Path();
		generateClip();
	}

	public void setPhase (float phase) {
		if (phase < 0 || phase > 1) {
			throw new IllegalArgumentException("phase must be between 0 and 1");
		}
		this.phase = phase;
		generateClip();
	}

	@Override
	public void onMeasure (int widthAvailable, int heightAvailable) {
		int dim = Math.min(widthAvailable, heightAvailable);
		setMeasuredDimension(dim, dim);
		generateClip();
	}

	@Override
	public void onDraw (Canvas canvas) {
		canvas.clipPath(clipPath);
		canvas.drawOval(drawArea, paint);

	}

	/**
	 * Generate the clip path for the circle
	 * Based on http://stackoverflow.com/a/22568222/2826188
	 */
	private void generateClip () {
		float angle = -(360 - phase * 360);
		clipPath.reset();
		calcCenter();

		drawArea = new RectF(0, 0, getWidth(), getHeight());

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
