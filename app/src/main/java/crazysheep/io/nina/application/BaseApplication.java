package crazysheep.io.nina.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.orhanobut.logger.Logger;

import crazysheep.io.nina.net_new.TwitterClient;
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
        Fabric.with(this, new Crashlytics(), TwitterClient.getInstance().getTwitterCore());
        // init stetho
        Stetho.initializeWithDefaults(this);
    }

    public static Application getAppContext() {
        return mContext;
    }

}
