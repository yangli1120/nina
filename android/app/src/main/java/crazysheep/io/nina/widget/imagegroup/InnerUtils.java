package crazysheep.io.nina.widget.imagegroup;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.TypedValue;

/**
 * inner utils for imagegroup widget, not use out side of its package
 *
 * Created by crazysheep on 16/1/28.
 */
class InnerUtils {

    /**
     * transform dp to px
     * */
    public static int dp2Px(@NonNull Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                r.getDisplayMetrics()));
    }

}
