package crazysheep.io.nina.widget.swiperefresh;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.TypedValue;

/**
 * inner util for swipe refresh widget
 *
 * Created by crazysheep on 16/1/27.
 */
class SwipeRefreshUtils {

    /**
     * transform dp to px
     * */
    public static int dp2Px(@NonNull Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                r.getDisplayMetrics()));
    }
}
