package crazysheep.io.nina.dagger2.module;

import android.app.Application;

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

    public PrefsModule(Application application) {
        mUserPrefs = new UserPrefs(application);
        mSettingPrefs = new SettingPrefs(application);
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
