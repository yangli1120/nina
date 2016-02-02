package crazysheep.io.nina.net;

/**
 * api constants
 *
 * Created by crazysheep on 16/1/22.
 */
public class HttpConstants {

    public static final String BASE_URL = "https://api.twitter.com/1.1/";
    public static final String HOST_NAME = "api.twitter.com";

    ////////////////////// http status code ///////////////////////////////

    public static final int CODE_200 = 200; // every thing is OK
    public static final int code_404 = 404; // Not Found
    public static final int code_403 = 403; // Forbidden

    ////////////////////// twitter api request header params //////////////////////////

    // generate at twitter offical website
    public static final String NINA_CONSUMER_KEY = "7QZaDRVbHnQUaAwQUAGTVYFsd";
    public static final String NINA_CONSUMER_SECRET = "X8msK0MvFLLQdrq9ifx0sU9uxXWCzAS8Y2YcweFSV4ISEFrA1v";

    public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    public static final String OAUTH_CONSUMER_SECRET = "oauth_consumer_secret";
    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String OAUTH_NONCE = "oauth_nonce";
    public static final String OAUTH_SIGNATURE = "oauth_signature";
    public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    public static final String OAUTH_VERSION = "oauth_version";

}
