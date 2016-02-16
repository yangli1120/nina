package crazysheep.io.nina.net;

import android.content.Context;
import android.support.annotation.NonNull;

import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.L;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * request use rxjava
 *
 * Created by crazysheep on 16/2/3.
 */
public class RxRequest {

    public interface RxRequestCallback<T> {
        void onRespond(T t);
        void onFailed(Throwable t);
    }

    private static Twitter getTwitter(@NonNull Context context) {
        final UserPrefs mUserPrefs = new UserPrefs(context);
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(HttpConstants.NINA_CONSUMER_KEY)
                .setOAuthConsumerSecret(HttpConstants.NINA_CONSUMER_SECRET)
                .setOAuthAccessToken(mUserPrefs.getAuthToken())
                .setOAuthAccessTokenSecret(mUserPrefs.getSecret());
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    /**
     * request user info
     * */
    public static void showUser(@NonNull Context context, @NonNull final String screenName,
                                @NonNull final RxRequestCallback<User> callback) {
        final Twitter twitter = getTwitter(context);

        Observable.just(twitter)
                .map(new Func1<Twitter, User>() {
                    @Override
                    public User call(Twitter twitter) {
                        try {
                            return twitter.showUser(screenName);
                        } catch (TwitterException te) {
                            L.d("fetch user exception: " + te);
                        }

                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onFailed(e);
                    }

                    @Override
                    public void onNext(User user) {
                        callback.onRespond(user);
                    }
                });
    }

}
