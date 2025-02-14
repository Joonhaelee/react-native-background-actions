package com.asterinet.react.bgactions;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

@SuppressWarnings("WeakerAccess")
public class BackgroundActionsModule extends ReactContextBaseJavaModule {

    private static final String TAG = "RNBackgroundActions";

    private final ReactApplicationContext reactContext;

    private Intent currentServiceIntent;

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
            Log.d(TAG, "starting background task...");
            // Stop any other intent
            if (currentServiceIntent != null) {
                reactContext.stopService(currentServiceIntent);
            }
            // Create the service
            currentServiceIntent = new Intent(reactContext, RNBackgroundActionsTask.class);
            // Get the task info from the options
            final BackgroundTaskOptions bgOptions = new BackgroundTaskOptions(reactContext, options);
            currentServiceIntent.putExtras(bgOptions.getExtras());
            // Start the task
            reactContext.startService(currentServiceIntent);
            Log.d(TAG, "background task started");
            promise.resolve(null);
        } catch (Exception e) {
            Log.e(TAG, "Fail to start background task", e);
            promise.reject(e);
        }
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void stop(@NonNull final Promise promise) {
        if (currentServiceIntent != null)
            reactContext.stopService(currentServiceIntent);
        Log.d(TAG, "background task stopped");
        promise.resolve(null);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void updateNotification(@NonNull final ReadableMap options, @NonNull final Promise promise) {
        // Get the task info from the options
        try {
            final BackgroundTaskOptions bgOptions = new BackgroundTaskOptions(reactContext, options);
            final Notification notification = RNBackgroundActionsTask.buildNotification(reactContext, bgOptions);
            final NotificationManager notificationManager = (NotificationManager) reactContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(bgOptions.getNotificationId(), notification);
            Log.d(TAG, "foreground service notification updated.");
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
