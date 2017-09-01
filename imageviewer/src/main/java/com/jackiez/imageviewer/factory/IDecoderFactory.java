package com.jackiez.imageviewer.factory;

import android.graphics.BitmapRegionDecoder;

/**
 * Created by zsigui on 17-9-1.
 */

public interface IDecoderFactory {

    BitmapRegionDecoder create();

    int[] getImageSize();
}
