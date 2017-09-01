package com.jackiez.imageviewer.factory;

import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;

import com.jackiez.imageviewer.util.CheckUtil;
import com.jackiez.imageviewer.util.LogUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by zsigui on 17-9-1.
 */

public class FileDecoderFactory implements IDecoderFactory {

    private String path;

    public FileDecoderFactory(File file) {
        CheckUtil.checkNullAndThrow(file);
        if (!file.exists()) {
            throw new IllegalStateException("[" + file.getName() + "] not exists");
        }
        if (!file.isFile()) {
            throw new IllegalStateException("[" + file.getName() + "] not a file");
        }
        if (!file.canRead()) {
            throw new IllegalStateException("[" + file.getName() + "] no read permission");
        }
        path = file.getAbsolutePath();
    }

    public FileDecoderFactory(String path) {
        this(new File(path));
    }

    @Override
    public BitmapRegionDecoder create() {
        try {
            return BitmapRegionDecoder.newInstance(path, false);
        } catch (IOException e) {
            LogUtil.d(e);
            return null;
        }
    }

    @Override
    public int[] getImageSize() {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, op);
        return new int[]{op.outWidth, op.outHeight};
    }
}
