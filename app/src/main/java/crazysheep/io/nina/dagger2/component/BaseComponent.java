package crazysheep.io.nina.dagger2.component;

import javax.inject.Singleton;

import crazysheep.io.nina.BaseActivity;
import crazysheep.io.nina.dagger2.module.ApplicationModule;
import crazysheep.io.nina.dagger2.module.PrefsModule;
import crazysheep.io.nina.fragment.BaseFragment;
import dagger.Component;

/**
 * application component
 *
 * Created by crazysheep on 16/3/9.
 */
@Singleton
@Component(
        modules = {
                ApplicationModule.class, PrefsModule.class
        }
)
public interface BaseComponent {

    void inject(BaseActivity baseActivity);

    void inject(BaseFragment baseFragment);

}
