package com.asterinet.react.bgactions;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

    public static final int SERVICE_NOTIFICATION_ID = 92901;
    private static final String CHANNEL_ID = "RN_BACKGROUND_ACTIONS_CHANNEL";
    // patch-line by jhlee 2024.12.
//    private static final long[] VIBRATE_PATTERN =
//                        new long[] {100L, 1000L, 200L, 1000L, 200L, 1000L};

    @SuppressLint("UnspecifiedImmutableFlag")
    @NonNull
    public static Notification buildNotification(@NonNull Context context,
            @NonNull final BackgroundTaskOptions bgOptions) {
        // Get info
        final String taskTitle = bgOptions.getTaskTitle();
        final String taskDesc = bgOptions.getTaskDesc();
        final int iconInt = bgOptions.getIconInt();
        final int color = bgOptions.getColor();
        final String linkingURI = bgOptions.getLinkingURI();
        Intent notificationIntent;
        if (linkingURI != null) {
            notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkingURI));
        } else {
            // as RN works on single activity architecture - we don't need to find current activity on behalf of react context
            notificationIntent =
                    new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        }
        final PendingIntent contentIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_NO_CREATE);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        /* patch begin by jhlee. 2024.12
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(taskTitle)
                        .setContentText(taskDesc)
                        .setSmallIcon(iconInt)
                        .setContentIntent(contentIntent)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setColor(color);
        */
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        // title & message & icon && color
                        .setContentTitle(taskTitle)
                        .setContentText(taskDesc)
                        .setSmallIcon(iconInt)
                        .setColor(color)
                        // use can not dismiss notification
                        .setOngoing(bgOptions.getOngoing())
                        // Make this notification automatically dismissed when the user touches it.
                        .setAutoCancel(bgOptions.getAutoCancel())
                        // Set the intent that fires when the user taps the notification.
                        .setContentIntent(contentIntent);
//                        .setDefaults(NotificationCompat.DEFAULT_ALL)
//                        .setFullScreenIntent(contentIntent, true) // 4;
//                        .setVibrate(VIBRATE_PATTERN)
//                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setCategory(NotificationCompat.CATEGORY_ALARM);
        }
        // patch-end

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

        startForeground(SERVICE_NOTIFICATION_ID, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel(BackgroundTaskOptions bgOptions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create channel with title and importance
            final NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, bgOptions.getTaskTitle(), bgOptions.getImportance());
            channel.setDescription(bgOptions.getTaskDesc());
            // vibrate pattern should be set here. can not change after channel created
            long[] vibrates = bgOptions.getVibrate();
            if (vibrates != null) {
                channel.setVibrationPattern(vibrates);
            }
            // show badge or not
            channel.setShowBadge(bgOptions.getShowBadge());;
            final NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
