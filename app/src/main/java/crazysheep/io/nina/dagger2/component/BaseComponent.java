package crazysheep.io.nina.dagger2.component;

import crazysheep.io.nina.BaseActivity;
import crazysheep.io.nina.dagger2.module.NetworkModule;
import crazysheep.io.nina.dagger2.module.PrefsModule;
import crazysheep.io.nina.dagger2.scope.DaggerActivity;
import crazysheep.io.nina.fragment.BaseFragment;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.prefs.SettingPrefs;
import crazysheep.io.nina.prefs.UserPrefs;
import dagger.Component;

/**
 * base component
 *
 * Created by crazysheep on 16/3/11.
 */
@DaggerActivity
@Component(
        dependencies = ApplicationComponent.class,
        modules = {
                NetworkModule.class, PrefsModule.class
        }
)
public interface BaseComponent {

    void inject(BaseActivity activity);
    void inject(BaseFragment fragment);

    HttpClient httpClient();
    UserPrefs userPrefs();
    SettingPrefs settingPrefs();
}
