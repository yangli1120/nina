package crazysheep.io.nina.net;

import android.support.annotation.NonNull;

import java.io.IOException;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.NetworkUtils;
import crazysheep.io.nina.utils.Utils;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * post tweet helper, do everything background
 * <p/>
 * Created by crazysheep on 16/2/18.
 */
public class RxTweeting {

    //////////////////////// api ////////////////////////////////

    public static class ErrorSubscription implements Subscription {

        private String subscriptionError;

        public ErrorSubscription(@NonNull String error) {
            this.subscriptionError = error;
        }

        public String getError() {
            return subscriptionError;
        }

        @Override
        public boolean isUnsubscribed() {
            return false;
        }

        @Override
        public void unsubscribe() {}
    }
    /////////////////////////////////////////////////////////////

    /**
     * post a tweet background
     *
     * @param postTweet The tweet to post
     */
    public static Subscription postTweet(@NonNull PostTweetBean postTweet) {
        // first step, check network state if is connected
        if(!NetworkUtils.isConnected(BaseApplication.getAppContext()))
            return new ErrorSubscription("network is not connected");

        return Observable.just(postTweet)
                .subscribeOn(Schedulers.io())
                // second step, save post tweet as draft(may be save to database or sharedperferences)
                .map(new Func1<PostTweetBean, PostTweetBean>() {
                    @Override
                    public PostTweetBean call(PostTweetBean postTweetBean) {
                        // TODO save post tweet as draft
                        return postTweetBean;
                    }
                })
                // third step, check if post tweet have media file to upload to twitter sever
                .map(new Func1<PostTweetBean, PostTweetBean>() {
                    @Override
                    public PostTweetBean call(PostTweetBean postTweetBean) {
                        // TODO upload media file and set media ids to post tweet bean if need
                        return postTweetBean;
                    }
                })
                // final step, post tweet
                .map(new Func1<PostTweetBean, TweetDto>() {
                    @Override
                    public TweetDto call(PostTweetBean post) {
                        try {
                            Response<TweetDto> response = HttpClient.getInstance()
                                    .getTwitterService()
                                    .postTweet(post.getStatus(), post.getReplyStatusId(),
                                            post.getPlaceId(), post.isDisplayCoordinates(),
                                            post.getMediaIds())
                                    .execute();

                            if (response.code() == HttpConstants.CODE_200
                                    && !Utils.isNull(response.body()))
                                return response.body();
                                // TODO post tweet error handle
                            else
                                throw Exceptions.propagate(new Throwable(
                                        "request \"" + response.raw().request().url() + "\" error: "
                                                + response.message()));
                        } catch (IOException ioe) {
                            ioe.printStackTrace();

                            throw Exceptions.propagate(ioe);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TweetDto>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO post tweet failed, handle it
                        DebugHelper.toast(BaseApplication.getAppContext(),
                                "post tweet exception: " + e.toString());
                    }

                    @Override
                    public void onNext(TweetDto tweetDto) {
                        // TODO post tweet successful, broadcast it
                        DebugHelper.toast(BaseApplication.getAppContext(),
                                "post tweet\"" + tweetDto.text + "\"" + " successful!");
                    }
                });
    }
}
