package com.apollo.loadbutton;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Apollo on 2017/7/11 9:44
 * 自定义加载按钮
 */

public class LoadingButton extends View implements Animator.AnimatorListener {

    private String mText;
    private float mTextSize;
    private int mStrokeColor;
    private int mContentColor;
    private float mRadius;
    private float mRectWidth;
    private float mContentPaddingLR;
    private float mContentPaddingTB;
    private float mProgressWidth;
    private int mBackgroundColor;
    private int mProgressColor;
    private int mProgressSecond_color;
    private Drawable mLoadSuccessDrawable;
    private Drawable mLoadErrorDrawable;
    private Drawable mLoadPauseDrawable;
    private Paint mPaint;
    private int mDefaultRadius;
    private int mDefaultTextSize;
    private int mDefaultWidth;
    private TextPaint mTextpaint;
    private int mTextColor;
    private RectF mLeftRect;
    private RectF mRightRect;
    private RectF mContentRect;
    private boolean isUnfold;
    private OnClickListener mListner;
    private State mCurrentState;
    private ObjectAnimator mShrinkAnim;
    private LoadListner mLoadLisener;
    private ObjectAnimator mLoadAnimator;
    private boolean progressReverse;
    private int mTextWidth;
    private Path mPath;
    private int left;
    private int top;
    private int right;
    private int bottom;
    private RectF mProgressRect;
    private int mProgressStartAngel;
    private int circleSweep;

    public LoadingButton(Context context) {
        this(context, null);
    }

    public LoadingButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LoadingButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public LoadListner getLoadLisener() {
        return mLoadLisener;
    }

    public void setLoadLisener(LoadListner mLoadLisener) {
        this.mLoadLisener = mLoadLisener;
    }

    public int getCircleSweep() {
        return circleSweep;
    }

    public void setCircleSweep(int circleSweep) {
        this.circleSweep = circleSweep;
    }

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        isUnfold = false;
        load();
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();
    }

    enum State {
        INITIAL,
        FODDING,
        LOADDING,
        COMPLETED_ERROR,
        COMPLETED_SUCCESS,
        COMPLETED_PAUSE
    }

    private void init(Context context, AttributeSet attrs) {
        mDefaultRadius = 40;
        mDefaultTextSize = 24;
        mDefaultWidth = 200;
        //初始化属性
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingButton);
        mText = typedArray.getString(R.styleable.LoadingButton_android_text);
        mTextSize = typedArray.getDimension(R.styleable.LoadingButton_android_textSize, 18);
        mTextColor = typedArray.getColor(R.styleable.LoadingButton_textColor, Color.WHITE);
        mStrokeColor = typedArray.getColor(R.styleable.LoadingButton_strokeColor, 0xffffff);
        mContentColor = typedArray.getColor(R.styleable.LoadingButton_contentColor, 0xffffff);
        mRadius = typedArray.getDimension(R.styleable.LoadingButton_radius, 20);
        mRectWidth = typedArray.getDimension(R.styleable.LoadingButton_rectWidth, 20);
        mContentPaddingLR = typedArray.getDimension(R.styleable.LoadingButton_contentPaddingLR, 10);
        mContentPaddingTB = typedArray.getDimension(R.styleable.LoadingButton_contentPaddingTB, 10);
        mProgressWidth = typedArray.getDimension(R.styleable.LoadingButton_progressedWidth, 2);
        mBackgroundColor = typedArray.getColor(R.styleable.LoadingButton_backgroundColor, Color.WHITE);
        mProgressColor = typedArray.getColor(R.styleable.LoadingButton_progressColor, Color.WHITE);
        mProgressSecond_color = typedArray.getColor(R.styleable.LoadingButton_progressSecond_color, Color.parseColor("#c3c3c3"));
        mLoadSuccessDrawable = typedArray.getDrawable(R.styleable.LoadingButton_loadSuccessDrawable);
        mLoadErrorDrawable = typedArray.getDrawable(R.styleable.LoadingButton_loadErrorDrawable);
        mLoadPauseDrawable = typedArray.getDrawable(R.styleable.LoadingButton_loadPauseDrawable);
        typedArray.recycle();

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mProgressWidth);

        //初始化文字画笔
        mTextpaint = new TextPaint();
        mTextpaint.setAntiAlias(true);
        mTextpaint.setTextSize(mTextSize);
        mTextpaint.setColor(mTextColor);
        mTextpaint.setTextAlign(Paint.Align.CENTER);

        //中间矩形宽度
        mRectWidth = mDefaultWidth - mDefaultRadius * 2;

        //左侧半圆
        mLeftRect = new RectF();
        //右侧半圆
        mRightRect = new RectF();
        //中间矩形
        mContentRect = new RectF();

        isUnfold = true;

        mListner = new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mCurrentState == State.FODDING) {
                    return;
                }
                if (mCurrentState == State.INITIAL) {
                    if (isUnfold) {
                        shrink();
                    }
                } else if (mCurrentState == State.COMPLETED_ERROR) {
                    if (mLoadLisener != null) {
                        mLoadLisener.onClick(false);
                    }
                } else if (mCurrentState == State.COMPLETED_SUCCESS) {
                    if (mLoadLisener != null) {
                        mLoadLisener.onClick(true);
                    }
                } else if (mCurrentState == State.COMPLETED_PAUSE) {
                    if (mLoadLisener != null) {
                        mLoadLisener.needLoading();
                        load();
                    }
                } else if (mCurrentState == State.LOADDING) {
                    mCurrentState = State.COMPLETED_PAUSE;
                    cancelAnimation();
                    invalidateSelf();
                }
            }
        };

        setOnClickListener(mListner);
        mCurrentState = State.INITIAL;

        if (mLoadSuccessDrawable == null) {
            mLoadSuccessDrawable = context.getResources().getDrawable(R.drawable.yes);
        }
        if (mLoadErrorDrawable == null) {
            mLoadErrorDrawable = context.getResources().getDrawable(R.drawable.no
            );
        }
        if (mLoadPauseDrawable == null) {
            mLoadPauseDrawable = context.getResources().getDrawable(R.drawable.pause);
        }

        mProgressSecond_color = Color.parseColor("#c3c3c3");
        mProgressColor = Color.WHITE;

    }

    public void shrink() {
        if (mShrinkAnim == null) {
            mShrinkAnim = ObjectAnimator.ofFloat(this, "mRectWidth", mRectWidth, 0);
        }
        mShrinkAnim.addListener(this);
        mShrinkAnim.setDuration(500);
        mShrinkAnim.start();
        mCurrentState = State.FODDING;
    }

    public void load() {
        if (mLoadAnimator == null) {
            mLoadAnimator = ObjectAnimator.ofFloat(this, "circleSweep", 0, 360);
        }
        mLoadAnimator.setDuration(1000);
        mLoadAnimator.setRepeatMode(ValueAnimator.RESTART);
        mLoadAnimator.setRepeatCount(ValueAnimator.INFINITE);

        mLoadAnimator.removeAllListeners();
        mLoadAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                progressReverse = !progressReverse;
            }
        });
        mLoadAnimator.start();
        mCurrentState = State.LOADDING;

    }

    public void cancelAnimation() {
        if (mShrinkAnim != null && mShrinkAnim.isRunning()) {
            mShrinkAnim.removeAllListeners();
            mShrinkAnim.cancel();
            mShrinkAnim = null;
        }
        if (mLoadAnimator != null && mLoadAnimator.isRunning()) {
            mLoadAnimator.removeAllListeners();
            mLoadAnimator.cancel();
            mLoadAnimator = null;
        }
    }

    public void invalidateSelf() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void loadSucceed() {
        mCurrentState = State.COMPLETED_SUCCESS;
        cancelAnimation();
        invalidateSelf();
    }

    public void loadFailed() {
        mCurrentState = State.COMPLETED_ERROR;
        cancelAnimation();
        invalidateSelf();
    }

    public void reset() {
        mCurrentState = State.INITIAL;
        mRectWidth = getWidth() - mRadius * 2;
        isUnfold = true;
        cancelAnimation();
        invalidateSelf();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取控件宽高和模式，当一个 View 的 layout_width 或者 layout_height 的取值为 wrap_content 时，它的测量模式就是 MeasureSpec.AT_MOST
        //当一个 View 的 layout_width 或者 layout_height 的取值为 match_parent 或 30dp 这样具体的数值时，这就表明它的测量模式是 MeasureSpec.EXACTLY
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //最终尺寸
        int resultW = widthSize;
        int resultH = heightSize;

        int contentW = 0;
        int contentH = 0;
        if (widthMode == MeasureSpec.AT_MOST) {
            mTextWidth = (int) mTextpaint.measureText(mText);
            contentW += mTextWidth + mContentPaddingLR * 2 + mRadius * 2;
            resultW = contentW < widthSize ? contentW : widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            contentH += mContentPaddingTB * 2 + mTextSize;
            resultH = contentH < heightSize ? contentH : heightSize;
        }

        resultW = resultW < mRadius * 2 ? (int) (mRadius * 2) : resultW;
        resultH = resultH < mRadius * 2 ? (int) (mRadius * 2) : resultH;

        mRadius = resultH / 2;
        mRectWidth = resultW - mRadius * 2;
        setMeasuredDimension(resultW, resultH);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取中心点
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        //画图形
        drawPath(canvas, cx, cy);

        int textDescent = (int) mTextpaint.getFontMetrics().descent;
        int textAscent = (int) mTextpaint.getFontMetrics().ascent;
        int delta = Math.abs(textAscent) - textDescent;

        int circleR = (int) (mRadius / 2);
        //画文字
        if (mCurrentState == State.INITIAL) {
            canvas.drawText(mText, cx, cy + delta / 2, mTextpaint);
        } else if (mCurrentState == State.LOADDING) {//画进度圆圈
            if (mProgressRect == null) {
                mProgressRect = new RectF();
            }
            mProgressRect.set(cx - circleR, cy - circleR, cx + circleR, cy + circleR);
            mPaint.setColor(mProgressSecond_color);
            canvas.drawCircle(cx, cy, circleR, mPaint);
            mPaint.setColor(mProgressColor);
            if (circleSweep != 360) {
                mProgressStartAngel = progressReverse ? 270 : 270 + circleSweep;
                canvas.drawArc(mProgressRect, mProgressStartAngel, progressReverse ? 270 : 360 - circleSweep, false, mPaint);
            }
            mPaint.setColor(mBackgroundColor);

        } else if (mCurrentState == State.COMPLETED_ERROR) {
            mLoadErrorDrawable.setBounds(cx - circleR, cy - circleR, cx + circleR, cy + circleR);
            mLoadErrorDrawable.draw(canvas);
        } else if (mCurrentState == State.COMPLETED_PAUSE) {
            mLoadPauseDrawable.setBounds(cx - circleR, cy - circleR, cx + circleR, cy + circleR);
            mLoadPauseDrawable.draw(canvas);
        } else if (mCurrentState == State.COMPLETED_SUCCESS) {
            mLoadSuccessDrawable.setBounds(cx - circleR, cy - circleR, cx + circleR, cy + circleR);
            mLoadSuccessDrawable.draw(canvas);
        }


    }

    private void drawPath(Canvas canvas, int cx, int cy) {
        if (mPath == null) {
            mPath = new Path();
        }
        mPath.reset();

        left = (int) (cx - mRectWidth / 2 - mRadius);
        top = 0;
        right = (int) (cx + mRectWidth / 2 + mRadius);
        bottom = getHeight();

        mLeftRect.set(left, top, left + mRadius * 2, bottom);
        mRightRect.set(right - mRadius * 2, top, right, bottom);
        mContentRect.set(cx - mRectWidth / 2, top, cx + mRectWidth / 2, bottom);

        mPath.moveTo(cx - mRectWidth / 2, bottom);//左下
        mPath.arcTo(mLeftRect, 90f, 180f);//左上
        mPath.lineTo(cx + mRectWidth / 2, top);//右上
        mPath.arcTo(mRightRect, 270f, 180f);//右下
        mPath.close();//闭合

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(mPath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mStrokeColor);

    }

    public interface LoadListner {
        void onClick(boolean isSuccess);

        void needLoading();
    }
}
