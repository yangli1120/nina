package crazysheep.io.nina.dagger2.module;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.dagger2.scope.DaggerActivity;
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

    @Provides
    @DaggerActivity
    public UserPrefs provideUserPrefs(BaseApplication context) {
        return new UserPrefs(context);
    }

    @Provides
    @DaggerActivity
    public SettingPrefs provideSettingPrefs(BaseApplication context) {
        return new SettingPrefs(context);
    }

}
