package com.agilegeodata.carriertrack.android.objects;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/* Used on the log viewer as a container for the delete and clear buttons
 *
 */
public class TransparentPanel extends LinearLayout{
	private Paint innerPaint, borderPaint;

	public TransparentPanel(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public TransparentPanel(Context context){
		super(context);
		init();
	}

	private void init(){
		innerPaint = new Paint();
		innerPaint.setARGB(0, 75, 75, 75); //gray
		innerPaint.setAntiAlias(true);

		borderPaint = new Paint();
		borderPaint.setARGB(0, 255, 255, 255);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(2);
	}

	public void setInnerPaint(Paint innerPaint){
		this.innerPaint = innerPaint;
	}

	public void setBorderPaint(Paint borderPaint){
		this.borderPaint = borderPaint;
	}

	@Override
	protected void dispatchDraw(Canvas canvas){

		RectF drawRect = new RectF();
		drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

		canvas.drawRoundRect(drawRect, 5, 5, innerPaint);
		canvas.drawRoundRect(drawRect, 5, 5, borderPaint);

		super.dispatchDraw(canvas);
	}
}