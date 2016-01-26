package crazysheep.io.nina.net;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.net.HttpCache.CacheConfig;
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

    public static Retrofit getInstance() {
        if(Utils.isNull(mRetrofit))
            synchronized (NetClient.class) {
                if (Utils.isNull(mRetrofit)) {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    // config timeout
                    okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
                    okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
                    // external cache
                    okHttpClient.setCache(HttpCache.getInstance().okhttpCache());
                    // authorization interceptor
                    okHttpClient.interceptors().add(mAuthorizationInterceptor);
                    okHttpClient.networkInterceptors().add(mCacheControlInterceptor);
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
    * okhttp client interceptor for twitter api authorization
    * */
    private static Interceptor mAuthorizationInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("User-Agent", "OAuth gem v0.4.4")
                    .addHeader("Host", ApiConstants.HOST_NAME)
                    // how to generate twitter REST api's authorization?
                    // see{@link http://soupkodjou.com/implementing-twitter-oauth-in-java-step-by-step/6/}
                    .addHeader("Authorization",
                            RequestHeaderHelper.builderAuth(
                                    BaseApplication.getAppContext(),
                                    chain.request().method(),
                                    chain.request().urlString(),
                                    null))
                    .build();
            return chain.proceed(request);
        }
    };

    /*
    * okhttp client interceptor for cache control
    * ## be careful, cache control interceptor must add to okhttp client's networkInterceptors,
    *    not interceptors ##
    * */
    private static Interceptor mCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            /* handle request data */
            Request originReq = chain.request();
            int cacheType = Integer.valueOf(originReq.header(CacheConfig.PARAM_CACHE_CONTROL));
            CacheControl cacheControl = CacheConfig.getCacheControl(cacheType);

            /* handle original response data */
            Response.Builder respBuilder = chain.proceed(originReq).newBuilder();
            // for enable url cache,
            // see{@link http://stackoverflow.com/questions/31321963/how-retrofit-with-okhttp-use-cache-data-when-offline}
            // see{@link https://github.com/square/retrofit/issues/693}
            respBuilder.removeHeader("Pragma");
            respBuilder.header("cache-control", cacheControl.toString());

            return respBuilder.build();
        }
    };

}
