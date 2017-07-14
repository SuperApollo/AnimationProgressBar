package com.apollo.animationprogressba;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * Created by Apollo on 2017/7/13 13:54
 */

public class AnimationProgressBar extends View {

    //背景色
    private int backgroundColor;
    //进度条色
    private int barColor;

    private Drawable drawable;
    private int halfDrawableWidth;
    private int halfDrawableHeight;
    private int drawableHeightOffset;
    private boolean isRound;
    private int roundX;
    private int roundY;
    private int progress;
    private int max;
    private boolean isSetBar;
    private int progressHeight;
    private int progressHeightOffset;
    private int refreshTime;
    private int animMode;
    private int rotateRate;
    private int rotateDegree;
    private float scaleMax = 1.5f;
    private float scaleMin;
    private float scaleRate;
    private int gradientStartColor;
    private int gradientEndColor;
    private boolean isGradient;
    private Paint paintBackground;
    private Paint paintBar;
    private Paint paintPicture;
    private int progressWidth;
    private LinearGradient linearGradient;
    private float progressPercentage;
    private int x;
    private int y;
    private RectF rectFBG = new RectF(), rectFPB = new RectF();
    private boolean isAnimRun = true;
    private final String TAG = getClass().getSimpleName();
    private int[] drawableIds;
    private int frameIndex = 0;
    private float scaleLevel = 1;
    private boolean isScaleIncrease = true;
    private OnProgressChangedListener onProgressChangedListener;

    public AnimationProgressBar(Context context) {
        this(context, null);
    }

    public AnimationProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取属性
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AnimationProgressBar, 0, 0);
        backgroundColor = typedArray.getColor(R.styleable.AnimationProgressBar_backgroundColor, Color.GRAY);
        barColor = typedArray.getColor(R.styleable.AnimationProgressBar_barColor, Color.RED);
        drawable = typedArray.getDrawable(R.styleable.AnimationProgressBar_drawable);
        halfDrawableWidth = typedArray.getDimensionPixelSize(R.styleable.AnimationProgressBar_halfDrawableWidth, 35);
        halfDrawableHeight = typedArray.getDimensionPixelSize(R.styleable.AnimationProgressBar_halfDrawableHeight, 35);
        drawableHeightOffset = typedArray.getDimensionPixelSize(R.styleable.AnimationProgressBar_drawableHeightOffset, 0);
        isRound = typedArray.getBoolean(R.styleable.AnimationProgressBar_isRound, true);
        roundX = typedArray.getDimensionPixelSize(R.styleable.AnimationProgressBar_roundX, 20);
        roundY = typedArray.getDimensionPixelSize(R.styleable.AnimationProgressBar_roundY, 20);
        progress = typedArray.getInt(R.styleable.AnimationProgressBar_progress, 0);
        max = typedArray.getInt(R.styleable.AnimationProgressBar_max, 100);
        isSetBar = typedArray.getBoolean(R.styleable.AnimationProgressBar_isSetBar, false);
        progressHeight = typedArray.getDimensionPixelSize(R.styleable.AnimationProgressBar_progressHeight, 30);
        progressWidth = typedArray.getDimensionPixelSize(R.styleable.AnimationProgressBar_progressHeight, 100);
        progressHeightOffset = typedArray.getDimensionPixelSize(R.styleable.AnimationProgressBar_progressHeightOffset, 0);
        refreshTime = typedArray.getInt(R.styleable.AnimationProgressBar_refreshTime, 100);
        animMode = typedArray.getInt(R.styleable.AnimationProgressBar_animMode, AnimMode.ANIM_MODE_NULL.getTypeCode());
        rotateRate = typedArray.getInt(R.styleable.AnimationProgressBar_rotateRate, 10);
        rotateDegree = typedArray.getInt(R.styleable.AnimationProgressBar_rotateDegree, 0);
        scaleMax = typedArray.getFloat(R.styleable.AnimationProgressBar_scaleMax, 2);
        scaleMin = typedArray.getFloat(R.styleable.AnimationProgressBar_scaleMin, 1);
        scaleRate = typedArray.getFloat(R.styleable.AnimationProgressBar_scaleRate, 0.1f);
        gradientStartColor = typedArray.getColor(R.styleable.AnimationProgressBar_gradientStartColor, Color.RED);
        gradientEndColor = typedArray.getColor(R.styleable.AnimationProgressBar_gradientEndColor, Color.YELLOW);
        isGradient = typedArray.getBoolean(R.styleable.AnimationProgressBar_isGradient, false);
        typedArray.recycle();

        init();
    }

    //初始化
    private void init() {
        //背景画笔
        paintBackground = new Paint();
        paintBackground.setColor(backgroundColor);
        //打开抗锯齿
        paintBackground.setAntiAlias(true);
        //bar画笔
        paintBar = new Paint();
        paintBar.setColor(barColor);
        paintBar.setAntiAlias(true);
        //图片画笔
        paintPicture = new Paint();
        paintPicture.setAntiAlias(true);

        if (isGradient) {//需要渐变
            //在predraw时获取view属性，因为在初始化的时候view还没有进行measure
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    //线性渐变
                    linearGradient = new LinearGradient(0, progressHeight / 2, progressWidth, progressHeight / 2, gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP);
                    paintBar.setShader(linearGradient);
                    return false;
                }
            });

        }


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            //计算wrapcontent时的宽
            width = halfDrawableWidth * 2;
        }
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            //计算wrapcontent时的高
            height = halfDrawableHeight * 2;
        }

        progressWidth = width;
        if (!isSetBar) {
            //如果不是自定义高度，则直接把高度设为进度条地高度
            progressHeight = height;
        }
        if (drawable != null) {
            //为图片预留出空间
            progressWidth = width - halfDrawableWidth;
        }

        //设置最终的宽高
        setMeasuredDimension(width, height);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //获取当前进度中心点坐标
        x = (int) (((progressWidth - halfDrawableWidth) * progressPercentage + halfDrawableWidth) / 2);
        y = getHeight() / 2;

        //画进度条
        drawBar(canvas);
        //画动画图片
        drawAnimPicture(canvas);
        //回调draw过程
        postInvalidateDelayed(refreshTime);

    }

    private void drawAnimPicture(Canvas canvas) {

        if (isAnimRun) {
            if (animMode == AnimMode.ANIM_MODE_NULL.getTypeCode()) {
                drawPicture(canvas);
            } else if (animMode == AnimMode.ANIM_MODE_FRAME.getTypeCode()) {
                drawable = getResources().getDrawable(drawableIds[frameIndex]);
                drawPicture(canvas);
                if (frameIndex > drawableIds.length - 1) {
                    frameIndex = 0;
                } else {
                    frameIndex++;
                }

            } else if (animMode == AnimMode.ANIM_MODE_ROTATE.getTypeCode()) {
                rotateCanvas(canvas);
                drawPicture(canvas);
            } else if (animMode == AnimMode.ANIM_MODE_SCALE.getTypeCode()) {
                scaleCanvas(canvas);
                drawPicture(canvas);
            } else if (animMode == AnimMode.ANIM_MODE_ROTATE_SCALE.getTypeCode()) {
                rotateCanvas(canvas);
                scaleCanvas(canvas);
                drawPicture(canvas);
            }
        }

    }

    //缩放画布
    private void scaleCanvas(Canvas canvas) {
        if (scaleLevel >= scaleMax) {
            isScaleIncrease = false;
        }
        if (isScaleIncrease) {
            scaleLevel += scaleRate;
        } else {
            scaleLevel -= scaleRate;
        }

        canvas.scale(scaleLevel, scaleLevel, x, y + drawableHeightOffset);
    }

    //旋转画布
    private void rotateCanvas(Canvas canvas) {
        canvas.rotate(rotateDegree % 360, x, y + drawableHeightOffset);
        rotateDegree += rotateRate;
    }

    //画图片
    private void drawPicture(Canvas canvas) {
        if (drawable == null) {
            Log.e(TAG, "drawable is null");
            return;
        }
        drawable.setBounds(x - halfDrawableWidth,
                getHeight() / 2 - halfDrawableHeight + drawableHeightOffset,
                x + halfDrawableWidth,
                getHeight() / 2 + halfDrawableHeight + drawableHeightOffset);
        drawable.draw(canvas);


    }

    private void drawBar(Canvas canvas) {
        if (isRound) {
            rectFBG.set(0, y - progressHeight / 2 + progressHeightOffset, progressWidth, y + progressHeight / 2 + progressHeightOffset);
            canvas.drawRoundRect(rectFBG, roundX, roundY, paintBackground);
            rectFPB.set(0, y - progressHeight / 2 + progressHeightOffset, x, y + progressHeight / 2 + progressHeightOffset);
            canvas.drawRoundRect(rectFPB, roundX, roundY, paintBar);

        } else {
            rectFBG.set(0, 0, getWidth(), getHeight());
            canvas.drawRect(rectFBG, paintBackground);
            canvas.drawRect(0, 0, x, getHeight(), paintBar);
        }

    }

    //动画模式
    public enum AnimMode {
        ANIM_MODE_NULL(0),
        ANIM_MODE_ROTATE(1),
        ANIM_MODE_SCALE(2),
        ANIM_MODE_ROTATE_SCALE(3),
        ANIM_MODE_FRAME(4);
        private int typeCode;

        AnimMode(int typeCode) {
            this.typeCode = typeCode;
        }

        public int getTypeCode() {
            return typeCode;
        }
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getBarColor() {
        return barColor;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public int getHalfDrawableWidth() {
        return halfDrawableWidth;
    }

    public void setHalfDrawableWidth(int halfDrawableWidth) {
        this.halfDrawableWidth = halfDrawableWidth;
    }

    public int getHalfDrawableHeight() {
        return halfDrawableHeight;
    }

    public void setHalfDrawableHeight(int halfDrawableHeight) {
        this.halfDrawableHeight = halfDrawableHeight;
    }

    public int getDrawableHeightOffset() {
        return drawableHeightOffset;
    }

    public void setDrawableHeightOffset(int drawableHeightOffset) {
        this.drawableHeightOffset = drawableHeightOffset;
    }

    public boolean isRound() {
        return isRound;
    }

    public void setRound(boolean round) {
        isRound = round;
    }

    public int getRoundX() {
        return roundX;
    }

    public void setRoundX(int roundX) {
        this.roundX = roundX;
    }

    public int getRoundY() {
        return roundY;
    }

    public void setRoundY(int roundY) {
        this.roundY = roundY;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress <= max) {
            this.progress = progress;
        } else if (progress < 0) {
            this.progress = 0;
        } else {
            this.progress = max;
        }
        progressPercentage = progress / max;
        doProgressRefrsh();


    }

    //更新进度
    private synchronized void doProgressRefrsh() {
        if (onProgressChangedListener != null) {
            onProgressChangedListener.onProgressChange(progress);
            if (progress >= max) {
                onProgressChangedListener.onProgressFinish();
            }
        }
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isSetBar() {
        return isSetBar;
    }

    public void setSetBar(boolean setBar) {
        isSetBar = setBar;
    }

    public int getProgressHeight() {
        return progressHeight;
    }

    public void setProgressHeight(int progressHeight) {
        this.progressHeight = progressHeight;
    }

    public int getProgressHeightOffset() {
        return progressHeightOffset;
    }

    public void setProgressHeightOffset(int progressHeightOffset) {
        this.progressHeightOffset = progressHeightOffset;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public int getAnimMode() {
        return animMode;
    }

    public void setAnimMode(int animMode) {
        this.animMode = animMode;
    }

    public int getRotateRate() {
        return rotateRate;
    }

    public void setRotateRate(int rotateRate) {
        this.rotateRate = rotateRate;
    }

    public int getRotateDegree() {
        return rotateDegree;
    }

    public void setRotateDegree(int rotateDegree) {
        this.rotateDegree = rotateDegree;
    }

    public float getScaleMax() {
        return scaleMax;
    }

    public void setScaleMax(float scaleMax) {
        this.scaleMax = scaleMax;
    }

    public float getScaleMin() {
        return scaleMin;
    }

    public void setScaleMin(float scaleMin) {
        this.scaleMin = scaleMin;
    }

    public float getScaleRate() {
        return scaleRate;
    }

    public void setScaleRate(float scaleRate) {
        this.scaleRate = scaleRate;
    }

    public int getGradientStartColor() {
        return gradientStartColor;
    }

    public void setGradientStartColor(int gradientStartColor) {
        this.gradientStartColor = gradientStartColor;
    }

    public int getGradientEndColor() {
        return gradientEndColor;
    }

    public void setGradientEndColor(int gradientEndColor) {
        this.gradientEndColor = gradientEndColor;
    }

    public boolean isGradient() {
        return isGradient;
    }

    public void setGradient(boolean gradient) {
        isGradient = gradient;
    }

    public Paint getPaintBackground() {
        return paintBackground;
    }

    public void setPaintBackground(Paint paintBackground) {
        this.paintBackground = paintBackground;
    }

    public Paint getPaintBar() {
        return paintBar;
    }

    public void setPaintBar(Paint paintBar) {
        this.paintBar = paintBar;
    }

    public Paint getPaintPicture() {
        return paintPicture;
    }

    public void setPaintPicture(Paint paintPicture) {
        this.paintPicture = paintPicture;
    }

    public int getProgressWidth() {
        return progressWidth;
    }

    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
    }

    public LinearGradient getLinearGradient() {
        return linearGradient;
    }

    public void setLinearGradient(LinearGradient linearGradient) {
        this.linearGradient = linearGradient;
    }

    public float getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(float progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    @Override
    public float getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public RectF getRectFBG() {
        return rectFBG;
    }

    public void setRectFBG(RectF rectFBG) {
        this.rectFBG = rectFBG;
    }

    public RectF getRectFPB() {
        return rectFPB;
    }

    public void setRectFPB(RectF rectFPB) {
        this.rectFPB = rectFPB;
    }

    public boolean isAnimRun() {
        return isAnimRun;
    }

    public void setAnimRun(boolean animRun) {
        isAnimRun = animRun;
    }

    public String getTAG() {
        return TAG;
    }

    public int[] getDrawableIds() {
        return drawableIds;
    }

    public void setDrawableIds(int[] drawableIds) {
        this.drawableIds = drawableIds;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public float getScaleLevel() {
        return scaleLevel;
    }

    public void setScaleLevel(float scaleLevel) {
        this.scaleLevel = scaleLevel;
    }

    public boolean isScaleIncrease() {
        return isScaleIncrease;
    }

    public void setScaleIncrease(boolean scaleIncrease) {
        isScaleIncrease = scaleIncrease;
    }

    public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
        this.onProgressChangedListener = onProgressChangedListener;
    }

    //进度监听器
    public interface OnProgressChangedListener {
        void onProgressChange(int progress);

        void onProgressFinish();
    }

}
