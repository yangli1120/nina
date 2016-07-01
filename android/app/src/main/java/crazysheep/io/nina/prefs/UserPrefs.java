package crazysheep.io.nina.prefs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * user prefs
 *
 * Created by crazysheep on 16/1/22.
 */
public class UserPrefs extends BasePrefs {

    public static final String PREFS_NAME = "nina.user";

    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_USER_SCREEN_NAME = "key_user_screen_name";
    public static final String KEY_USER_AVATAR = "key_user_avatar";
    public static final String KEY_ID = "key_id";
    public static final String KEY_SECRET = "key_secret";
    public static final String KEY_AUTH_TOKEN = "key_auth_token";

    public UserPrefs(@NonNull Context context) {
        super(context);
    }

    public final void setUsername(String name) {
        setString(KEY_USER_NAME, name);
    }

    public final String getUsername() {
        return getString(KEY_USER_NAME, null);
    }

    public final void setUserScreenName(String screenName) {
        setString(KEY_USER_SCREEN_NAME, screenName);
    }

    public final String getUserScreenName() {
        return getString(KEY_USER_SCREEN_NAME, null);
    }

    public final void setUserAvatar(String avatarUrl) {
        setString(KEY_USER_AVATAR, avatarUrl);
    }

    public final String getUserAvatar() {
        return getString(KEY_USER_AVATAR, null);
    }

    public final void setId(long id) {
        setLong(KEY_ID, id);
    }

    public final long getId() {
        return getLong(KEY_ID, -1);
    }

    public final void setSecret(String secret) {
        setString(KEY_SECRET, secret);
    }

    public final String getSecret() {
        return getString(KEY_SECRET, null);
    }

    public final void setAuthToken(String token) {
        setString(KEY_AUTH_TOKEN, token);
    }

    public final String getAuthToken() {
        return getString(KEY_AUTH_TOKEN, null);
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(getAuthToken()) && !TextUtils.isEmpty(getSecret())
                && !TextUtils.isEmpty(getUserScreenName()) && getId() > 0;
    }

    public void logout() {
        clear();
    }

}
