package crazysheep.io.nina.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

/**
 * network utils
 *
 * Created by crazysheep on 16/1/26.
 */
public class NetworkUtils {

    /**
     * is network connected ?
     * */
    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiInfo = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo.getState() == NetworkInfo.State.CONNECTED
                || mobileInfo.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

}
