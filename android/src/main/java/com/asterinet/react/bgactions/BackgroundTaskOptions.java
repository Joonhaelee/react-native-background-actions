package com.asterinet.react.bgactions;

import android.graphics.Color;
import android.os.Bundle;
import android.app.NotificationManager;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;

public final class BackgroundTaskOptions {
    private final Bundle extras;

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
        // importance
        try {
            String v = extras.getString("importance", "");
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            if (v.equals("high")) {
                return NotificationManager.IMPORTANCE_HIGH;
            }
            else if (v.equals("low")) {
                return NotificationManager.IMPORTANCE_LOW;
            }
            else if (v.equals("min")) {
                return NotificationManager.IMPORTANCE_MIN;
            }
            else if (v.equals("none")) {
                return NotificationManager.IMPORTANCE_NONE;
            }
            extras.putInt("importance", importance);            
        } catch (Exception e) {
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

    public boolean getShowBadge() {
        return extras.getBoolean("showBadge");
    }
    public boolean getOngoing() {
        return extras.getBoolean("ongoing");
    }
    public boolean getAutoCancel() {
        return extras.getBoolean("autoCancel");
    }
    public int getImportance() {
        return extras.getBoolean("importance");
    }

    @Nullable
    // new long[] {100L, 1000L, 200L, 1000L, 200L, 1000L}; [idle, vibrate, ...]
    public long[] getVibrate() {
        String[] items = extras.getString("vibrates", "").split(",");
        if (items.length > 0) {
            long[] vibrates = new long[items.length];
            for(int i = 0; i < items.length; i++) {
                try {
                    vibrates[i] = Integer.parseInt(items[i]);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
            return vibrates;
        }
        else {
            return null;
        }
    }
}
