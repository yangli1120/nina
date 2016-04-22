package crazysheep.io.nina.dagger2.component;

import com.squareup.leakcanary.RefWatcher;

import javax.inject.Singleton;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.dagger2.module.ApplicationModule;
import crazysheep.io.nina.dagger2.module.NetworkModule;
import crazysheep.io.nina.dagger2.module.PrefsModule;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.prefs.SettingPrefs;
import crazysheep.io.nina.prefs.UserPrefs;
import dagger.Component;

/**
 * application component
 *
 * Created by crazysheep on 16/3/9.
 */
@Singleton
@Component(
        modules = {
                ApplicationModule.class, PrefsModule.class, NetworkModule.class
        }
)
public interface ApplicationComponent {

    // ApplicationModule provide
    BaseApplication getContext();
    RefWatcher getRefWatcher();

    // PrefsModule provide
    UserPrefs getUserPrefs();
    SettingPrefs getSettingPrefs();

    // NetworkModule provide
    HttpClient getHttpClient();

}
