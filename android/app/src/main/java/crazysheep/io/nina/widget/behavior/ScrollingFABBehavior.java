package crazysheep.io.nina.widget.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import crazysheep.io.nina.utils.SystemUIHelper;

/**
 * see{@link https://mzgreen.github.io/2015/06/23/How-to-hideshow-Toolbar-when-list-is-scrolling(part3)/}
 *
 * Created by crazysheep on 16/2/3.
 */
public class ScrollingFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private int toolbarHeight;

    public ScrollingFABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.toolbarHeight = SystemUIHelper.getToolbarSize(context);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        if (dependency instanceof AppBarLayout) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;
            int distanceToScroll = fab.getHeight() + fabBottomMargin;
            float ratio = dependency.getY()/(float)toolbarHeight;
            //fab.setTranslationY(-distanceToScroll * ratio);

            ratio = 1 - Math.abs(ratio) < 0 ? 0 : 1 - Math.abs(ratio);
            ViewCompat.animate(fab)
                    .scaleX(ratio)
                    .scaleY(ratio)
                    .setInterpolator(INTERPOLATOR)
                    .setDuration(150)
                    .start();
        }
        return true;
    }
}
