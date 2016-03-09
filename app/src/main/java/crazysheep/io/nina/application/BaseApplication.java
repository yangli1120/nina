package crazysheep.io.nina.application;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.orhanobut.logger.Logger;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import crazysheep.io.nina.dagger2.component.BaseComponent;
import crazysheep.io.nina.dagger2.component.DaggerBaseComponent;
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

    private static BaseApplication mContext;

    public static final String TAG = "nina";

    private BaseComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        mAppComponent = DaggerBaseComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .prefsModule(new PrefsModule(this))
                .build();

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

    public BaseComponent getComponent() {
        return mAppComponent;
    }

    public static BaseApplication from(@NonNull Context context) {
        return (BaseApplication) context.getApplicationContext();
    }

}
