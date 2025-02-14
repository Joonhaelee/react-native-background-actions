package com.asterinet.react.bgactions;

import android.graphics.Color;
import android.os.Bundle;
import android.app.NotificationManager;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;

public final class BackgroundTaskOptions {
    private final Bundle extras;
    private static final String CHANNEL_ID_DEFAULT = "RN_BACKGROUND_ACTIONS_CHANNEL";
    public static final int NOTIFICATION_ID_DEFAULT = 92901;
    private static final String TAG = "RNBackgroundTaskOptions";

    public BackgroundTaskOptions(@NonNull final Bundle extras) {
        this.extras = extras;
    }

    public BackgroundTaskOptions(@NonNull final ReactContext reactContext, @NonNull final ReadableMap options) {
        // Create extras
        extras = Arguments.toBundle(options);
        if (extras == null)
            throw new IllegalArgumentException("Could not convert arguments to bundle");
        // Get taskTitle
        try {
            if (options.getString("taskTitle") == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Task title cannot be null");
        }
        // Get taskDesc
        try {
            if (options.getString("taskDesc") == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Task description cannot be null");
        }
        // Get iconInt
        try {
            final ReadableMap iconMap = options.getMap("taskIcon");
            if (iconMap == null)
                throw new IllegalArgumentException();
            final String iconName = iconMap.getString("name");
            final String iconType = iconMap.getString("type");
            String iconPackage;
            try {
                iconPackage = iconMap.getString("package");
                if (iconPackage == null)
                    throw new IllegalArgumentException();
            } catch (Exception e) {
                // Get the current package as default
                iconPackage = reactContext.getPackageName();
            }
            final int iconInt = reactContext.getResources().getIdentifier(iconName, iconType, iconPackage);
            extras.putInt("iconInt", iconInt);
            if (iconInt == 0)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Task icon not found");
        }
        // Get color
        try {
            final String color = options.getString("color");
            extras.putInt("color", Color.parseColor(color));
        } catch (Exception e) {
            extras.putInt("color", Color.parseColor("#ffffff"));
        }
        // ongoing
        extras.putBoolean("ongoing", options.hasKey("ongoing") && options.getBoolean("ongoing"));
        // autoCancel
        extras.putBoolean("autoCancel", options.hasKey("autoCancel") && options.getBoolean("autoCancel"));
        // notification id
        if (options.hasKey("notificationId")) {
            final int v = options.getInt("notificationId");
            extras.putInt("notificationId", v > 0 ? v : NOTIFICATION_ID_DEFAULT);
        }
        else {
            extras.putInt("notificationId", NOTIFICATION_ID_DEFAULT);
        }
        // channel id. 주의 생성된 채널은 앱을 삭제하기 전까지 지속됩니다.
        if (options.hasKey("channelId")) {
            final String cid = options.getString("channelId");
            extras.putString("channelId", cid != null && !cid.isEmpty() ? cid : CHANNEL_ID_DEFAULT);
        }
        else {
            extras.putString("channelId", CHANNEL_ID_DEFAULT);
        }
        // channel importance
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (options.hasKey("channelImportance")) {
            final String imp = options.getString("channelImportance");
            if (imp != null) {
                importance = switch (imp.toLowerCase()) {
                    case "high" -> NotificationManager.IMPORTANCE_HIGH;
                    case "low" -> NotificationManager.IMPORTANCE_LOW;
                    case "min" -> NotificationManager.IMPORTANCE_MIN;
                    case "none" -> NotificationManager.IMPORTANCE_NONE;
                    default -> NotificationManager.IMPORTANCE_DEFAULT;
                };
            }
        }
        extras.putInt("channelImportance", importance);
        // channel sound
        extras.putBoolean("channelSound", options.hasKey("channelSound") && options.getBoolean("channelSound"));
        // channel showBadge
        extras.putBoolean("channelShowBadge", options.hasKey("channelShowBadge") && options.getBoolean("channelShowBadge"));
        // channel vibrate
        if (options.hasKey("channelVibrate")) {
            extras.putString("channelVibrate", options.getString("channelVibrate"));
        }
    }

    public Bundle getExtras() {
        return extras;
    }

    public String getTaskTitle() {
        return extras.getString("taskTitle", "");
    }

    public String getTaskDesc() {
        return extras.getString("taskDesc", "");
    }

    @IdRes
    public int getIconInt() {
        return extras.getInt("iconInt");
    }

    @ColorInt
    public int getColor() {
        return extras.getInt("color");
    }

    @Nullable
    public String getLinkingURI() {
        return extras.getString("linkingURI");
    }

    @Nullable
    public Bundle getProgressBar() {
        return extras.getBundle("progressBar");
    }
    public boolean getOngoing() {
        return extras.getBoolean("ongoing");
    }
    public boolean getAutoCancel() {
        return extras.getBoolean("autoCancel");
    }
    public int getNotificationId() {
        return extras.getInt("notificationId");
    }
    // importance, showBadge, vibrate for channel !
    public int getChannelImportance() {
        return extras.getInt("channelImportance");
    }
    public boolean getChannelSound() {
        return extras.getBoolean("channelSound");
    }
    public boolean getChannelShowBadge() {
        return extras.getBoolean("channelShowBadge");
    }
    // new long[] {100L, 1000L, 200L, 1000L, 200L, 1000L}; [idle, vibrate, ...]
    @Nullable
    public String getChannelVibrate() {
        return extras.getString("channelVibrate");
    }
    public String getChannelId() {
        return extras.getString("channelId");
    }

}
