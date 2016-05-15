package crazysheep.io.nina.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * utils for intent, like share, view
 *
 * Created by crazysheep on 16/5/15.
 */
public class IntentUtils {

    /**
     * share action
     * */
    public static void shareText(@NonNull Context context, String text) {
        if(!TextUtils.isEmpty(text))
            return;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        try {
            context.startActivity(sendIntent);
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
        }
    }
}
