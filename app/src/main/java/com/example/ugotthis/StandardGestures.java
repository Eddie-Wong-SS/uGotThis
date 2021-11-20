//Class that allows for usage of multitouch(pinch and double tap) to zoom in and out of views. Also includes scrolling functionality
//Only pinch and scroll functionality is enabled currently
package com.example.ugotthis;
//Java imports
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

public class StandardGestures implements View.OnTouchListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
    private View view;
    private GestureDetector gesture;
    private ScaleGestureDetector gestureScale;
    private float scaleFactor = 2;
    private boolean inScale;
    //Constructor
    public StandardGestures(Context c){
        gesture = new GestureDetector(c, this);
        gestureScale = new ScaleGestureDetector(c, this);
    }
    //Sets view and calls other functions when touch event is detected
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        this.view = view;
        gesture.onTouchEvent(event);
        gestureScale.onTouchEvent(event);
        return true;
    }
    //Motion first detected on screen
    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }
    //Motion detected moving without leaving screen surface
    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float x, float y) {
        return true;
    }
    //Focus detected on specific view for long period of time
    @Override
    public void onLongPress(MotionEvent event) {
    }
    //Allows for scrolling ability in view
    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float x, float y) {
        float newX = view.getX();
        float newY = view.getY();
        if(!inScale){
            newX -= x;
            newY -= y;
        }
        WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        Point p = new Point();
        d.getSize(p);
        //Determine new position scroll should go to
        if (newX > (view.getWidth() * scaleFactor - p.x) / 2){
            newX = (view.getWidth() * scaleFactor - p.x) / 2;
        } else if (newX < -((view.getWidth() * scaleFactor - p.x) / 2)){
            newX = -((view.getWidth() * scaleFactor - p.x) / 2);
        }

        if (newY > (view.getHeight() * scaleFactor - p.y) / 2){
            newY = (view.getHeight() * scaleFactor - p.y) / 2;
        } else if (newY < -((view.getHeight() * scaleFactor - p.y) / 2)){
            newY = -((view.getHeight() * scaleFactor - p.y) / 2);
        }

        view.setX(newX);
        view.setY(newY);

        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }
    //Single tap motion detected
    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }
    //Double tap motion detected
    @Override
    public boolean onDoubleTap(MotionEvent event) {

        return true;
    }
    //Double tap was detected
    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return true;
    }
    //Single tap was detected
    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return true;
    }
    //Scales view to either zoom in or out
    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        scaleFactor *= detector.getScaleFactor();
        scaleFactor = scaleFactor < 1 ? 1 : scaleFactor; // prevent the image from becoming too small
        scaleFactor = (float) (int) (scaleFactor * 100) / 100; // Change precision to help with jitter when user just rests their fingers //
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);
        onScroll(null, null, 0, 0); // call scroll to make sure bounds are still ok //
        return true;
    }
    //Scaling of view begins
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        inScale = true;
        return true;
    }
    //Scaling of view ends
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        inScale = false;
        onScroll(null, null, 0, 0); // call scroll to make sure our bounds are still ok //
    }
}