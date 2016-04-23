package crazysheep.io.nina.dagger2.module;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

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

    public NetworkModule(@NonNull Context context) {
        mHttpClient = HttpClient.getInstance(context);
    }

    @Provides
    @Singleton
    public HttpClient provideHttpClient() {
        return mHttpClient;
    }

}
