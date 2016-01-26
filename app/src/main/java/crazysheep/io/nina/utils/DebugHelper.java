package crazysheep.io.nina.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import crazysheep.io.nina.BuildConfig;

/**
 * helper for debug
 *
 * Created by crazysheep on 16/1/23.
 */
public class DebugHelper {

    /**
     * show a toast
     * */
    public static void toast(@NonNull Context context, @NonNull String msg) {
        if(BuildConfig.DEBUG)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * normal log
     * */
    public static void log(@NonNull String msg) {
        if(BuildConfig.DEBUG)
            android.util.Log.d(DebugHelper.class.getSimpleName(), msg);
    }

}
