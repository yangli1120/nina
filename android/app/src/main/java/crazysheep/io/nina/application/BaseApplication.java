package crazysheep.io.nina.application;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.orhanobut.logger.Logger;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import crazysheep.io.nina.BuildConfig;
import crazysheep.io.nina.dagger2.component.ApplicationComponent;
import crazysheep.io.nina.dagger2.component.DaggerApplicationComponent;
import crazysheep.io.nina.dagger2.module.ApplicationModule;
import crazysheep.io.nina.dagger2.module.PrefsModule;
import crazysheep.io.nina.net.HttpConstants;
import io.fabric.sdk.android.Fabric;

/**
 * base application
 *
 * Created by crazysheep on 16/1/20.
 */
public class BaseApplication extends com.activeandroid.app.Application {

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static BaseApplication from(@NonNull Context context) {
        return (BaseApplication) context.getApplicationContext();
    }

    public static final String TAG = "nina";

    private ApplicationComponent mAppComponent;
    protected static BaseApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        mAppComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        // init TinyDancer
        /*TinyDancer.create()
                .redFlagPercentage(0.1f)
                .startingGravity(Gravity.TOP | Gravity.RIGHT)
                .startingXPosition(200)
                .startingYPosition(600)
                .show(this);*/

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

        if(BuildConfig.DEBUG)
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
    }

    public static Application getAppContext() {
        return mContext;
    }

    public ApplicationComponent getComponent() {
        return mAppComponent;
    }

}
