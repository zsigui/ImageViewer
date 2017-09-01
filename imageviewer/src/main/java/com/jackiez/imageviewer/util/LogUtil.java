package com.jackiez.imageviewer.util;

import android.util.Log;

/**
 * Created by zsigui on 17-9-1.
 */

public class LogUtil {

    private static final String DEFAULT_TAG = "TagOfImageViewer";

    public static void d(String msg) {
        Log.d(DEFAULT_TAG, msg);
    }

    public static void d(Throwable t) {
        if (t != null) {
            Log.d(DEFAULT_TAG, t.getMessage());
            StackTraceElement[] elements = t.getStackTrace();
            int end = elements.length - 4;
            StackTraceElement e;
            Log.d(DEFAULT_TAG, "==================== Print Start ====================");
            for (int i = elements.length - 1; i > end; i--) {
                e = elements[i];
                Log.d(DEFAULT_TAG, e.toString());
            }
            Log.d(DEFAULT_TAG, "==================== Print End ====================");
            t.printStackTrace();
        }
    }
}
