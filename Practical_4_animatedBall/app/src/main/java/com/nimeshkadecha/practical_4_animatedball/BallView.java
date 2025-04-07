package com.nimeshkadecha.practical_4_animatedball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;

public class BallView extends View {
	private Paint paint;
	private float centerX, centerY, radius;
	private float angle = 0;
	private float ballRadius = 20f;
	private int ballColor = Color.RED;
	private long startTime;
	private Handler handler;
	private final int FRAME_RATE = 16;

	public BallView(Context context) {
		super(context);
		paint = new Paint();
		paint.setAntiAlias(true);
		handler = new Handler();
		startTime = System.currentTimeMillis();
		animateBall();
	}
	private void animateBall() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				long elapsed = System.currentTimeMillis() - startTime;
				if (elapsed > 2 * 60 * 1000) {
					ballRadius = 60f;
					ballColor = Color.GREEN;
				} else if (elapsed > 1 * 60 * 1000) {
					ballRadius = 40f;
					ballColor = Color.BLUE;
				}
				angle += 2;
				invalidate();
				handler.postDelayed(this, FRAME_RATE);
			}
		}, FRAME_RATE);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (centerX == 0 || centerY == 0) {
			centerX = getWidth() / 2f;
			centerY = getHeight() / 2f;
			radius = Math.min(centerX, centerY) - 100;
		}
		float radian = (float) Math.toRadians(angle);
		float x = (float) (centerX + radius * Math.cos(radian));
		float y = (float) (centerY + radius * Math.sin(radian));
		paint.setColor(ballColor);
		canvas.drawCircle(x, y, ballRadius, paint);
	}
}