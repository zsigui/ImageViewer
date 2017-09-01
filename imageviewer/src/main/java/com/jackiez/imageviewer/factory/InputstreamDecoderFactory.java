package com.jackiez.imageviewer.factory;

import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;

import com.jackiez.imageviewer.util.CheckUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zsigui on 17-9-1.
 */

public class InputstreamDecoderFactory implements IDecoderFactory {

    private InputStream mStream;

    public InputstreamDecoderFactory(InputStream stream) {
        CheckUtil.checkNullAndThrow(stream);
        mStream = stream;
    }

    @Override
    public BitmapRegionDecoder create() {
        try {
            return BitmapRegionDecoder.newInstance(mStream, false);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int[] getImageSize() {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(mStream, null, op);
        return new int[]{op.outWidth, op.outHeight};
    }
}
