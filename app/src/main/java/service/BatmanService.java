package service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.constants.EventBusConstants;
import crazysheep.io.nina.net.RxTweeting;
import crazysheep.io.nina.utils.Utils;

/**
 * "He's a silent guardian, a watchful protector, a dark knight."
 *
 * Created by crazysheep on 16/2/19.
 */
public class BatmanService extends Service {

    private BatmanBinder mBinder = new BatmanBinder();
    public class BatmanBinder extends Binder {
        public BatmanService getService() {
            return BatmanService.this;
        }
    }

    private static final int MSG_POST_TWEET = 9527;
    private LinkedList<PostTweetBean> mPostQueue = new LinkedList<>();

    private boolean hasTweetPosting = false;

    private Selina mHandler = new Selina(this);
    private static class Selina extends Handler {
        private WeakReference<BatmanService> reference;

        public Selina(BatmanService service) {
            reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_POST_TWEET: {
                    if(!Utils.isNull(reference.get()) && reference.get().mPostQueue.size() > 0) {
                        PostTweetBean postTweetBean = reference.get().mPostQueue.poll();
                        if(!Utils.isNull(postTweetBean)) {
                            // if post tweet bean is not null, start request
                            reference.get().hasTweetPosting = true;
                            RxTweeting.postTweet(postTweetBean);
                        } else if(reference.get().mPostQueue.size() > 0) {
                            // if post tweet is null, but queue is more than 0, toggle next post
                            sendEmptyMessage(MSG_POST_TWEET);
                        }
                    }
                }break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void postTweet(@NonNull PostTweetBean postTweetBean) {
        mPostQueue.addLast(postTweetBean);

        if(!hasTweetPosting)
            mHandler.sendEmptyMessage(MSG_POST_TWEET);
    }

    @SuppressWarnings("unused")
    @Subscribe(priority = EventBusConstants.PRIORITY_HIGH)
    public void onEvent(@NonNull RxTweeting.EventPostTweetSuccess event) {
        // TODO post tweet success, delete draft from database

        // notify queue to post next tweet
        hasTweetPosting = false;
        mHandler.sendEmptyMessage(MSG_POST_TWEET);
    }

    @SuppressWarnings("unused")
    @Subscribe(priority = EventBusConstants.PRIORITY_HIGH)
    public void onEvent(@NonNull RxTweeting.EventPostTweetFailed event) {
        // TODO post tweet failed, re-add post tweet bean to queue's last for next change, and update draft state in database
        mPostQueue.addLast(event.getPostTweetBean());

        // notify queue to post next tweet
        hasTweetPosting = false;
        mHandler.sendEmptyMessage(MSG_POST_TWEET);
    }

}
