package crazysheep.io.nina.dagger2.component;

import crazysheep.io.nina.BaseActivity;
import crazysheep.io.nina.dagger2.scope.PerActivity;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.prefs.SettingPrefs;
import crazysheep.io.nina.prefs.UserPrefs;
import dagger.Component;

/**
 * base component
 *
 * Created by crazysheep on 16/3/11.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class)
public interface ActivityComponent {

    void inject(BaseActivity activity);

    HttpClient getHttpClient();
    UserPrefs getUserPrefs();
    SettingPrefs getSettingPrefs();
}
