package crazysheep.io.nina.dagger2.component;

import com.squareup.leakcanary.RefWatcher;

import crazysheep.io.nina.dagger2.scope.PerFragment;
import crazysheep.io.nina.fragment.BaseFragment;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.prefs.SettingPrefs;
import crazysheep.io.nina.prefs.UserPrefs;
import dagger.Component;

/**
 * component for {@link crazysheep.io.nina.fragment.BaseFragment}
 *
 * Created by crazysheep on 16/4/22.
 */
@PerFragment
@Component(dependencies = ApplicationComponent.class)
public interface FragmentComponent {

    void inject(BaseFragment fragment);

    RefWatcher getRefWatcher();
    HttpClient getHttpClient();
    UserPrefs getUserPrefs();
    SettingPrefs getSettingPrefs();
}
