package crazysheep.io.nina.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.orhanobut.logger.Logger;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import crazysheep.io.nina.constants.Constants;
import io.fabric.sdk.android.Fabric;

/**
 * base application
 *
 * Created by crazysheep on 16/1/20.
 */
public class BaseApplication extends Application {

    public static final String TAG = "nina";

    @Override
    public void onCreate() {
        super.onCreate();

        // init logger
        Logger.init(TAG);
        // init twitter sdk
        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_KEY,
                Constants.TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new TwitterCore(authConfig));
    }
}
