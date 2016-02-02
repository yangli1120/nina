package crazysheep.io.nina.net;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.text.TextUtils;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import crazysheep.io.nina.R;
import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.net.HttpCache.CacheConfig;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.Utils;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

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
                    OkHttpClient okHttpClient = new OkHttpClient();
                    // ssl socket
                    /*try {
                        okHttpClient.setSslSocketFactory(
                                getSSLContent(BaseApplication.getAppContext(), R.raw.ca_bundle)
                                        .getSocketFactory());
                    } catch (Exception e) {
                        e.printStackTrace();
                        L.d(e.toString());
                    }*/
                    // config timeout
                    okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
                    okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
                    // external cache
                    okHttpClient.setCache(HttpCache.getInstance().okhttpCache());
                    // authorization interceptor
                    okHttpClient.interceptors().add(mAuthorizationInterceptor);
                    // cache control interceptor
                    okHttpClient.interceptors().add(mCacheControlInterceptor);
                    okHttpClient.networkInterceptors().add(mCacheControlNetworkInterceptor);
                    // use stetho debug network request
                    okHttpClient.networkInterceptors().add(new StethoInterceptor());

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
    * okhttp client interceptor for twitter api authorization
    * */
    private static Interceptor mAuthorizationInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder reqBuilder = chain.request().newBuilder();
            reqBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("User-Agent", "OAuth gem v0.4.4")
                    .addHeader("Host", HttpConstants.HOST_NAME)
                    // how to generate twitter REST api's authorization?
                    // see{@link http://soupkodjou.com/implementing-twitter-oauth-in-java-step-by-step/6/}
                    .addHeader("Authorization",
                            RequestHeaderHelper.builderAuth(
                                    BaseApplication.getAppContext(),
                                    chain.request().method(),
                                    chain.request().urlString(),
                                    null));

            return chain.proceed(reqBuilder.build());
        }
    };

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
            DebugHelper.log("begin request " + originReq.urlString());
            long startTime = System.currentTimeMillis();
            Response.Builder respBuilder = chain.proceed(originReq).newBuilder();
            DebugHelper.log("end request " + originReq.urlString()
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

    private static SSLContext getSSLContent(@NonNull Context context, @RawRes int resId)
            throws Exception {
        // build key store with ca certificate
        KeyStore keyStore = buildKeyStore(context, resId);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext;
    }

    private static KeyStore buildKeyStore(Context context, int certRawResId) throws Exception {
        // init a default key store
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);

        // read and add certificate authority
        Certificate cert = readCert(context, certRawResId);
        keyStore.setCertificateEntry("ca", cert);

        return keyStore;
    }

    private static Certificate readCert(Context context, int certResourceId)
            throws CertificateException, IOException, NoSuchProviderException {
        // read certificate resource
        InputStream caInput = context.getResources().openRawResource(certResourceId);

        Certificate ca;
        try {
            // generate a certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            ca = cf.generateCertificate(caInput);
        } finally {
            caInput.close();
        }

        return ca;
    }

}
