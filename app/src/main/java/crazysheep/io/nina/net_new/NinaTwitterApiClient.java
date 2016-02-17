package crazysheep.io.nina.net_new;

import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterApiClient;

/**
 * custom twitter api client for request REST api more than twitter-kit-android sdk
 * see{@link https://docs.fabric.io/android/twitter/access-rest-api.html#Extenstions}
 *
 * Created by crazysheep on 16/2/16.
 */
public class NinaTwitterApiClient extends TwitterApiClient {

    public NinaTwitterApiClient(Session session) {
        super(session);
    }

    /**
     * twitter REST api service
     * */
    public TwitterService getTwitterService() {
        return getService(TwitterService.class);
    }

}
