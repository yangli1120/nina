package crazysheep.io.nina.net;

import android.app.Application;
import android.support.annotation.NonNull;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

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
                                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                                    .addHeader("User-Agent", "OAuth gem v0.4.4")
                                    .addHeader("Host", "api.twitter.com")
                                    // how to generate twitter REST api's authorization?
                                    // see{@link http://soupkodjou.com/implementing-twitter-oauth-in-java-step-by-step/6/}
                                    .addHeader("Authorization",
                                            RequestHeaderHelper.builderAuth(application,
                                                    chain.request().method(),
                                                    chain.request().urlString(),
                                                    null))
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

}
