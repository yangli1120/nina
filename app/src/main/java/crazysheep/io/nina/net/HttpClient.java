package crazysheep.io.nina.net;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import crazysheep.io.nina.R;
import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.net.HttpCache.CacheConfig;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.Utils;
import okhttp3.CacheControl;
import okhttp3.CertificatePinner;
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
                            // ssl socket factory for twitter
                            .sslSocketFactory(getPinnedCertSslSocketFactory(BaseApplication.getAppContext()))
                            // Certificate Pinner
                            // how to generate pins?
                            // see{@link http://stackoverflow.com/questions/24006545/how-can-i-pin-a-certificate-with-square-okhttp}
                            // see{@link https://square.github.io/okhttp/2.x/okhttp/com/squareup/okhttp/CertificatePinner.html}
                            .certificatePinner(new CertificatePinner.Builder()
                                    .add(HttpConstants.HOST_NAME, "sha1/xL1ID5vs0PCu6fmKxLXWYTt8yiE")
                                    .add(HttpConstants.HOST_NAME, "sha1/3lKvjNsfmrn+WmfDhvr2iVh/yRs=")
                                    .build())
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

    // see{@link http://developer.android.com/intl/zh-cn/training/articles/security-ssl.html#Unknown certificate authority}
    // for dowmload .pem file, see{@link https://twittercommunity.com/t/how-to-use-the-ssl-certificate/12454}
    // for generate .crt file, see{@link http://android.stackexchange.com/questions/4053/how-do-you-import-ca-certificates-onto-an-android-phone}
    private static SSLContext createSSLContext(Context context) {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf;
        InputStream caInput;
        try {
            cf = CertificateFactory.getInstance("X.509", "BC");
            caInput = new BufferedInputStream(
                    context.getResources().openRawResource(R.raw.bithertruststore));
        } catch (CertificateException | NoSuchProviderException e) {
            e.printStackTrace();

            throw new RuntimeException("create sll context step 1 exception: " + e);
        }

        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            caInput.close();
        } catch (CertificateException | IOException e) {
            e.printStackTrace();

            throw new RuntimeException("create sll context step 2 exception: " + e);
        }

        // Create a KeyStore containing our trusted CAs
        KeyStore keyStore;
        try {
            String keyStoreType = KeyStore.getDefaultType();
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
        } catch (KeyStoreException | CertificateException
                | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();

            throw new RuntimeException("create sll context step 3 exception: " + e);
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        TrustManagerFactory tmf;
        try {
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();

            throw new RuntimeException("create sll context step 4 exception: " + e);
        }

        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {tm}, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();

            throw new RuntimeException("create sll context step 5 exception: " + e);
        }

        return sslContext;
    }

    // how to generate ssl socket factory for https ssl/tls
    // prepare step 1: download .pem file from {@link https://twittercommunity.com/t/how-to-use-the-ssl-certificate/12454}
    // prepare step 2: transform .pem file to .bks file,
    //         (we must add "Bouncy Castle" jar to JAVA_HOME/jre/lib/ext/,
    //          and modify JAVA_HOME/jre/lib/ext/lib/security/java.security)
    //         see{@link http://stackoverflow.com/questions/24006545/how-can-i-pin-a-certificate-with-square-okhttp}
    //         see{@link http://songchenwen.com/tech/2015/01/28/android-trust-self-signed-ssl-certificate/}
    // prepare step 3: add generate .bks file to android application project res/raw/
    private static SSLSocketFactory getPinnedCertSslSocketFactory(Context context) {
        KeyStore localTrustStore;
        InputStream input = null;
        try {
            localTrustStore = KeyStore.getInstance("BKS");
            input = context.getResources().openRawResource(R.raw.bithertruststore);
            localTrustStore.load(input, "bither".toCharArray());
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException
                | CertificateException e) {
            throw new RuntimeException("generate keystore exception: " + e);
        } finally {
            if(input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        BitherTrustManager trustManager = new BitherTrustManager(localTrustStore);

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{trustManager}, new java.security.SecureRandom());

            return sc.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("init ssl context exception: " + e);
        }
    }

}
