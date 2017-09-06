package com.jackiez.imageviewer.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zsigui on 17-9-4.
 */

public class TestView extends View {

    public TestView(Context context) {
        this(context, null);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mOptions = new BitmapFactory.Options();
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        ScaleGestureDetector.OnScaleGestureListener scaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                int focusX = (int) (detector.getFocusX() + 0.5);
                int focusY = (int) (detector.getFocusY() + 0.5);

                Log.d("TestView", "onScale.scaleFactory = " + detector.getScaleFactor());
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        };
        mScaleDetector = new ScaleGestureDetector(getContext(), scaleListener);
    }

    /**
     * 自动判断以宽或高作为初始显示图片的标准
     */
    public static int LOAD_BOTH = 0;
    /**
     * 规定以高作为初始显示图片的标准
     */
    public static int LOAD_VERTICAL = 1;
    /**
     * 规定以宽作为初始显示图片的标准
     */
    public static int LOAD_HORIZONTAL = 2;

    private int mLoadState = LOAD_HORIZONTAL;
    private BitmapRegionDecoder mDecoder;
    private ScaleGestureDetector mScaleDetector;
    private int srcImageHeight;
    private int srcImageWidth;
    // 定义当前像素尺寸下可见
    private int centerX;
    private int centerY;
    private float mScale = 1;
    private int offset = 0;


    /**
     * 屏幕图片可见区域，最大跟屏幕区域相同
     */
    private Rect mVisibleRect;
    /**
     * 视图屏幕区域
     */
    private Rect mScreenRect;
    /**
     * 实际图片大小区域
     */
    private Rect mPicRect;
    private BitmapFactory.Options mOptions;
    private Paint mPaint;
    private boolean mNeedInit;

    private void initImageInfo() {
        Log.d("TestView", "initImageInfo.call()");
        if (mScreenRect == null
                || mScreenRect.width() == 0
                || mScreenRect.height() == 0) {
             mNeedInit = true;
            return;
        }

        srcImageHeight = mDecoder.getHeight();
        srcImageWidth = mDecoder.getWidth();
        Log.d("TestView", "initImageInfo.call()： w = " + srcImageWidth + ", h = " + srcImageHeight);

        final int screenWidth = mScreenRect.width();
        final int screenHeight = mScreenRect.height();

        // 对图片区域进行初始化
        if (mPicRect == null) {
            mPicRect = new Rect(0, 0, srcImageWidth, srcImageHeight);
        } else {
            mPicRect.left = 0;
            mPicRect.right = srcImageWidth;
            mPicRect.top = 0;
            mPicRect.bottom = srcImageHeight;
        }
        if (mVisibleRect == null) {
            mVisibleRect = new Rect(0, 0, screenWidth, screenHeight);
        } else {
            mVisibleRect.left = 0;
            mVisibleRect.right = screenWidth;
            mVisibleRect.top = 0;
            mVisibleRect.bottom = screenHeight;
        }


        int tmpImgWidth = srcImageWidth;
        int tmpImgHeight = srcImageHeight;
        // 获取显示的宽高比例
        float imgRate = (float) tmpImgWidth / tmpImgHeight;
        float screenRate = (float) screenWidth / screenHeight;
        if ((mLoadState == LOAD_VERTICAL) ||
                (mLoadState == LOAD_BOTH && imgRate > screenRate)) {
            // 以高作为标准，宽进行变动
            int realW = (int) (screenHeight * imgRate + 0.5f);
            if (realW > screenWidth) {
                mPicRect.right = (int) (srcImageHeight * screenRate + 0.5f);
                realW = screenWidth;
            }
            mVisibleRect.left = (screenWidth - realW) >> 1;
            mVisibleRect.right = mVisibleRect.left + realW;
        } else {
            // 默认，以宽作为标准，高进行变动
            int realH = (int) (screenWidth / imgRate + 0.5f);
            if (realH > screenHeight) {
                mPicRect.bottom = (int) (srcImageWidth / screenRate + 0.5f);
                realH = screenHeight;
            }
            mVisibleRect.top = (screenHeight - realH) >> 1;
            mVisibleRect.bottom = mVisibleRect.top + realH;
        }

        // 初始化选择最适合的缩放比例
        // 合适的缩放比例能够达成具有同样分辨率效果的情况下，占内存更少
        // 原理是加载同样比例的图形块，然后缩放填充到更小的可见区域中，那么总体缩放效果一致，但是提前缩放减少了先将未缩放的图片加载到内存中
        final int visWidth = mVisibleRect.width();
        final int visHeight = mVisibleRect.height();
        if (tmpImgHeight > visHeight
                || tmpImgWidth > visWidth) {
            // 图片区域 > 可见区域，进行缩放，找到适合的缩放精度
            int startScale = 1;
            mOptions.inSampleSize = 1;
            while (tmpImgHeight > visHeight
                    && tmpImgWidth > visWidth) {
                tmpImgHeight = tmpImgHeight >> 1;
                tmpImgWidth = tmpImgWidth >> 1;
                startScale = startScale << 1;
            }
            Log.d("TestView", "startStcale = " + startScale);
            mOptions.inSampleSize = startScale;
        }


        Log.d("TestView", "pic.l = " + mPicRect.left + ", t = " + mPicRect.top + ", r = " + mPicRect.right + ", b = " + mPicRect.bottom);
        Log.d("TestView", "vis.l = " + mVisibleRect.left + ", t = " + mVisibleRect.top + ", r = " + mVisibleRect.right + ", b = " + mVisibleRect.bottom);

    }

    public void setImage(InputStream is) {
        try {
            if (mDecoder != null && !mDecoder.isRecycled()) {
                mDecoder.recycle();
            }
            mDecoder = BitmapRegionDecoder.newInstance(is, false);
            initImageInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setImage(String path) {
        try {
            if (mDecoder != null && !mDecoder.isRecycled()) {
                mDecoder.recycle();
            }
            mDecoder = BitmapRegionDecoder.newInstance(path, false);
            initImageInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.w("TestView", "点击位置坐标：(" + event.getX() + ", " + event.getY() + ")");
            mDownX = (int) (event.getX() + 0.5f);
            mDownY = (int) (event.getY() + 0.5f);
        }
        return super.onTouchEvent(event);
    }

    int mScrollX = 0;
    int mScrollY = 0;
    int mDownX;
    int mDownY;

    /**
     * 模拟放大操作处理
     */
    public void mockClick() {
        // 定义放大点位置 (1/2 * vw, 1/3 * vh)
        // scrollX, scrollY
        Log.d("TestView", "mockClick.call()");
//        int scaleX = mVisibleRect.width() >> 1;
//        int scaleY = mVisibleRect.height() / 3;
        int scaleX = mDownX;
        int scaleY = mDownY;

        int realScaleX = (int) (scaleX / mScale + 0.5f);
        int realScaleY = (int) (scaleY / mScale + 0.5f);

        if (mScale == 1) {
            // scale > 1 表示放大，即是单位可见区域显示像素缩小， scale < 1 则是缩小
            // 此处表示放大两倍
            mScale = 2;
        } else {
            mScale = 1;
        }
        int realImgWidth = (int) (srcImageWidth / mScale + 0.5f);
        int realImgHeight = realImgWidth * getMeasuredHeight() / getMeasuredWidth();
        // 计算中心点坐标的实际位置
        int relativeX = (int) (realImgWidth * ((float) scaleX / mVisibleRect.width()) + 0.5f);
        int relativeY = (int) (realImgHeight * ((float) scaleY / mVisibleRect.height()) + 0.5f);
        Log.d("TestView", "mockClick.call() rx = " + relativeX + ", ry = " + relativeY + ", riw = " +realImgWidth + ", rih = " + realImgHeight
        + ", realScaleX = " + realScaleX + ", realScaleY = " + realScaleY);
        if (relativeX > realScaleX + mScrollX) {
            mPicRect.left = 0;
            mPicRect.right = realImgWidth;
        } else {
            mPicRect.left = realScaleX + mScrollX - relativeX;
            mPicRect.right = mPicRect.left + realImgWidth;
        }

        if (relativeY > realScaleY + mScrollY) {
            mPicRect.top = 0;
            mPicRect.bottom = realImgHeight;
        } else {
            mPicRect.top = relativeY + mScrollY - relativeY;
            mPicRect.bottom = mPicRect.top + realImgHeight;
        }
        invalidate();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("TestView", "onMeasure! w = " + w + ", h = " + h);
    }

    @Override
    public void layout(@Px int l, @Px int t, @Px int r, @Px int b) {
        super.layout(l, t, r, b);
        Log.d("TestView", "onLayout!");
        if (mScreenRect == null) {
            //mScreenRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            Log.d("TestView", "layout() = " + getLeft() + ", " + getTop() + ", " + getRight() + ", " + getBottom());
            // or (需要进行测试)
            mScreenRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
            Log.d("TestView", "layout() w.h = " + getWidth() + ", " + getHeight());
        }
        if (mNeedInit) {
            mScreenRect.right = getMeasuredWidth();
            mScreenRect.bottom = getMeasuredHeight();
            initImageInfo();
            mNeedInit = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("TestView", "onDraw.isCall = " + mPicRect.left + ", " + mPicRect.top + ", " + mPicRect.right + ", " + mPicRect.bottom);
        // 进行当前图片区域计算
        // 计算切割实际图形区域大小，再实际绘制到屏幕上
        Bitmap bp = mDecoder.decodeRegion(mPicRect, mOptions);
        canvas.drawBitmap(bp, mPicRect, mVisibleRect, mPaint);

    }

}
