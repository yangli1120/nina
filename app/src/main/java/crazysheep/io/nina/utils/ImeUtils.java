package crazysheep.io.nina.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * input method utils
 *
 * Created by crazysheep on 16/2/18.
 */
public class ImeUtils {

    private static InputMethodManager getImeMgr(@NonNull Context context) {
        return (InputMethodManager)context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    /**
     * show input method for target view
     * */
    public static void show(@NonNull View view) {
        getImeMgr(view.getContext()).showSoftInput(view, 0);
    }

    /**
     * hide current activity's input method
     * */
    public static void hide(@NonNull Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            getImeMgr(activity).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
