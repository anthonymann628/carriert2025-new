package com.agilegeodata.carriertrack.android.herenavigation;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.here.sdk.core.Point2D;
import com.here.sdk.mapview.MapCamera;

/**
 * A simple class that takes care of smooth zoom gestures.
 */
public class GestureMapAnimator{

	private final MapCamera camera;
	Point2D zoomOrigin;
	private ValueAnimator zoomValueAnimator;

	@SuppressLint("ClickableViewAccessibility")
	public GestureMapAnimator(MapCamera camera){
		this.camera = camera;
	}

	//=== Starts the zoom in animation.
	public void zoomIn(Point2D touchPoint){
		zoomOrigin = touchPoint;
		startZoomAnimation(true);
	}

	//=== Starts the zoom out animation.
	public void zoomOut(Point2D touchPoint){
		zoomOrigin = touchPoint;
		startZoomAnimation(false);
	}

	private void startZoomAnimation(boolean zoomIn){
		stopAnimations();

		//=== A new Animator that zooms the map.
		zoomValueAnimator = createZoomValueAnimator(zoomIn);

		//=== Start the animation.
		zoomValueAnimator.start();
	}

	private ValueAnimator createZoomValueAnimator(boolean zoomIn){
		ValueAnimator zoomValueAnimator = ValueAnimator.ofFloat(0.1F, 0);
		zoomValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		zoomValueAnimator.addUpdateListener(animation -> {
			// Called periodically until zoomVelocity is zero.
			float zoomVelocity = (float) animation.getAnimatedValue();
			double zoomFactor = 1;
			zoomFactor = zoomIn ? zoomFactor + zoomVelocity : zoomFactor - zoomVelocity;
			//=== zoomFactor values > 1 will zoom in and values < 1 will zoom out.
			camera.zoomBy(zoomFactor, zoomOrigin);
		});

		long halfSecond = 500;
		zoomValueAnimator.setDuration(halfSecond);

		return zoomValueAnimator;
	}

	//=== Stop any ongoing zoom animation.
	public void stopAnimations(){
		if(zoomValueAnimator != null){
			zoomValueAnimator.cancel();
		}
	}
}