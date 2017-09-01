package com.jackiez.imageviewer.bean;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Created by zsigui on 17-9-1.
 */

public class ImageBlock {

    /**
     * 切割出来的Bitmap
     */
    public Bitmap scrapBitmap;

    /**
     * 切割的位置大小
     */
    public Rect scrapRect;

    /**
     * 需要进行缩放的倍率
     */
    public float scale;

    /**
     * 需要绘制的区域所属行
     */
    public int row;

    /**
     * 需要绘制的区域所属列
     */
    public int col;
}
