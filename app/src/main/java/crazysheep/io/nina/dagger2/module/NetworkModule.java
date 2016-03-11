package crazysheep.io.nina.dagger2.module;

import crazysheep.io.nina.dagger2.scope.DaggerActivity;
import crazysheep.io.nina.net.HttpClient;
import dagger.Module;
import dagger.Provides;

/**
 * network module
 *
 * Created by crazysheep on 16/3/11.
 */
@Module
public class NetworkModule {

    private HttpClient mHttpClient;

    public NetworkModule() {
        mHttpClient = HttpClient.getInstance();
    }

    @Provides
    @DaggerActivity
    public HttpClient provideHttpClient() {
        return mHttpClient;
    }

}
