package crazysheep.io.nina.net;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.net.HttpCache.CacheConfig;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.Utils;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;

/**
 * network layer
 *
 * Created by crazysheep on 16/1/22.
 */
public class HttpClient {

    private static Retrofit mRetrofit;

    private HttpClient() {}

    public static Retrofit getInstance() {
        if(Utils.isNull(mRetrofit))
            synchronized (HttpClient.class) {
                if (Utils.isNull(mRetrofit)) {
                    UserPrefs userPrefs = new UserPrefs(BaseApplication.getAppContext());
                    OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(
                            HttpConstants.NINA_CONSUMER_KEY, HttpConstants.NINA_CONSUMER_SECRET);
                    consumer.setTokenWithSecret(userPrefs.getAuthToken(), userPrefs.getSecret());

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            // config timeout
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            // external cache
                            .cache(HttpCache.getInstance().okhttpCache())
                            // authorization interceptor
                            .addInterceptor(new SigningInterceptor(consumer))
                            // cache control interceptor
                            .addInterceptor(mCacheControlInterceptor)
                            .addNetworkInterceptor(mCacheControlNetworkInterceptor)
                            // use stetho debug network request
                            .addNetworkInterceptor(new StethoInterceptor())
                            .build();

                    mRetrofit = new Retrofit.Builder()
                            .baseUrl(HttpConstants.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(okHttpClient)
                            .build();
                }
            }

        return mRetrofit;
    }

    /*
    * okhttp client will not invoke network interceptor if hit cache, so we
    * must add a application interceptor to avoid this.
    * see{@link https://github.com/square/okhttp/wiki/Interceptors}
    * */
    private static Interceptor mCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            CacheControl cacheControl = parseCacheControlFromRequest(chain.request());
            Request.Builder reqBuilder = chain.request().newBuilder();
            reqBuilder.header("cache_control", cacheControl.toString());

            return chain.proceed(reqBuilder.build());
        }
    };

    /*
    * okhttp client interceptor for cache control
    * ## be careful, cache control interceptor must add to okhttp client's networkInterceptors,
    *    not interceptors ##
    * */
    private static Interceptor mCacheControlNetworkInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            /* handle request data */
            String cacheControl = chain.request().header("cache_control");

            /* handle original response data */
            Request originReq = chain.request();
            DebugHelper.log("begin request " + originReq.url().toString());
            long startTime = System.currentTimeMillis();
            Response.Builder respBuilder = chain.proceed(originReq).newBuilder();
            DebugHelper.log("end request " + originReq.url().toString()
                    + ", use time: " + (System.currentTimeMillis() - startTime) + "ms");
            // for enable url cache, response header is very important
            // see{@link http://stackoverflow.com/questions/31321963/how-retrofit-with-okhttp-use-cache-data-when-offline}
            // see{@link https://github.com/square/retrofit/issues/693}
            respBuilder.removeHeader("Pragma");
            respBuilder.header("cache-control", cacheControl);

            return respBuilder.build();
        }
    };

    private static CacheControl parseCacheControlFromRequest(@NonNull Request request) {
        // by default, if request not give a cache control, use CacheControl.FORCE_NETWORK
        L.d("parse header, cache control: " + request.header(CacheConfig.PARAM_CACHE_CONTROL));
        if(TextUtils.isEmpty(request.header(CacheConfig.PARAM_CACHE_CONTROL)))
            return CacheControl.FORCE_NETWORK;
        int cacheType = Integer.valueOf(request.header(CacheConfig.PARAM_CACHE_CONTROL));
        return CacheConfig.getCacheControl(cacheType);
    }

}
