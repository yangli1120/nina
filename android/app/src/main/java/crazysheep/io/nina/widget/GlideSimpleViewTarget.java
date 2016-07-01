package crazysheep.io.nina.widget;

import android.support.annotation.NonNull;
import android.view.View;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

/**
 * custom view target for Glide
 *
 * Created by crazysheep on 16/2/6.
 */
public class GlideSimpleViewTarget<V extends View> extends ViewTarget<V, GlideDrawable> {

    //////////////////////// api /////////////////////////////////

    public static <V extends View> GlideSimpleViewTarget<V> createViewTarget(@NonNull V v) {
        return new GlideSimpleViewTarget<>(v);
    }

    /////////////////////////////////////////////////////////////

    public GlideSimpleViewTarget(V view) {
        super(view);
    }

    @Override
    public void onResourceReady(GlideDrawable resource,
                                GlideAnimation<? super GlideDrawable> glideAnimation) {
        getView().setBackground(resource.getCurrent());
    }
}
