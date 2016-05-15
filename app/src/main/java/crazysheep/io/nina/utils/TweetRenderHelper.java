package crazysheep.io.nina.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.twitter.Extractor;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import crazysheep.io.nina.R;
import crazysheep.io.nina.WebViewActivity;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.bean.TweetMediaDto;
import crazysheep.io.nina.bean.UrlDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.widget.TwitterLikeImageView;
import crazysheep.io.nina.widget.text.LinkTouchMovementMethod;
import crazysheep.io.nina.widget.text.TouchableSpan;
import retrofit2.Response;

/**
 * helper for render a tweet
 *
 * Created by crazysheep on 16/5/15.
 */
public class TweetRenderHelper {

    ////////////////// like event /////////////////////////

    public static class EventLikeStatus {
        private TweetDto tweetDto;

        public TweetDto getTweetDto() {
            return tweetDto;
        }

        public EventLikeStatus(@NonNull TweetDto tweetDto) {
            this.tweetDto = tweetDto;
        }
    }

    public static class EventUnLikeStatus {
        private TweetDto tweetDto;

        public TweetDto getTweetDto() {
            return tweetDto;
        }

        public EventUnLikeStatus(@NonNull TweetDto tweetDto) {
            this.tweetDto = tweetDto;
        }
    }

    public static class EventReplyTweet {
        private long replyStatusId;
        private ArrayList<String> metionedNames;

        public long getReplyStatusId() {
            return replyStatusId;
        }

        public ArrayList<String> getMetionedNames() {
            return metionedNames;
        }

        public EventReplyTweet(@NonNull ArrayList<String> metionedNames, long replyStatusId) {
            this.metionedNames = metionedNames;
            this.replyStatusId = replyStatusId;
        }
    }

    ///////////////////////////////////////////////////////

    /**
     * helper to render tweet content text, about url click, url format contians by tweet text
     * */
    public static void renderTxt(final @NonNull Context context, @NonNull TweetDto tweetDto,
                                       TextView contentTv) {
        if(!TextUtils.isEmpty(tweetDto.text) && !Utils.isNull(tweetDto.entities)
                && Utils.size(tweetDto.entities.getUrls()) > 0) {
            String text = tweetDto.text;
            if(!Utils.isNull(tweetDto.entities.media))
                for(TweetMediaDto mediaDto : tweetDto.entities.getMedias())
                    if(text.contains(mediaDto.url))
                        text = text.replace(mediaDto.url, "");

            for (UrlDto urlDto : tweetDto.entities.getUrls())
                text = text.replace(urlDto.url, urlDto.display_url);
            SpannableString ss = new SpannableString(text);
            int startIndex;
            for(final UrlDto urlDto : tweetDto.entities.getUrls()) {
                startIndex = text.indexOf(urlDto.display_url);
                ss.setSpan(
                        new TouchableSpan(
                                ContextCompat.getColor(context, R.color.url_highlight),
                                ContextCompat.getColor(context, R.color.url_highlight_pressed),
                                ContextCompat.getColor(context, android.R.color.darker_gray)) {
                            @Override
                            public void onClick(View widget) {
                                // use chrome if could, otherwise use local shit webview
                                ActivityUtils.start(context,
                                        ActivityUtils.prepare(context, WebViewActivity.class)
                                                .putExtra(BundleConstants.EXTRA_OPEN_WEB_URL,
                                                        urlDto.url));
                            }
                        },
                        startIndex,
                        startIndex + urlDto.display_url.length(),
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            contentTv.setText(ss);
            contentTv.setMovementMethod(LinkTouchMovementMethod.get());
        } else {
            contentTv.setText(tweetDto.text);
            contentTv.setMovementMethod(null);
        }
    }

    /**
     * render tweet bottom bar
     * */
    public static void renderBottomBar(
            @NonNull final Activity mContext, @NonNull final HttpClient mHttpClient,
            @NonNull boolean isMyOwnTweet, @NonNull final TweetDto tweetDto,
            @NonNull View replyLl, @NonNull View retweetLl, @NonNull final ImageView retweetIv,
            @NonNull TextView retweetCountTv,
            @NonNull View likeLl, @NonNull TextView likeCountTv,
            @NonNull final TwitterLikeImageView likeIv) {
        final TweetDto mTweetDto = tweetDto.isRetweeted() ? tweetDto.retweeted_status : tweetDto;

        replyLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Extractor extractor = new Extractor();
                ArrayList<String> metionedNames = new ArrayList<>();
                if (tweetDto.isRetweeted()) {
                    metionedNames.add(tweetDto.retweeted_status.user.screen_name);
                    for (String metionedName : extractor.extractMentionedScreennames(
                            tweetDto.retweeted_status.text))
                        if (!metionedNames.contains(metionedName))
                            metionedNames.add(metionedName);
                }
                if (!metionedNames.contains(mTweetDto.user.screen_name))
                    metionedNames.add(mTweetDto.user.screen_name);
                for (String metionedName : extractor.extractMentionedScreennames(tweetDto.text))
                    if (!metionedNames.contains(metionedName))
                        metionedNames.add(metionedName);

                EventBus.getDefault().post(new EventReplyTweet(metionedNames, tweetDto.id));
            }
        });
        // if this tweet is own by myself, cannot retweet
        if(isMyOwnTweet) {
            retweetIv.setImageResource(R.drawable.ic_retweet_light_grey_24dp);

            replyLl.setOnClickListener(null);
        } else {
            retweetIv.setImageResource(mTweetDto.retweeted ? R.drawable.ic_retweeted_green_24dp
                    : R.drawable.ic_retweet_grey_24dp);

            retweetLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mTweetDto.retweeted)
                        mHttpClient.getTwitterService()
                                .retweet(mTweetDto.id)
                                .enqueue(new NiceCallback.EmptyNiceCallback<TweetDto>());
                    else
                        mHttpClient.getTwitterService()
                                .unretweet(mTweetDto.id)
                                .enqueue(new NiceCallback.EmptyNiceCallback<TweetDto>());

                    mTweetDto.retweeted = !mTweetDto.retweeted;
                    retweetIv.setImageResource(mTweetDto.retweeted
                            ? R.drawable.ic_retweeted_green_24dp : R.drawable.ic_retweet_grey_24dp);
                }
            });
        }
        retweetCountTv.setText(String.valueOf(tweetDto.retweet_count));

        // like action
        likeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTweetDto.favorited) {
                    mHttpClient.getTwitterService()
                            .like(mTweetDto.id)
                            .enqueue(new NiceCallback<TweetDto>(mContext) {
                                @Override
                                public void onRespond(Response<TweetDto> response) {
                                    EventBus.getDefault().post(
                                            new EventLikeStatus(response.body()));
                                }

                                @Override
                                public void onFailed(Throwable t) {
                                }
                            });
                    likeIv.unlike();
                } else {
                    mHttpClient.getTwitterService()
                            .unlike(mTweetDto.id)
                            .enqueue(new NiceCallback<TweetDto>(mContext) {
                                @Override
                                public void onRespond(Response<TweetDto> response) {
                                    EventBus.getDefault().post(
                                            new EventUnLikeStatus(response.body()));
                                }

                                @Override
                                public void onFailed(Throwable t) {
                                }
                            });
                    likeIv.like();
                }
            }
        });
        likeCountTv.setText(String.valueOf(mTweetDto.favorite_count));
        likeIv.setHeartRes(R.drawable.ic_like_red_24dp, R.drawable.ic_unlike_grey_24dp);
        likeIv.setImageResource(mTweetDto.favorited
                ? R.drawable.ic_like_red_24dp : R.drawable.ic_unlike_grey_24dp);
    }

}
