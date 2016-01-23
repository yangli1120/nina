package crazysheep.io.nina.net;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.Utils;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * network layer
 *
 * Created by crazysheep on 16/1/22.
 */
public class NetClient {

    private static Retrofit mRetrofit;

    private NetClient() {}

    public static Retrofit getInstance(@NonNull final Application application) {
        if(Utils.isNull(mRetrofit))
            synchronized (NetClient.class) {
                if (Utils.isNull(mRetrofit)) {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.interceptors().add(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder()
                                    .addHeader("Authorization", authBuilder(application))
                                    .build();

                            return chain.proceed(request);
                        }
                    });
                    // use stetho debug network request
                    okHttpClient.networkInterceptors().add(new StethoInterceptor());

                    mRetrofit = new Retrofit.Builder()
                            .baseUrl(ApiConstants.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(okHttpClient)
                            .build();
                }
            }

        return mRetrofit;
    }

    /*
    * such like:
    * Authorization: OAuth oauth_consumer_key="OK4Jokq5amNT6NIASXA0rIUiI", oauth_nonce="5ca1ff6c549b765783795c5baf75d2f2", oauth_signature="NUQdUVamvXbc8sNU5K78sm32GGQ%3D", oauth_signature_method="HMAC-SHA1", oauth_timestamp="1453557047", oauth_token="3301250162-M6evawbslQqtCfUGN2BHBLsIwE07DeK4xekbSXu", oauth_version="1.0"
    * */
    private static String authBuilder(@NonNull Context context) {
        UserPrefs userPrefs = new UserPrefs(context);
        StringBuilder sb = new StringBuilder(" OAuth ");
        sb.append("oauth_consumer_key=")
                .append("\"OK4Jokq5amNT6NIASXA0rIUiI\"")
                .append(", oauth_consumer_secret=")
                .append("\"uv2jOCK3GbsFwOhN7gpgIJHwFvksz59GlxQgWXw56VIy9ZVRr1\"")
                .append(", oauth_token=")
                .append("\"")
                .append(userPrefs.getAuthToken())
                .append("\"")
                .append(", oauth_token_secret=")
                .append("\"")
                .append(userPrefs.getSecret())
                .append("\"")
                .append(", oauth_version=\"1.0\"");

        L.d("Authorization: " + sb.toString());

        return sb.toString();
    }

}
