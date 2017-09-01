package com.jackiez.imageviewer;

import android.content.Context;
import android.graphics.BitmapRegionDecoder;

import com.jackiez.imageviewer.bean.ImageBlock;
import com.jackiez.imageviewer.factory.IDecoderFactory;

import java.util.List;

/**
 * Created by zsigui on 17-9-1.
 */

public class ImageBlockLoader {


    private Context mContext;

    private List<ImageBlock> mDataList;
    private BitmapRegionDecoder mDecoder;

    private IDecoderFactory mFactory;

    public void setDecoderFactory(IDecoderFactory factory) {
        this.mFactory = factory;
    }

    public void loadImage(IDecoderFactory factory) {
        if (mFactory != null) {
            mDecoder = factory.create();
        }


        // 绘制缓存区域
        // ######
        // #    #
        // #    #
        // ######
        //


    }
}
