package crazysheep.io.nina.dagger2.component;

import crazysheep.io.nina.dagger2.scope.PerActivity;
import crazysheep.io.nina.holder.timeline.NormalBaseHolder;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.prefs.UserPrefs;
import dagger.Component;

/**
 * component for ViewHolder
 *
 * Created by crazysheep on 16/4/22.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class)
public interface ViewHolderComponent {

    void inject(NormalBaseHolder holder);

    HttpClient getHttpClient();

    UserPrefs getUserPrefs();
}
