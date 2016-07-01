package crazysheep.io.nina.utils;

import rx.Subscription;

/**
 * utils for rxjava
 *
 * Created by crazysheep on 16/5/15.
 */
public class RxUtils {

    /**
     * un-subscribe
     * */
    public static void unsubscribe(Subscription subscription) {
        if(!Utils.isNull(subscription) && subscription.isUnsubscribed())
            subscription.unsubscribe();
    }
}
