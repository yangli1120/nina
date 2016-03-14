package crazysheep.io.nina.holder.timeline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.twitter.Extractor;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.ProfileActivity;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.TimeUtils;
import crazysheep.io.nina.widget.TwitterLikeImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Response;

/**
 * base viewholder for timeline, implements base layout
 *
 * Created by crazysheep on 16/1/23.
 */
public abstract class NormalBaseHolder extends BaseHolder<TweetDto>
        implements View.OnClickListener {

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

    @Bind(R.id.author_avatar_iv) CircleImageView avatarIv;
    @Bind(R.id.retweet_author_tv) TextView retweetAuthorTv;
    @Bind(R.id.author_name_tv) TextView authorNameTv;
    @Bind(R.id.author_screen_name_tv) TextView authorScreenNameTv;
    @Bind(R.id.time_tv) TextView timeTv;
    @Bind(R.id.tweet_content_fl) FrameLayout contentFl;
    @Bind(R.id.tweet_content_tv) TextView contentTv;
    @Bind(R.id.action_reply_ll) View replyLl;
    @Bind(R.id.action_reply_iv) ImageView replyIv;
    @Bind(R.id.action_retweet_ll) View retweetLl;
    @Bind(R.id.action_retweet_iv) ImageView retweetIv;
    @Bind(R.id.action_retweet_count_tv) TextView retweetCountTv;
    @Bind(R.id.action_like_ll) View likeLl;
    @Bind(R.id.action_like_iv)
    TwitterLikeImageView likeIv;
    @Bind(R.id.action_like_count_tv) TextView likeCountTv;

    protected Context mContext;
    protected TweetDto mTweetDto;
    protected UserPrefs mUserPrefs;

    public NormalBaseHolder(@NonNull ViewGroup parent) {
        super(parent);
        View contentView = LayoutInflater.from(parent.getContext())
                .inflate(getContentViewRes(), parent, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        contentFl = ButterKnife.findById(parent, R.id.tweet_content_fl);
        contentFl.addView(contentView, params);

        mContext = parent.getContext();
        mUserPrefs = new UserPrefs(mContext);
    }

    public abstract int getContentViewRes();

    /**
     * base holder implement common ui, sub-holder implement itself ui
     * */
    public void bindData(int position, @NonNull final TweetDto tweetDto) {
        mTweetDto = tweetDto.isRetweeted() ? tweetDto.retweeted_status : tweetDto;

        /* top header */
        // cancel before image load request if need
        Glide.clear(avatarIv);
        // start current request
        Glide.with(mContext)
                .load(mTweetDto.user.biggerProfileImageUrlHttps())
                .into(avatarIv);
        avatarIv.setOnClickListener(this);

        if(tweetDto.isRetweeted()) {
            retweetAuthorTv.setVisibility(View.VISIBLE);
            retweetAuthorTv.setText(
                    mContext.getString(R.string.retweet_author, tweetDto.user.name));
        } else {
            retweetAuthorTv.setVisibility(View.GONE);
        }
        authorNameTv.setText(mTweetDto.user.name);
        authorNameTv.setOnClickListener(this);
        authorScreenNameTv.setText(mContext.getString(R.string.screen_name,
                mTweetDto.user.screen_name));
        authorScreenNameTv.setOnClickListener(this);
        timeTv.setText(TimeUtils.formatTimestamp(mContext,
                TimeUtils.getTimeFromDate(mTweetDto.created_at.trim())));

        /* content layout should be render by sub-holder */
        contentTv.setText(mTweetDto.text);

        /* bottom action bar */
        replyLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Extractor extractor = new Extractor();
                ArrayList<String> metionedNames = new ArrayList<>();
                if(tweetDto.isRetweeted()) {
                    metionedNames.add(tweetDto.retweeted_status.user.screen_name);
                    for(String metionedName : extractor.extractMentionedScreennames(
                            tweetDto.retweeted_status.text))
                        if(!metionedNames.contains(metionedName))
                            metionedNames.add(metionedName);
                }
                if(!metionedNames.contains(mTweetDto.user.screen_name))
                    metionedNames.add(mTweetDto.user.screen_name);
                for(String metionedName : extractor.extractMentionedScreennames(tweetDto.text))
                    if(!metionedNames.contains(metionedName))
                        metionedNames.add(metionedName);

                // TODO reply this tweet
                EventBus.getDefault().post(new EventReplyTweet(metionedNames, tweetDto.id));
            }
        });
        retweetLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugHelper.toast(mContext, "click retweet");
            }
        });
        retweetCountTv.setText(String.valueOf(tweetDto.retweet_count));

        // like action
        likeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTweetDto.favorited) {
                    HttpClient.getInstance()
                            .getTwitterService()
                            .like(Long.parseLong(mTweetDto.idStr))
                            .enqueue(new NiceCallback<TweetDto>() {
                                @Override
                                public void onRespond(Response<TweetDto> response) {
                                    EventBus.getDefault().post(
                                            new EventLikeStatus(response.body()));
                                }

                                @Override
                                public void onFailed(Throwable t) {}
                            });
                    likeIv.unlike();
                } else {
                    HttpClient.getInstance()
                            .getTwitterService()
                            .unlike(Long.parseLong(mTweetDto.idStr))
                            .enqueue(new NiceCallback<TweetDto>() {
                                @Override
                                public void onRespond(Response<TweetDto> response) {
                                    EventBus.getDefault().post(
                                            new EventUnLikeStatus(response.body()));
                                }

                                @Override
                                public void onFailed(Throwable t) {}
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

    @Override
    public boolean isSwipeEnable() {
        // if this tweet's author is myself, then I can delete it if I'm happy
        return mUserPrefs.getUserScreenName().equals(mTweetDto.user.screen_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.author_avatar_iv:
            case R.id.author_name_tv:
            case R.id.author_screen_name_tv: {
                ActivityUtils.start(mContext,
                        ActivityUtils.prepare(mContext, ProfileActivity.class)
                                .putExtra(BundleConstants.EXTRA_USER_SCREEN_NAME,
                                        mTweetDto.user.screen_name)
                                .putExtra(BundleConstants.EXTRA_USER_NAME, mTweetDto.user.name));
            }break;
        }
    }

}
