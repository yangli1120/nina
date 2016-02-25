package crazysheep.io.nina.holder.timeline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * base viewholder for timeline, implements base layout
 *
 * Created by crazysheep on 16/1/23.
 */
public abstract class NormalBaseHolder extends BaseHolder<TweetDto>
        implements View.OnClickListener {

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
    @Bind(R.id.action_like_iv) ImageView likeIv;
    @Bind(R.id.action_like_count_tv) TextView likeCountTv;

    protected Context mContext;
    protected TweetDto mTweetDto;

    public NormalBaseHolder(@NonNull ViewGroup parent) {
        super(parent);
        View contentView = LayoutInflater.from(parent.getContext())
                .inflate(getContentViewRes(), parent, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        contentFl = ButterKnife.findById(parent, R.id.tweet_content_fl);
        contentFl.addView(contentView, params);

        mContext = parent.getContext();
    }

    public abstract int getContentViewRes();

    /**
     * base holder implement common ui, sub-holder implement itself ui
     * */
    public void bindData(int position, @NonNull TweetDto tweetDto) {
        mTweetDto = tweetDto.isRetweeted() ? tweetDto.retweeted_status : tweetDto;

        /* top header */
        // cancel before image load request if need
        Glide.clear(avatarIv);
        // start current request
        Glide.with(mContext)
                .load(mTweetDto.user.profile_image_url_https)
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
                DebugHelper.toast(mContext, "click reply");
            }
        });
        retweetLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugHelper.toast(mContext, "click retweet");
            }
        });
        retweetCountTv.setText(String.valueOf(tweetDto.retweet_count));
        likeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugHelper.toast(mContext, "click like");
            }
        });
        likeCountTv.setText(String.valueOf(mTweetDto.user.favourites_count));
        likeIv.setImageResource(mTweetDto.favorited
                ? R.drawable.ic_favorite_black : R.drawable.ic_un_favorite_grey);
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
