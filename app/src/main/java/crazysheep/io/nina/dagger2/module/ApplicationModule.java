package crazysheep.io.nina.dagger2.module;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import crazysheep.io.nina.application.BaseApplication;
import dagger.Module;
import dagger.Provides;

/**
 * application module
 *
 * Created by crazysheep on 16/3/9.
 */
@Module
public class ApplicationModule {

    private BaseApplication mApplication;

    public ApplicationModule(@NonNull BaseApplication application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    public BaseApplication provideApplication() {
        return mApplication;
    }

}
