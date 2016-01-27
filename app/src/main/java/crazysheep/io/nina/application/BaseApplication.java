package crazysheep.io.nina.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.orhanobut.logger.Logger;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import crazysheep.io.nina.net.HttpConstants;
import io.fabric.sdk.android.Fabric;

/**
 * base application
 *
 * Created by crazysheep on 16/1/20.
 */
public class BaseApplication extends Application {

    private static BaseApplication mContext;

    public static final String TAG = "nina";

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        // init logger
        Logger.init(TAG);
        // init twitter sdk
        TwitterAuthConfig authConfig = new TwitterAuthConfig(HttpConstants.NINA_CONSUMER_KEY,
                HttpConstants.NINA_CONSUMER_SECRET);
        Fabric.with(this, new Crashlytics(), new TwitterCore(authConfig));
        // init stetho
        Stetho.initializeWithDefaults(this);
    }

    public static Application getAppContext() {
        return mContext;
    }

}
