package crazysheep.io.nina.application;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

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
public class BaseApplication extends com.activeandroid.app.Application {

    private static BaseApplication mContext;

    public static final String TAG = "nina";

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);

        // init logger
        Logger.init(TAG);
        // init twitter sdk
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                HttpConstants.NINA_CONSUMER_KEY,
                HttpConstants.NINA_CONSUMER_SECRET);
        TwitterCore twitterCore = new TwitterCore(authConfig);
        Fabric.with(this, new Crashlytics(), twitterCore);
        // init stetho
        Stetho.initializeWithDefaults(this);
    }

    public static Application getAppContext() {
        return mContext;
    }

}
