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
//        if (tmpImgHeight > visHeight
//                || tmpImgWidth > visWidth) {
//            // 图片区域 > 可见区域，进行缩放，找到适合的缩放精度
//            int startScale = 1;
//            mOptions.inSampleSize = 1;
//            while (tmpImgHeight > visHeight
//                    && tmpImgWidth > visWidth) {
//                tmpImgHeight = tmpImgHeight >> 1;
//                tmpImgWidth = tmpImgWidth >> 1;
//                startScale = startScale << 1;
//            }
//            Log.d("TestView", "startStcale = " + startScale);
//            mOptions.inSampleSize = startScale;
//        }

        mDrawBitmap = mDecoder.decodeRegion(mPicRect, mOptions);
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
    private Bitmap mDrawBitmap;
    private int mViewWidth;
    private int mViewHeight;
    /**
     * 模拟放大操作处理，可在线程中执行
     */
    public void mockClick() {
        // scrollX, scrollY
        Log.d("TestView", "mockClick.call()");
        // 定义放大点位置 (1/2 * vw, 1/3 * vh)
//        int scaleX = mVisibleRect.width() >> 1;
//        int scaleY = mVisibleRect.height() / 3;
        int downX = mDownX;
        int downY = mDownY;

        final float lastScale = mScale;

        // 以下暂时针对 LoadState == LOAD_HORIZONTAL 这种情况处理
        final int screenHeight = getMeasuredHeight();
        final int screenWidth = getMeasuredWidth();

        // 计算上一次缩放参数下可视视图的宽高
        int visWidth = (int) (screenWidth * lastScale + 0.5f);
        int visHeight = visWidth * srcImageHeight / srcImageWidth;

        // 计算当前缩放中心点所在位置在当前屏幕中的比例值，以后续根据它来确定缩放后的位图区域
        float scalePosFactorX = (float) downX / screenWidth;
        float scalePosFactorY = (float) downY / screenHeight;
        int padding;
        // 当视图中除位图区域外还有留白，则需要考虑留白处点击的处理，此处按边界值计算
        if (visHeight < screenHeight) {
            padding = (screenHeight - visHeight) >> 1;
            if (downY <= padding) {
                scalePosFactorY = 0f;
            } else if (downY >= visHeight + padding) {
                scalePosFactorY = 1f;
            } else {
                scalePosFactorY = (float) (downY - padding) / visHeight;
            }
        }
        if (visWidth < screenWidth) {
            padding = (screenWidth - visWidth) >> 1;
            if (downX < padding) {
                scalePosFactorX = 0f;
            } else if (downY >= visWidth + padding) {
                scalePosFactorX = 1f;
            } else {
                scalePosFactorX = (float) (downX - padding) / visWidth;
            }
        }

        float curScale; // 此处新的缩放值需要通过判断获取
        // scale > 1 表示放大，即是单位可见区域显示像素缩小,相当于缩小可视图里的位图区域， scale < 1 则是相反，表示缩小
        if (lastScale > 1) {
            // 此前处于放大状态，恢复到1
            curScale = 1;
        } else {
            // 此前处于默认或者缩小状态，判断是否位图高度小于可见视图高度，小于则计算缩放该差距，否则直接进行2倍放大
            if (screenWidth != 0 && visHeight < screenHeight) {
                // (visHeight == screenHeight) = screenWidth * lastScale * srcImageHeight / srcImageWidth;
                curScale = (screenHeight * srcImageWidth) / (screenWidth * srcImageHeight);
            } else
                curScale = 2;
        }
        Log.d("TestView", "curScale = " + curScale + ", lastScale = " + lastScale);
        // 根据新的缩放值计算当前可视视图区域及对应位图区域
        int realImgWidth = (int) (srcImageWidth / curScale + 0.5f);
        int realImgHeight = realImgWidth * getMeasuredHeight() / getMeasuredWidth();

        // 区分是缩小还是放大
        int lastScrollX = mScrollX;
        int lastScrollY = mScrollY;
        int curScrollX, curScrollY;
        // 依据 (lastScrollX + downX) / (curScrollX + downX) = lastScale / curScale 及 curScrollX = lastScrollX + diffX
        // 则当 diffX > 0 时，表示放大，因为可滚动区域变大了， diffX < 0 时，表示缩小
        int diffX = (int) ((lastScrollX + downX) * (curScale / lastScale - 1) + 0.5f);
        int diffY = (int) ((lastScrollY + downY) * (curScale / lastScale - 1) + 0.5f);
        curScrollX = lastScrollX + diffX;
        curScrollY = lastScrollY + diffY;
        Log.d("TestView", "lastScrollX = " + lastScrollX + ", lastScrollY = " + lastScrollY
         + ", curScrollX = " + curScrollX + ", curScrollY = " + curScrollY + ", diffX = " + diffX + ", diffY = " + diffY);
        // 进行滚动距离边界的判断
        if (curScrollX < 0)
            curScrollX = 0;
        else if (curScrollX > getScrollRangeX(curScale))
            curScrollX = getScrollRangeX(curScale);
        if (curScrollY < 0)
            curScrollY = 0;
        else if (curScrollY > getScrollRangeY(curScale))
            curScrollY = getScrollRangeY(curScale);

        visWidth = (int) (screenWidth * curScale + 0.5f);
        visHeight = visWidth * srcImageHeight / srcImageWidth;
        // 表示新缩放值下，图片展示的可见区域在视图范围之内，置中
        // 有必要还需要考虑padding的存在,暂时这里先不考虑减少复杂度
        float rW = srcImageWidth / (screenWidth * curScale);
        Log.d("TestView", "rW = " + rW);
        if (visWidth < screenWidth) {
            mVisibleRect.left = (screenWidth - visWidth) >> 1;
            mVisibleRect.right = mVisibleRect.left + visWidth;
            // 此时图片区域宽部分肯定是完全显示的
            mPicRect.left = 0;
            mPicRect.right = srcImageWidth;
        } else {
            Log.d("TestView", "viewWidth > screenWidth : curScrollX = " + curScrollX);
            mVisibleRect.left = 0;
            mVisibleRect.right = screenWidth;
            mPicRect.left = (int) (curScrollX * rW + 0.5f);
            mPicRect.right = (int) (mPicRect.left + screenWidth * rW + 0.5f);
            if (mPicRect.right > srcImageWidth) {
                mPicRect.right = screenWidth;
            }
        }
        if (visHeight < screenHeight) {
            mVisibleRect.top = (screenHeight - visHeight) >> 1;
            mVisibleRect.bottom = mVisibleRect.top + visHeight;
            mPicRect.top = 0;
            mPicRect.bottom = srcImageHeight;
        } else {
            Log.d("TestView", "viewHeight > screenHeight : curScrollY = " + curScrollY);
            mVisibleRect.top = 0;
            mVisibleRect.bottom = screenHeight;
            mPicRect.top = (int) (curScrollY * rW + 0.5f);
            mPicRect.bottom = (int) (mPicRect.top + screenHeight * rW + 0.5f);
            if (mPicRect.bottom > srcImageHeight)
                mPicRect.bottom = srcImageHeight;
        }


        Log.d("TestView", "pic.l = " + mPicRect.left + ", t = " + mPicRect.top + ", r = " + mPicRect.right + ", b = " + mPicRect.bottom);
        Log.d("TestView", "vis.l = " + mVisibleRect.left + ", t = " + mVisibleRect.top + ", r = " + mVisibleRect.right + ", b = " + mVisibleRect.bottom);

        mScrollX = curScrollX;
        mScrollY = curScrollY;
        mScale = curScale;
        mOptions.inBitmap = mDrawBitmap;
        mDrawBitmap = mDecoder.decodeRegion(mPicRect, mOptions);
        // 请求重新绘制
        postInvalidate();
    }

    private boolean canScrollX(float scale) {
        if (getMeasuredWidth() == 0) {
            mViewWidth = 0;
        } else if (mViewWidth == 0) {
            mViewWidth = (int) (getMeasuredWidth() * scale + 0.5f);
        }
        return mViewWidth != 0 && mViewWidth > getMeasuredWidth();
    }

    private boolean canScrollY(float scale) {
        if (getMeasuredHeight() == 0)
            mViewHeight = 0;
        else if (mViewHeight == 0) {
            canScrollX(scale);
            mViewHeight = mViewWidth * srcImageHeight / srcImageWidth;
        }
        return mViewHeight != 0 && mViewHeight > getMeasuredHeight();
    }

    private int getScrollRangeY(float scale) {
        if (!canScrollY(scale))
            return 0;
        return mViewHeight - getMeasuredHeight();
    }

    private int getScrollRangeX(float scale) {
        if (!canScrollX(scale))
            return 0;
        return mViewWidth - getMeasuredWidth();
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
//        Bitmap bp = mDecoder.decodeRegion(mPicRect, mOptions);
//        canvas.drawBitmap(bp, mPicRect, mVisibleRect, mPaint);
        if (mDrawBitmap != null)
            canvas.drawBitmap(mDrawBitmap, mPicRect, mVisibleRect, mPaint);

    }

}
