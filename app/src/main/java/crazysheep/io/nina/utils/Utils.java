package crazysheep.io.nina.utils;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import java.util.UUID;

/**
 * common utils
 *
 * Created by crazysheep on 16/1/22.
 */
public class Utils {

    public static boolean isNull(Object obj) {
        return null == obj;
    }

    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    /**
     * change dp to px
     * {@Link http://stackoverflow.com/questions/4605527/converting-pixels-to-dp}
     * */
    public static float dp2px(@NonNull Resources res, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                res.getDisplayMetrics());
    }
}
