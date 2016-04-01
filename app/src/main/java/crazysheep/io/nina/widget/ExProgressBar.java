package crazysheep.io.nina.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import crazysheep.io.nina.utils.Utils;

/**
 * ex progressbar
 *
 * Created by crazysheep on 16/3/30.
 */
public class ExProgressBar extends LinearLayout {

    ////////////////// callback ///////////////////

    public interface OnProgressListener {
        void onStart(int currentProgress);
        void onEnd(int maxProgress);
    }
    ///////////////////////////////////////////////

    public static int MIN_PROGRESS = 0;
    public static int WARNING_PROGRESS = 5;
    public static int MAX_PROGRESS = 30;

    private Paint mWarningPaint;
    private static int WARNING_WIDTH = 1;
    private Paint mStopPositionPaint;
    private static int STOP_POSITION_WIDTH = 1;

    private List<Integer> mStopPositions;
    private Paint mDeletePaint;
    private int mDeletePos = -1;

    private View mProgressBar;
    private View mBlinkV;

    private ValueAnimator mProgressAnimator;

    private OnProgressListener mOnProgressListener;

    public ExProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ExProgressBar(Context context) {
        super(context);
        init();
    }

    public ExProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        int warningPos = Math.round(WARNING_PROGRESS * 1f / MAX_PROGRESS * getWidth());
        canvas.drawRect(warningPos - WARNING_WIDTH / 2, 0, warningPos + WARNING_WIDTH / 2,
                getHeight(), mWarningPaint);

        for(int stopPosition : mStopPositions)
            if(mDeletePos > 0 && stopPosition == mDeletePos) {
                int previousPos = mStopPositions.indexOf(mDeletePos) > 0
                        ? mStopPositions.get(mStopPositions.indexOf(mDeletePos) - 1) : 0;
                canvas.drawRect(previousPos, 0, mDeletePos, getHeight(), mDeletePaint);
            } else {
                canvas.drawRect(stopPosition - STOP_POSITION_WIDTH / 2, 0,
                        stopPosition + WARNING_WIDTH / 2, getHeight(), mStopPositionPaint);
            }
    }

    public void setMaxProgress(int maxProgress) {
        MAX_PROGRESS =  maxProgress > 0 ? maxProgress : 0;
    }

    public void setMinProgress(int minProgress) {
        MIN_PROGRESS = minProgress < 0 ? 0 : minProgress;
    }

    public void setWarningProgress(int warningProgress) {
        WARNING_PROGRESS = warningProgress < 0 ? 0 : warningProgress;
    }

    public void setOnProgressListener(OnProgressListener listener) {
        mOnProgressListener = listener;
    }

    private void init() {
        mStopPositions = new ArrayList<>();
        mDeletePaint = new Paint();
        mDeletePaint.setAntiAlias(true);
        mDeletePaint.setColor(Color.RED);

        mStopPositionPaint = new Paint();
        mStopPositionPaint.setColor(Color.BLACK);
        mStopPositionPaint.setAntiAlias(true);
        STOP_POSITION_WIDTH = Math.round(Utils.dp2px(getResources(), 2));

        mWarningPaint = new Paint();
        mWarningPaint.setColor(Color.WHITE);
        mWarningPaint.setAntiAlias(true);
        WARNING_WIDTH = Math.round(Utils.dp2px(getResources(), 2));

        setOrientation(LinearLayout.HORIZONTAL);

        mProgressBar = new View(getContext().getApplicationContext());
        mProgressBar.setBackgroundColor(Color.WHITE);
        mBlinkV = new View(getContext().getApplicationContext());
        mBlinkV.setBackgroundColor(Color.LTGRAY);

        // add progressbar
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        pbParams.width = 0;
        addView(mProgressBar, pbParams);
        // add blink view
        LinearLayout.LayoutParams blinkParams = new LinearLayout.LayoutParams(
                Math.round(Utils.dp2px(getResources(), 4)), LayoutParams.MATCH_PARENT);
        addView(mBlinkV, blinkParams);

        animateBlink(mBlinkV, 0f, 1f);
    }

    public void start() {
        cancelAnimation(mBlinkV);

        mProgressAnimator = ValueAnimator.ofInt(mProgressBar.getWidth(), getWidth());
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mProgressBar
                        .getLayoutParams();
                params.width = (int)animation.getAnimatedValue();
                mProgressBar.setLayoutParams(params);

                if((int)animation.getAnimatedValue() >= getWidth()
                        && null != mOnProgressListener)
                    mOnProgressListener.onEnd(MAX_PROGRESS);
            }
        });
        mProgressAnimator.setInterpolator(new LinearInterpolator());
        int duration = MAX_PROGRESS
                - Math.round(mProgressBar.getWidth() * 1f / getWidth() * MAX_PROGRESS); // seconds
        mProgressAnimator.setDuration(duration * 1000);
        mProgressAnimator.start();
        if(null != mOnProgressListener)
            mOnProgressListener.onStart(duration);
    }

    public void stop() {
        mProgressAnimator.cancel();

        mStopPositions.add(mProgressBar.getWidth());
        animateBlink(mBlinkV, 0f, 1f);
    }

    public boolean isReachMaxProgress() {
        return mProgressBar.getWidth() >= getWidth();
    }

    public boolean isInvalid() {
        return mStopPositions.size() <= 0
                || mStopPositions.get(mStopPositions.size() - 1) <
                        Math.round(WARNING_PROGRESS * 1f / MAX_PROGRESS * getWidth());
    }

    public boolean isPrepareDelete() {
        return mDeletePos > 0;
    }

    public void prepareDelete() {
        if(mStopPositions.size() > 0) {
            mDeletePos = mStopPositions.get(mStopPositions.size() - 1);
            invalidate();
        }
    }

    public void delete() {
        if(mDeletePos > 0) {
            mStopPositions.remove(mStopPositions.size() - 1);
            mDeletePos = -1;

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mProgressBar
                    .getLayoutParams();
            if(mStopPositions.size() > 0)
                params.width = mStopPositions.get(mStopPositions.size() - 1);
            else
                params.width = 0;
            mProgressBar.setLayoutParams(params);

            invalidate();
        }
    }

    private void animateBlink(View view, final float from, final float to) {
        cancelAnimation(view);

        view.setAlpha(from);
        ViewCompat.animate(view)
                .alpha(to)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        animateBlink(view, to, from);
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }
                })
                .start();
    }

    private void cancelAnimation(View view) {
        ViewCompat.animate(view)
                .setListener(null)
                .cancel();
        view.setAlpha(1f);
    }

}
