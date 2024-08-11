package com.asterinet.react.bgactions;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
// patch begin: by jhlee. 2024.08.09
import android.os.PowerManager;
import android.util.Log;
// patch end

@SuppressWarnings("WeakerAccess")
public class BackgroundActionsModule extends ReactContextBaseJavaModule {

    private static final String TAG = "RNBackgroundActions";

    private final ReactContext reactContext;

    private Intent currentServiceIntent;

    // patch begin: by jhlee. 2024.08.09
    private void handleWakeLock() {
        // PowerManager pm = (PowerManager) this.reactContext.getSystemService(Context.POWER_SERVICE);
        // boolean isScreenOn = pm.isScreenOn();
        // if (isScreenOn == false) {
        //     PowerManager.WakeLock wl =
        //             pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
        //                     | PowerManager.ON_AFTER_RELEASE, "LocalNoti:MyLock");
        //     wl.acquire(5000);
        //     PowerManager.WakeLock wl_cpu =
        //             pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocalNoti:MyCpuLock");
        //     wl_cpu.acquire(5000);
        //     wl.release();
        //     wl_cpu.release();
        // }
    }
    // patch end

    public BackgroundActionsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return TAG;
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void start(@NonNull final ReadableMap options, @NonNull final Promise promise) {
        try {
            // Stop any other intent
            if (currentServiceIntent != null) {
                Log.d("RNBgActions", "stopping prior existing foreground service.");
                reactContext.stopService(currentServiceIntent);
            }
            // Create the service
            currentServiceIntent = new Intent(reactContext, RNBackgroundActionsTask.class);
            // Get the task info from the options
            final BackgroundTaskOptions bgOptions =
                    new BackgroundTaskOptions(reactContext, options);
            currentServiceIntent.putExtras(bgOptions.getExtras());
            // Start the task
            ComponentName componentName = reactContext.startService(currentServiceIntent);
            Log.d("RNBgActions", "foreground service started. class: " + componentName.getClassName() + ", package: " componentName.getPackageName());
            promise.resolve(null);
        } catch (Exception e) {
            Log.e("RNBgActions", "error staring foreground service. " + e.getMessage());
            promise.reject(e);
        }
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void stop(@NonNull final Promise promise) {
        if (currentServiceIntent != null) {
            Log.d("RNBgActions", "stopping foreground service.");
            reactContext.stopService(currentServiceIntent);
        }
        promise.resolve(null);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void updateNotification(@NonNull final ReadableMap options,
            @NonNull final Promise promise) {
        // Get the task info from the options
        try {
            final BackgroundTaskOptions bgOptions =
                    new BackgroundTaskOptions(reactContext, options);
            final Notification notification =
                    RNBackgroundActionsTask.buildNotification(reactContext, bgOptions);
            final NotificationManager notificationManager = (NotificationManager) reactContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            // patch begin by jhlee
            this.handleWakeLock();
            // patch end
            notificationManager.notify(RNBackgroundActionsTask.SERVICE_NOTIFICATION_ID,
                    notification);
        } catch (Exception e) {
            promise.reject(e);
            return;
        }
        promise.resolve(null);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void addListener(String eventName) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void removeListeners(Integer count) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

}
