package com.asterinet.react.bgactions;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;

public final class BackgroundTaskOptions {
    private final Bundle extras;
    // patch begin by jhlee
    private static final long[] VIBRATE_DEF = new long[] {100L, 1000L, 200L, 1000L, 200L, 1000L};
    private final List<Long> vibrate = VIBRATE_DEF;
    // patch end    

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
        // Get VIBRATE
        try {
            String vibrate = options.getString("vibrate");
            if (vibrate != null) {
                String[] values = vibrate.split(",");
                if (values.length > 0) {
                    this.vibrate = new ArrayList<Long>();
                    for (String v : values) {
                        this.vibrate.add(Long.parseLong(v));
                    }
                }
                // 빈 값이 오면 진동 없음  
                else {
                    this.vibrate = null;
                }
            }
            // 지정하지 않으면 기본 진동 사용
            else {
                this.vibrate = VIBRATE_DEF;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Task vibrate values illegal");
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
    @Nullable
    public List<Long> getVibrate() {
        return vibrate;
    }
}
