package com.jackiez.imageviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.jackiez.imageviewer.bean.ImageBlock;

import java.util.List;

/**
 * Created by zsigui on 17-9-1.
 */

public class LargeImageView extends View {


    /**
     * 图片的原始宽度
     */
    private int mImageWidth;
    /**
     * 图片的原始高度
     */
    private int mImageHeight;
    /**
     * 当前的缩放比率，默认为1
     */
    private float mCurScale;

    /**
     * 最大缩放比率，一旦超过这个值，则在触摸结束时自动动画恢复到该缩放比率
     */
    private float mMaxScale;
    /**
     * 最小缩放比率，一旦小于这个值，则在触摸结束时自动动画恢复到该缩放比率
     */
    private float mMinScale;

    /**
     * 绘制当前界面的区域
     */
    private List<ImageBlock> mDataList;
    private ImageBlockLoader mLoader;
    /**
     * 默认视图，用于加载高清晰图片之前进行显示的
     */
    private Drawable mDefaultView;

    public LargeImageView(Context context) {
        super(context);
    }

    public LargeImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LargeImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    /**
     * 获取图片于当前缩放比率下的实际宽度
     */
    private int getContentWidth() {
        return (int) (mImageWidth * mCurScale);
    }

    /**
     * 获取图片于当前缩放比率下的实际高度
     */
    private int getContentHeight() {
        return (int) (mImageHeight * mCurScale);
    }

    /**
     * 获取当前缩放比率下，水平方向总的最大可滚动距离
     */
    private int getMaxScrollRangeX() {
        final int visibleWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        return Math.max(0, getContentWidth() - visibleWidth);
    }

    /**
     * 获取当前缩放比率下，垂直方向总的最大可滚动距离
     */
    private int getMaxScrollRangeY() {
        final int visibleHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        return Math.max(0, getContentHeight() - visibleHeight);
    }

    @Override
    protected int computeHorizontalScrollRange() {
        int range = getContentWidth();
        final int scrollX = getScrollX();
        final int visibleWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int overScrollRight = Math.max(0, range - visibleWidth);
        if (scrollX < 0) {
            range -= scrollX;
        } else if (scrollX > overScrollRight) {
            range += scrollX - overScrollRight;
        }
        return Math.max(0, range);
    }

    @Override
    protected int computeVerticalScrollRange() {
        int range = getContentHeight();
        final int scrollY = getScrollY();
        final int visibleHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        final int overScrollBottom = Math.max(0, range - visibleHeight);
        if (scrollY < 0) {
            range -= scrollY;
        } else if (scrollY > overScrollBottom) {
            range += scrollY - overScrollBottom;
        }
        return Math.max(0, range);
    }
}
