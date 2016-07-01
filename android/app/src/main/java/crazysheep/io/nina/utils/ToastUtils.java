package crazysheep.io.nina.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * toast utils
 *
 * Created by crazysheep on 16/2/18.
 */
public class ToastUtils {

    /**
     * show a toast
     * */
    public static void t(@NonNull Context context, @NonNull String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * show a toast
     * */
    public static void t(@NonNull Context context, @NonNull String msg, int length) {
        Toast.makeText(context, msg, length).show();
    }

}
