package crazysheep.io.nina.net_legacy;

import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import crazysheep.io.nina.net.HttpConstants;
import crazysheep.io.nina.utils.Utils;

/**
 * twitter client
 *
 * Created by crazysheep on 16/2/16.
 */
class TwitterClient {

    private static TwitterClient mTwitterClient;

    private static TwitterCore mTwitterCore;
    private static NinaTwitterApiClient mTwitterApiClient;

    private TwitterClient(TwitterCore twitterCore) {
        mTwitterCore = twitterCore;
    }

    public static TwitterClient getInstance() {
        if(Utils.isNull(mTwitterClient))
            synchronized (TwitterClient.class) {
                if(Utils.isNull(mTwitterClient)) {
                    TwitterAuthConfig authConfig = new TwitterAuthConfig(
                            HttpConstants.NINA_CONSUMER_KEY,
                            HttpConstants.NINA_CONSUMER_SECRET);
                    TwitterCore twitterCore = new TwitterCore(authConfig);

                    mTwitterClient = new TwitterClient(twitterCore);
                }
            }

        return mTwitterClient;
    }

    public TwitterCore getTwitterCore() {
        return mTwitterCore;
    }

    public NinaTwitterApiClient getTwitterApiClient() {
        if(Utils.isNull(mTwitterApiClient))
            mTwitterApiClient = new NinaTwitterApiClient(
                    mTwitterCore.getSessionManager().getActiveSession());

        return mTwitterApiClient;
    }

}
