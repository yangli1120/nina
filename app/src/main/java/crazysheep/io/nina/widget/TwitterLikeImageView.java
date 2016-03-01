package crazysheep.io.nina.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

/**
 * implement twitter like animation button
 *
 * Created by crazysheep on 16/3/1.
 */
public class TwitterLikeImageView extends ImageView {

    private int resRedHeart= 0;
    private int resGreyHeart = 0;

    public TwitterLikeImageView(Context context) {
        super(context);
    }

    public TwitterLikeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwitterLikeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TwitterLikeImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setHeartRes(@DrawableRes int red, @DrawableRes int grey) {
        this.resRedHeart = red;
        this.resGreyHeart = grey;
    }

    public void like() {
        if(resGreyHeart > 0)
            setImageResource(resGreyHeart);
        tik();
    }

    public void unlike() {
        if(resGreyHeart > 0)
            setImageResource(resGreyHeart);
    }

    private void tik() {
        setScaleX(0.7f);
        setScaleY(0.7f);
        ViewCompat.animate(this)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(200)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        reset();

                        if(resRedHeart > 0)
                            setImageResource(resRedHeart);
                        tok();
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        reset();
                    }
                })
                .start();
    }

    private void tok() {
        setScaleX(1.5f);
        setScaleY(1.5f);
        ViewCompat.animate(this)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(200)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        reset();
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        reset();
                    }
                })
                .start();
    }

    private void reset() {
        ViewCompat.animate(this)
                .setListener(null)
                .cancel();
    }

}
