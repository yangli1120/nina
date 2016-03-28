package crazysheep.io.nina.prefs;

import android.content.Context;

/**
 * save twitter config
 *
 * Created by crazysheep on 16/3/28.
 */
public class TwitterConfigPrefs extends BasePrefs {

    public static final int DURATION_CACHE_VALID = 24 * 60 * 60 * 1000; // 24h, 1day

    public static final String PREFS_NAME = "nina.config";

    public TwitterConfigPrefs(Context context) {
        super(context);
    }

    public static final String KEY_SHORT_URL_LENGTH = "key_short_url_length";
    public static final String KEY_SHORT_URL_LENGTH_HTTPS = "key_short_url_length_https";
    public static final String KEY_LASTED_UPDATE_TIME = "key_lasted_update_time";

    public void setShortUrlLength(int length) {
        setInt(KEY_SHORT_URL_LENGTH, length);
        setLong(KEY_LASTED_UPDATE_TIME, System.currentTimeMillis());
    }

    public int getShortUrlLength() {
        return getInt(KEY_SHORT_URL_LENGTH, 23);
    }

    public void setShortUrlLengthHttps(int lengthHttps) {
        setInt(KEY_SHORT_URL_LENGTH_HTTPS, lengthHttps);
        setLong(KEY_LASTED_UPDATE_TIME, System.currentTimeMillis());
    }

    public int getShortUrlLengthHttps() {
        return getInt(KEY_SHORT_URL_LENGTH_HTTPS, 23);
    }

    public boolean isDataValid() {
        return System.currentTimeMillis() - getLong(KEY_LASTED_UPDATE_TIME, -1)
                < DURATION_CACHE_VALID;
    }

}
