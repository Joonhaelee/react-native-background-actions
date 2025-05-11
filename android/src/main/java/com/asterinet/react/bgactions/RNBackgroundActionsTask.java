package com.asterinet.react.bgactions;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import android.media.RingtoneManager;
import android.util.Log;

final public class RNBackgroundActionsTask extends HeadlessJsTaskService {

    private static final String TAG = "RNBackgroundActionsTask";

    @Nullable
    private static long[] stringToLongArray(@Nullable String v) {
        if (v != null && !v.isEmpty()) {
            Log.d(TAG, "channelVibrate: " + v);
            String[] items = v.split(",");
            if (items.length > 0) {
                long[] vibrates = new long[items.length];
                for (int i = 0; i < items.length; i++) {
                    try {
                        vibrates[i] = Long.parseLong(items[i]);
                    } catch (NumberFormatException nfe) {
                        Log.e(TAG, "invalid vibrate value");
                        return null;
                    }
                }
                return vibrates;
            }
        }
        return null;
    }

    private static Class<?> getMainActivityClass(Context context) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null || launchIntent.getComponent() == null) {
            Log.e(TAG, "Failed to get launch intent or component");
            return null;
        }
        try {
            return Class.forName(launchIntent.getComponent().getClassName());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed to get main activity class");
            return null;
        }
    }
    @SuppressLint("UnspecifiedImmutableFlag")
    @NonNull
    public static Notification buildNotification(@NonNull Context context,
                                                 @NonNull final BackgroundTaskOptions bgOptions) {
        Log.d(TAG, "buildNotification...");
        // Get info
        final String linkingURI = bgOptions.getLinkingURI();
        Intent notificationIntent;
        if (linkingURI != null) {
            notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkingURI));
        } else {
            Class<?> mainActivityClass = getMainActivityClass(context);
            if (mainActivityClass == null) {
                throw new IllegalArgumentException("RN main activity class not found");
            }
            notificationIntent = new Intent(context, mainActivityClass);
        }
        final PendingIntent contentIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_NO_CREATE);
        } else {
            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        final String channelId = bgOptions.getChannelId();
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        // .setDefaults(NotificationCompat.DEFAULT_ALL)
                        // title & message & icon && color
                        .setContentTitle(bgOptions.getTaskTitle())
                        .setContentText(bgOptions.getTaskDesc())
                        .setSmallIcon(bgOptions.getIconInt())
                        .setColor(bgOptions.getColor())
                        // use can not dismiss notification
                        .setOngoing(bgOptions.getOngoing())
                        // Make this notification automatically dismissed when the user touches it.
                        .setAutoCancel(bgOptions.getAutoCancel())
                        // Set the intent that fires when the user taps the notification.
                        .setContentIntent(contentIntent)
                        // fire notification immediately
                        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setCategory(NotificationCompat.CATEGORY_ALARM);
        }

        final Bundle progressBarBundle = bgOptions.getProgressBar();
        if (progressBarBundle != null) {
            final int progressMax = (int) Math.floor(progressBarBundle.getDouble("max"));
            final int progressCurrent = (int) Math.floor(progressBarBundle.getDouble("value"));
            final boolean progressIndeterminate = progressBarBundle.getBoolean("indeterminate");
            builder.setProgress(progressMax, progressCurrent, progressIndeterminate);
        }
        return builder.build();
    }

    @Override
    protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            return new HeadlessJsTaskConfig(extras.getString("taskName"),
                    Arguments.fromBundle(extras), 0, true);
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Bundle extras = intent.getExtras();
        if (extras == null) {
            throw new IllegalArgumentException("Extras cannot be null");
        }
        final BackgroundTaskOptions bgOptions = new BackgroundTaskOptions(extras);
        createNotificationChannel(bgOptions);
        // Create the notification
        final Notification notification = buildNotification(this, bgOptions);
        startForeground(bgOptions.getNotificationId(), notification);
        Log.d(TAG, "foreground service started");
        return super.onStartCommand(intent, flags, startId);
    }

    /* notification channel.
       한번 생성되면 앱이 지워지지 않는 이상 계속 유지됨. 따라서, 처음 생성할 때 필요한 인자를 주어야 함.
       importance, vibrate, sound, showBadge 는 채널에서 설정해야 합니다.
    */
    private void createNotificationChannel(BackgroundTaskOptions bgOptions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager.getNotificationChannel(bgOptions.getChannelId()) == null) {
                // create channel with title and importance
                final NotificationChannel channel =
                        new NotificationChannel(bgOptions.getChannelId(), bgOptions.getTaskTitle(), bgOptions.getChannelImportance());
                channel.setDescription(bgOptions.getTaskDesc());
                // vibrate pattern should be set here. can not change after channel created
                long[] vibrates = stringToLongArray(bgOptions.getChannelVibrate());
                if (vibrates != null) {
                    channel.enableVibration(true);
                    channel.setVibrationPattern(vibrates);
                }
                // sound
                if (bgOptions.getChannelSound()) {
                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build();
                    channel.setSound(soundUri, audioAttributes);
                }
                // show badge or not
                channel.setShowBadge(bgOptions.getChannelShowBadge());;
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, String.format("notification channel created. channelId=%s", bgOptions.getChannelId()));
            }
        }
    }
}
