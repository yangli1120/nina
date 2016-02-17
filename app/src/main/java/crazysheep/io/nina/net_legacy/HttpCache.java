package crazysheep.io.nina.net_legacy;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.utils.Utils;
import okhttp3.Cache;
import okhttp3.CacheControl;

/**
 * http cache
 *
 * Created by crazysheep on 16/1/26.
 */
class HttpCache {

    private static HttpCache mInstance;
    private Cache mCache; // cache for okhttp client

    private HttpCache(@NonNull Context context) {
        mCache = new Cache(new File(context.getExternalCacheDir(), CacheConfig.FILE_DIR),
                CacheConfig.FILE_SIZE);
    }

    public static HttpCache getInstance() {
        if(Utils.isNull(mInstance))
            synchronized (HttpCache.class) {
                if(Utils.isNull(mInstance))
                    mInstance = new HttpCache(BaseApplication.getAppContext());
            }

        return mInstance;
    }

    public Cache okhttpCache() {
        return mCache;
    }

    /**
     * config request cache config
     *
     * Created by crazysheep on 16/1/26.
     */
    public static class CacheConfig {

        public static final String FILE_DIR = "http_cache";
        public static final long FILE_SIZE = 10 * 1024 * 1024; // 10M cache size

        public static final String PARAM_CACHE_CONTROL = "cache_control";

        public static final int DEFAULT_CACHE_FRESH_DURATION = 5 * 60; // cache fresh time is 5 minutes

        public static final int CACHE_NETWORK = -1; // response from network only, not cache
        public static final int CACHE_IF_HIT = 0; // if hit cache, response from cache, otherwise from network
        public static final int CACHE_FORCE = 1; // response only from cache, not network

        /**
         * make cache control from cacheType
         *
         * @param cacheType CACHE_NETWORK, CACHE_IF_HIT or CACHE_FORE.
         * */
        public static CacheControl getCacheControl(int cacheType) {
            switch (cacheType) {
                case CACHE_NETWORK: {
                    return CacheControl.FORCE_NETWORK;
                }

                case CACHE_IF_HIT: {
                    return new CacheControl.Builder()
                            .maxAge(DEFAULT_CACHE_FRESH_DURATION, TimeUnit.SECONDS)
                            .build();
                }

                case CACHE_FORCE: {
                    return CacheControl.FORCE_CACHE;
                }

                default:
                    return CacheControl.FORCE_NETWORK;
            }
        }

    }

}
