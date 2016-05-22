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

    @Provides
    @Singleton
    public UserPrefs provideUserPrefs(@NonNull Context context) {
        return new UserPrefs(context);
    }

    @Provides
    @Singleton
    public SettingPrefs provideSettingPrefs(@NonNull Context context) {
        return new SettingPrefs(context);
    }

}
