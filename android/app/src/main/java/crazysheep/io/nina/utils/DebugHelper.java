package crazysheep.io.nina.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import crazysheep.io.nina.BuildConfig;

/**
 * helper for debug
 *
 * Created by crazysheep on 16/1/23.
 */
public class DebugHelper {

    public static final String TAG = DebugHelper.class.getSimpleName();

    /**
     * show a toast
     * */
    public static void toast(@NonNull Context context, @NonNull String msg) {
        if(BuildConfig.DEBUG)
            ToastUtils.t(context, msg);
    }

    /**
     * normal log
     * */
    public static void log(@NonNull String msg) {
        if(BuildConfig.DEBUG)
            android.util.Log.i(TAG, msg);
    }

}
