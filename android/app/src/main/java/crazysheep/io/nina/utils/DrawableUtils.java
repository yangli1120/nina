package crazysheep.io.nina.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * drawable utils
 *
 * Created by crazysheep on 16/5/1.
 */
public class DrawableUtils {

    /**
     * tint source with color, compat api before lollipop(API 21)
     * */
    public static Drawable tint(@NonNull Drawable source, @ColorInt int color) {
        final Drawable tintDrawable = DrawableCompat.wrap(source);
        DrawableCompat.setTint(tintDrawable, color);
        return tintDrawable;
    }
}
