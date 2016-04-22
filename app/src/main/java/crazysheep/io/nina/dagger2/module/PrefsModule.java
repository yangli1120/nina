package crazysheep.io.nina.dagger2.module;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import crazysheep.io.nina.prefs.SettingPrefs;
import crazysheep.io.nina.prefs.UserPrefs;
import dagger.Module;
import dagger.Provides;

/**
 * SharedPreferences module
 *
 * Created by crazysheep on 16/3/10.
 */
@Module
public class PrefsModule {

    private UserPrefs mUserPrefs;
    private SettingPrefs mSettingPrefs;

    public PrefsModule(@NonNull Context context) {
        mUserPrefs = new UserPrefs(context);
        mSettingPrefs = new SettingPrefs(context);
    }

    @Provides
    @Singleton
    public UserPrefs provideUserPrefs() {
        return mUserPrefs;
    }

    @Provides
    @Singleton
    public SettingPrefs provideSettingPrefs() {
        return mSettingPrefs;
    }

}
