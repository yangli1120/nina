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
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.utils.DebugHelper;

/**
 * base viewholder for timeline, implements base layout
 *
 * Created by crazysheep on 16/1/23.
 */
public abstract class BaseHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.author_avatar_iv) ImageView avatarIv;
    @Bind(R.id.author_name_tv) TextView authorNameTv;
    @Bind(R.id.time_tv) TextView timeTv;
    @Bind(R.id.tweet_content_fl) FrameLayout contentFl;
    @Bind(R.id.action_reply_ll) View replyLl;
    @Bind(R.id.action_reply_iv) ImageView replyIv;
    @Bind(R.id.action_retweet_ll) View retweetLl;
    @Bind(R.id.action_retweet_iv) ImageView retweetIv;
    @Bind(R.id.action_retweet_count_tv) TextView retweetCountTv;
    @Bind(R.id.action_like_ll) View likeLl;
    @Bind(R.id.action_like_iv) ImageView likeIv;
    @Bind(R.id.action_like_count_tv) TextView likeCountTv;

    protected Context mContext;

    public BaseHolder(@NonNull ViewGroup parent, @NonNull Context context) {
        super(parent);
        View contentView = LayoutInflater.from(context).inflate(getContentViewRes(), parent, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        contentFl = ButterKnife.findById(parent, R.id.tweet_content_fl);
        contentFl.addView(contentView, params);

        mContext = context;
    }

    public abstract int getContentViewRes();

    /**
     * base holder implement common ui, sub-holder implement itself ui
     * */
    public void bindData(@NonNull TweetDto tweetDto) {
        /* top header */
        // cancel before image load request if need
        Glide.clear(avatarIv);
        // start current request
        Glide.with(mContext)
                .load(tweetDto.user.profile_image_url)
                .into(avatarIv);

        authorNameTv.setText(tweetDto.user.screen_name);
        timeTv.setText(tweetDto.created_at);

        /* content layout should be render by sub-holder */

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
        likeCountTv.setText(String.valueOf(tweetDto.user.favourites_count));
        likeIv.setImageResource(tweetDto.favorited
                ? R.drawable.ic_favorite_black : R.drawable.ic_un_favorite_grey);
    }

}
