//Class for method of determining if phone has been shaken or not
package com.example.ugotthis;
//Java imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class ShakeDetector implements SensorEventListener
{
    //The gForce that is necessary to register as shake must be greater than 1G (one earth gravity unit).

    //Constants for measurements
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 2000;

    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;

    //Constructor
    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    //Listener for when phone is shaken
    public interface OnShakeListener {
        public void onShake(int count);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //When the accelerometer detects a change
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mListener != null) {
            //Gets the accelerometer sensor values
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //Calculate accelerometer sensor values after gravity has been accounted for
            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            float gForce = (float)Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) { //Phone is determined to have been shaken
                final long now = System.currentTimeMillis(); //Gets the current system time
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                //Updates the timestamp
                mShakeTimestamp = now;
                mShakeCount++;

                mListener.onShake(mShakeCount);
            }
        }
    }
}

