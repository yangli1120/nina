package crazysheep.io.nina.compat;

import android.os.Build;

/**
 * api compat
 *
 * Created by crazysheep on 16/3/3.
 */
public class APICompat {

    /**
     * api 23, equal {@link android.os.Build.VERSION_CODES.M}
     * */
    public static final int M = Build.VERSION_CODES.M;

    /**
     * api 21, equal {@link android.os.Build.VERSION_CODES.LOLLIPOP}
     * */
    public static final int L = Build.VERSION_CODES.LOLLIPOP;
    /**
     * api 19, equal {@link android.os.Build.VERSION_CODES.KITKAT}
     * */
    public static final int K = Build.VERSION_CODES.KITKAT;
    /**
     * api 16, equal {@link android.os.Build.VERSION_CODES.JELLY_BEAN}
     * */
    public static final int J16 = Build.VERSION_CODES.JELLY_BEAN;

    /**
     * is current api beyond android lollipop
     * */
    public static boolean L() {
        return api21();
    }

    /**
     * is current api beyond android Marshmallow
     * */
    public static boolean M() {
        return api23();
    }

    public static boolean api23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean api21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * is current api beyond android kitkat
     * */
    public static boolean K() {
        return api19();
    }

    public static boolean api19() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean api18() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

}
