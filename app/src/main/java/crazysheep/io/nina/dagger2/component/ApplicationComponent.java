package crazysheep.io.nina.dagger2.component;

import javax.inject.Singleton;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.dagger2.module.ApplicationModule;
import dagger.Component;

/**
 * application component
 *
 * Created by crazysheep on 16/3/9.
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    BaseApplication getContext();
}
