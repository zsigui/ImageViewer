package com.jackiez.imageviewer.util;

/**
 * Created by zsigui on 17-9-1.
 */

public class CheckUtil {


    public static void checkNullAndThrow(Object o) {
        if (o == null) {
            throw new NullPointerException("Can't be null!");
        }
    }
}
