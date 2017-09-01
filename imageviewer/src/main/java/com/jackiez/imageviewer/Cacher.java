package com.jackiez.imageviewer;

import android.graphics.Bitmap;
import android.support.v4.util.Pools;

/**
 * Created by zsigui on 17-9-1.
 */

public class Cacher {

    private static Pools.SynchronizedPool<Bitmap> sCachePool;

    public void release(Bitmap bp) {
        sCachePool.release(bp);
    }

    public Bitmap acquire() {
        return sCachePool.acquire();
    }

    public void clear() {
        Bitmap tmp;
        while ((tmp = sCachePool.acquire()) != null) {
            if (!tmp.isRecycled())
                tmp.recycle();
        }
    }

}
