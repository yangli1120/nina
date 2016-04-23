package crazysheep.io.nina.dagger2.component;

import crazysheep.io.nina.dagger2.scope.PerActivity;
import crazysheep.io.nina.net.HttpClient;
import dagger.Component;

/**
 * component for {@link crazysheep.io.nina.adapter.TimelineAdapter}
 *
 * Created by crazysheep on 16/4/22.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class)
public interface AdapterComponent {

    HttpClient getHttpClient();
}
