/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemProperties;
import android.util.FloatMath;
import android.util.Log;
//import android.util.Slog;

/**
 * A special helper class used by the WindowManager
 * for receiving notifications from the SensorManager when
 * the orientation of the device has changed.
 *
 * NOTE: If changing anything here, please run the API demo
 * "App/Activity/Screen Orientation" to ensure that all orientation
 * modes still work correctly.
 *
 * You can also visualize the behavior of the WindowOrientationListener.
 * Refer to frameworks/base/tools/orientationplot/README.txt for details.
 *
 * @hide
 */
public abstract class WindowOrientationListener {
    private static final String TAG = "WindowOrientationListener";
    private static final boolean LOG = SystemProperties.getBoolean(
            "debug.orientation.log", false);

    private static final boolean USE_GRAVITY_SENSOR = false;

    private static final String ROTATION_CHANGED_ACTION = "org.mitre.svmp.action.ROTATION_CHANGED";

    private boolean mEnabled;
    boolean mLogEnabled;
    int mCurrentRotation = -1;

    /**
     * Creates a new WindowOrientationListener.
     * 
     * @param context for the WindowOrientationListener.
     */
    public WindowOrientationListener(Context context) {
        this(context, SensorManager.SENSOR_DELAY_UI);
    }
    
    /**
     * Creates a new WindowOrientationListener.
     * 
     * @param context for the WindowOrientationListener.
     * @param rate at which sensor events are processed (see also
     * {@link android.hardware.SensorManager SensorManager}). Use the default
     * value of {@link android.hardware.SensorManager#SENSOR_DELAY_NORMAL 
     * SENSOR_DELAY_NORMAL} for simple screen orientation change detection.
     *
     * This constructor is private since no one uses it.
     */
    private WindowOrientationListener(Context context, int rate) {
        // register a broadcast receiver to determine screen rotation
        context.registerReceiver(
                rotationInfoReceiver, new IntentFilter(ROTATION_CHANGED_ACTION));
    }

    // SVMP addition to process rotation info messages
    private BroadcastReceiver rotationInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // make sure this intent has a valid action
            if( intent.getAction().equals(ROTATION_CHANGED_ACTION) ) {
                // make sure the intent contains expected data
                if( intent.hasExtra("rotation") ) {
                    // get rotation from intent
                    int newRotation = intent.getIntExtra("rotation", -1);
                    // if rotation has changed, trigger an event
                    if( newRotation > -1 && newRotation != mCurrentRotation ) {
                        mCurrentRotation = newRotation;
                        onProposedRotationChanged(mCurrentRotation);
                    }
                }
            }
        }
    };

    /**
     * Enables the WindowOrientationListener so it will monitor the sensor and call
     * {@link #onOrientationChanged} when the device orientation changes.
     */
    public void enable() {
        if (mEnabled == false) {
            if (localLOGV) Log.d(TAG, "WindowOrientationListener enabled");
            mEnabled = true;
        }
    }

    /**
     * Disables the WindowOrientationListener.
     */
    public void disable() {
        if (mEnabled == true) {
            if (localLOGV) Log.d(TAG, "WindowOrientationListener disabled");
            mEnabled = false;
        }
    }

    /**
     * Sets the current rotation.
     *
     * @param rotation The current rotation.
     */
    public void setCurrentRotation(int rotation) {
        mCurrentRotation = rotation;
    }

    /**
     * Gets the proposed rotation.
     *
     * This method only returns a rotation if the orientation listener is certain
     * of its proposal.  If the rotation is indeterminate, returns -1.
     *
     * @return The proposed rotation, or -1 if unknown.
     */
    public int getProposedRotation() {
        if (mEnabled) {
            return mCurrentRotation;
        }
        return -1;
    }

    /**
     * Returns true if sensor is enabled and false otherwise
     */
    public boolean canDetectOrientation() {
        return true;
    }

    /**
     * Called when the rotation view of the device has changed.
     *
     * This method is called whenever the orientation becomes certain of an orientation.
     * It is called each time the orientation determination transitions from being
     * uncertain to being certain again, even if it is the same orientation as before.
     *
     * @param rotation The new orientation of the device, one of the Surface.ROTATION_* constants.
     * @see Surface
     */
    public abstract void onProposedRotationChanged(int rotation);

    /**
     * Enables or disables the window orientation listener logging for use with
     * the orientationplot.py tool.
     * Logging is usually enabled via Development Settings.  (See class comments.)
     * @param enable True to enable logging.
     */
    public void setLogEnabled(boolean enable) {
        mLogEnabled = enable;
    }
}
