package hr.riteh.apaslab.apaslab.misc;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public abstract class OnSwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
	private static final String TAG = "Gestures";

	@Override
	public boolean onDown(MotionEvent event) {
		Log.d(TAG, "onDown: " + event.toString());
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.d(TAG, "distanceY: " + distanceY);
		onSwipe(distanceY);
		return true;
	}
	
	 public abstract void onSwipe(float distanceY);

}
