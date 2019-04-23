package com.igo.customview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by Edgar on 2019/4/12.
 */
public class DialProgressBar extends View {

    private static final String TAG = "DialProgressBar";
    private static final float DEFAULT_DIVIDER_ANGLE = 2.5F;
    private static final float DEFAULT_START_ANGLE = 130;
    private static final float DEFAULT_SWEEP_ANGLE = 95;
    private static final int DEFAULT_MAX_ARC_NUM = 10;
    private static final DecelerateInterpolator PROGRESS_ANIM_INTERPOLATOR =
            new DecelerateInterpolator();
    private static final PorterDuffXfermode SRC_IN_MODE = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    private static final PorterDuffXfermode CLEAR_MODE = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private static final int PROGRESS_ANIM_DURATION = 300;

    private float mStartAngle;
    private float mSweepAngle;
    private int[] mGradualColors = new int[]{0xFFFF2515, 0xFFFFA62B};
    private int mBgColor = 0xFFD1D1D1;

    private Paint mPaint; //进度条画笔
    private Path mArcOutlinePath; //轮廓路径
    private float mArcOutlineLineWidth; //轮廓的线宽
    private Paint mDividerPaint;
    private SweepGradient mGradient;

    private RectF mOutOval;
    private RectF mMiddleOval;
    private RectF mInnerOval;
    private RectF mRoundOval;

    private int mArcWidth;
    private float mProgress;
    private float mOvalRadius;
    private float mDividerAngle = DEFAULT_DIVIDER_ANGLE;
    private float mDividerWidth;
    private int mMaxArcNum = DEFAULT_MAX_ARC_NUM;
    private int mLevel;

    public DialProgressBar(Context context) {
        this(context, null);
    }

    public DialProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final Resources res = getResources();
        mArcWidth = res.getDimensionPixelOffset(R.dimen.dial_progressbar_arc_stroke_width);
        mArcOutlineLineWidth = res.getDimension(R.dimen.dial_progressbar_outline_line_width);
        mDividerWidth = res.getDimension(R.dimen.dial_progressbar_divider_width);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBgColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mArcOutlinePath = new Path();

        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDividerPaint.setXfermode(CLEAR_MODE);
        mDividerPaint.setStrokeCap(Paint.Cap.BUTT);
        mDividerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mDividerPaint.setStrokeWidth(mDividerWidth);

        mOutOval = new RectF();
        mInnerOval = new RectF();
        mMiddleOval = new RectF();
        mRoundOval = new RectF();
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        setAngle(DEFAULT_START_ANGLE,DEFAULT_SWEEP_ANGLE);
        setLevel(1,false);
    }

    public void setAngle(float startAngle, float sweepAngle) {
        mStartAngle = startAngle;
        mSweepAngle = sweepAngle;
    }

    public void setMaxArcNum(int maxArcNum) {
        mMaxArcNum = maxArcNum;
    }

    public void setLevel(int level, boolean animation) {
        if (level > mMaxArcNum) {
            level = mMaxArcNum;
        }
        if (level < 0) {
            level = 0;
        }
        if (mLevel != level) {
            mLevel = level;
            float progress = mLevel * mMaxArcNum;
            if (animation) {
                refreshProgress(progress);
            } else {
                setProgressInternal(progress);
            }
        }
    }

    private void setProgressInternal(float progress) {
        mProgress = progress;
        postInvalidate();
    }

    private void refreshProgress(float progress) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, VISUAL_PROGRESS, progress);
        animator.setAutoCancel(true);
        animator.setDuration(PROGRESS_ANIM_DURATION);
        animator.setInterpolator(PROGRESS_ANIM_INTERPOLATOR);
        animator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //外圆的矩阵
        float left = mArcOutlineLineWidth / 2.0f;
        float top = mArcOutlineLineWidth / 2.0f;
        mOutOval.set(left, top, w - left, h - top);

        //中心圆的矩阵
        final float middlePadding = mArcWidth / 2f;
        mMiddleOval.set(left + middlePadding, top + middlePadding,
                mOutOval.right - middlePadding,
                mOutOval.bottom - middlePadding);
        //内圆的矩阵
        mInnerOval.set(left + mArcWidth, top + mArcWidth, mOutOval.right - mArcWidth, mOutOval.bottom - mArcWidth);

        mGradient = new SweepGradient(mMiddleOval.centerX(), mMiddleOval.centerY(), mGradualColors, null);
        mOvalRadius = mMiddleOval.width()/2f;
    }

    private void resetPaint() {
        mPaint.setXfermode(null);
        mPaint.setShader(null);
        mPaint.setStrokeWidth(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        resetPaint();
        int saveCount = canvas.saveLayer(mOutOval, mPaint,Canvas.ALL_SAVE_FLAG);
        //绘制灰色背景圆弧
        final float endAngle = mStartAngle + mSweepAngle;
        mArcOutlinePath.reset();
        //从内圆弧的终点角度到起点描线
        mArcOutlinePath.arcTo(mInnerOval,mStartAngle+mSweepAngle,-mSweepAngle);
        //描下面的圆弧
        roundArcTo(mOvalRadius, mStartAngle, mStartAngle + 180);
        //外圆, 从起点角度开始描线到终点
        mArcOutlinePath.arcTo(mOutOval,mStartAngle, mSweepAngle);
        //描下面的圆弧
        roundArcTo(mOvalRadius, endAngle, endAngle);
        mArcOutlinePath.close();
        canvas.drawPath(mArcOutlinePath,mPaint);

        //计算额外的角度,需要追加起始角度,才能看起来两边有圆角
        float value = (mRoundOval.width()/2f)/(mMiddleOval.width()/2f);
        float angle = (float) (value*180/Math.PI);
        final float startFinalAngle = mStartAngle - angle;
        final float finalSweepAngle = mSweepAngle + angle * 2f;
        drawProgress(canvas,startFinalAngle,mProgress/100f*finalSweepAngle);
        drawDivider(canvas,startFinalAngle,finalSweepAngle);
        canvas.restoreToCount(saveCount);
    }

    private void roundArcTo(float radius, final float angle, float startAngle) {
        float radian = (float) (Math.PI / 180.0 * angle);
        float x = (float) (mMiddleOval.centerX() + radius * Math.cos(radian));
        float y = (float) (mMiddleOval.centerY() + radius * Math.sin(radian));
        float left = x - mArcWidth / 2.0f;
        float top = y - mArcWidth / 2.0f;
        mRoundOval.set(left, top, left + mArcWidth, top + mArcWidth);
        mArcOutlinePath.arcTo(mRoundOval,startAngle,180);
    }

    /**
     * 绘制进度条
     * @param canvas 绘制画布
     */
    private void drawProgress(Canvas canvas,float startAngle, float endAngle) {
        mPaint.setShader(mGradient);
        mPaint.setXfermode(SRC_IN_MODE);
        mPaint.setStrokeWidth(mArcWidth);
        canvas.drawArc(mMiddleOval,startAngle,endAngle,false,mPaint);
    }

    private void drawDivider(Canvas canvas, float startAngle, float sweepAngle) {
        final float totalAngle = sweepAngle - ((mMaxArcNum-1) * mDividerAngle);
        final float angle = totalAngle / mMaxArcNum;
        float start = startAngle + angle;
        for (int i=1; i < mMaxArcNum; i++) {
            canvas.drawArc(mMiddleOval,start, mDividerAngle,false, mDividerPaint);
            start = start + mDividerAngle + angle;
        }
    }

    private final FloatProperty<DialProgressBar> VISUAL_PROGRESS = new FloatProperty<DialProgressBar>("visual_progress") {
        @Override
        public Float get(DialProgressBar object) {
            return object.mProgress;
        }

        @Override
        public void setValue(DialProgressBar object, float value) {
            object.mProgress = value;
            postInvalidate();
        }
    };
}