package crazysheep.io.nina.dagger2.component;

import android.app.Activity;
import android.support.v4.app.Fragment;

import crazysheep.io.nina.dagger2.scope.PerActivity;
import crazysheep.io.nina.reactnative.ReactNativeContainer;
import dagger.Component;

/**
 * component provide single ReactNativeContainer
 *
 * Created by crazysheep on 16/7/4.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class)
public interface ReactNativeComponent {

    void inject(Activity activity);
    void inject(Fragment fragment);

    ReactNativeContainer getReactNativeContainer();
}
