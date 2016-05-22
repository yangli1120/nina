package crazysheep.io.nina.holder.timeline;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.ProfileActivity;
import crazysheep.io.nina.R;
import crazysheep.io.nina.TweetDetailActivity;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.TimeUtils;
import crazysheep.io.nina.utils.TweetRenderHelper;
import crazysheep.io.nina.widget.TwitterLikeImageView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * base viewholder for timeline, implements base layout
 *
 * Created by crazysheep on 16/1/23.
 */
public abstract class NormalBaseHolder extends BaseHolder<TweetDto>
        implements View.OnClickListener {

    @Bind(R.id.holder_root) View rootView;
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
    @Bind(R.id.action_like_iv) TwitterLikeImageView likeIv;
    @Bind(R.id.action_like_count_tv) TextView likeCountTv;

    protected Context mContext;
    protected TweetDto mTweetDto;
    private UserPrefs mUserPrefs;

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
        rootView.setOnClickListener(this);

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
        TweetRenderHelper.renderTxt(mContext, mTweetDto, contentTv);

        /* bottom action bar */
        TweetRenderHelper.renderBottomBar((Activity)mContext, HttpClient.getInstance(),
                mUserPrefs.getUserScreenName().equals(mTweetDto.user.screen_name), tweetDto,
                replyLl, retweetLl, retweetIv, retweetCountTv, likeLl, likeCountTv, likeIv);
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

            case R.id.holder_root: {
                // tweet detail
                ActivityUtils.start(mContext,
                        ActivityUtils.prepare(mContext, TweetDetailActivity.class)
                                .putExtra(BundleConstants.EXTRA_TWEET, mTweetDto));
            }break;
        }
    }

}
