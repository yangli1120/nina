package crazysheep.io.nina.net_legacy;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * helper for build twitter api request
 *
 * Created by crazysheep on 16/1/25.
 */
class RequestHeaderHelper {

    private static Comparator<Map.Entry<String, String>> mComparator = new Comparator<Map.Entry<String, String>>() {

        @Override
        public int compare(Map.Entry<String, String> lhs, Map.Entry<String, String> rhs) {
            return lhs.getKey().compareTo(rhs.getKey());
        }
    };

    /*
    * such like:
    * Authorization: OAuth oauth_consumer_key="OK4Jokq5amNT6NIASXA0rIUiI", oauth_nonce="5ca1ff6c549b765783795c5baf75d2f2", oauth_signature="NUQdUVamvXbc8sNU5K78sm32GGQ%3D", oauth_signature_method="HMAC-SHA1", oauth_timestamp="1453557047", oauth_token="3301250162-M6evawbslQqtCfUGN2BHBLsIwE07DeK4xekbSXu", oauth_version="1.0"
    * see{@link https://dev.twitter.com/oauth/overview/authorizing-requests}
    * */
    /*public static String builderAuth(
            @NonNull Context context, @NonNull String method,
            @NonNull String url, Map<String, String> requestBodyForm) {
        UserPrefs userPrefs = new UserPrefs(context);

        Authorization authorization = new Authorization(HttpConstants.NINA_CONSUMER_KEY,
                HttpConstants.NINA_CONSUMER_SECRET);
        String authorizationStr = authorization.getAuthorizationHeader(getRequestUrl(url), method,
                getQuerysFromUrl(url), userPrefs.getAuthToken(), userPrefs.getSecret(), false);

        // trim is very important, see{@link https://github.com/square/retrofit/issues/1153}
        return authorizationStr.trim();
    }*/

    private static String randomAuthNonce() {

        String uuid_string = UUID.randomUUID().toString();
        return uuid_string.replaceAll("-", "").trim();
    }

    /**
     * create authorization map but without "signature"
     * */
    private static List<Map.Entry<String, String>> buildAuthorizationMap(@NonNull String token) {
        String auth_nonce = randomAuthNonce();

        Map<String, String> authMap = new HashMap<>();
        // consumer key
        authMap.put(HttpConstants.OAUTH_CONSUMER_KEY, HttpConstants.NINA_CONSUMER_KEY);
        // nonce
        authMap.put(HttpConstants.OAUTH_NONCE, auth_nonce);
        // signature method
        authMap.put(HttpConstants.OAUTH_SIGNATURE_METHOD, "HMAC-SHA1");
        // timestamp
        authMap.put(HttpConstants.OAUTH_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000));
        // token
        authMap.put(HttpConstants.OAUTH_TOKEN, token);
        // version
        authMap.put(HttpConstants.OAUTH_VERSION, "1.0");

        return sortToListMap(authMap);
    }

    private static List<Map.Entry<String, String>> sortToListMap(@NonNull Map<String, String> map) {
        List<Map.Entry<String, String>> sortListMap = new ArrayList<>(map.size());
        for(Map.Entry<String, String> entry : map.entrySet())
            sortListMap.add(entry);
        Collections.sort(sortListMap, mComparator);

        return sortListMap;
    }

    private static Map<String, String> getQuerysFromUrl(@NonNull String url) {
        Uri uri = Uri.parse(url);
        Set<String> queryKeys = uri.getQueryParameterNames();
        if(queryKeys.size() > 0) {
            Map<String, String> querys = new HashMap<>(queryKeys.size());
            for(String query : queryKeys)
                if(!TextUtils.isEmpty(uri.getQueryParameter(query)))
                    querys.put(query, uri.getQueryParameter(query));

            return querys;
        } else {
            return null;
        }
    }

    /**
     * return encode request url
     * */
    private static String getRequestUrl(@NonNull String url) {
        String baseUrl = url;
        if(url.indexOf('?') > 0)
            baseUrl = url.substring(0, url.indexOf("?"));

        return baseUrl;
    }

    /**
     * hmacSha1 encode string
     *
     * see{@link https://gist.github.com/tistaharahap/1202974}
     * */
    private static String hmacSha1(String baseStr, String keyStr) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec key = new SecretKeySpec((keyStr).getBytes("UTF-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);

        byte[] bytes = mac.doFinal(baseStr.getBytes("UTF-8"));

        return Base64.encodeToString(bytes, Base64.URL_SAFE);
    }

    private static String generateSignKey(@NonNull String tokenSecret) {
        return new StringBuilder()
                .append(HttpConstants.NINA_CONSUMER_SECRET)
                .append("&")
                .append(tokenSecret)
                .toString()
                .trim();
    }

}
