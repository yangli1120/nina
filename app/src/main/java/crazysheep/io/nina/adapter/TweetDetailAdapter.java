package crazysheep.io.nina.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.TimeUtils;
import crazysheep.io.nina.utils.TweetRenderHelper;
import crazysheep.io.nina.widget.TwitterLikeImageView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * adapter for tweet detail, see{@link crazysheep.io.nina.TweetDetailActivity}
 *
 * Created by crazysheep on 16/5/7.
 */
public class TweetDetailAdapter extends RecyclerViewBaseAdapter<TweetDetailAdapter.ReplyHolder,
        TweetDto> {

    private UserPrefs mUserPrefs;

    public TweetDetailAdapter(@NonNull Context context, List<TweetDto> items) {
        super(context, items);

        mUserPrefs = new UserPrefs(context);
    }

    @Override
    public void onBindViewHolder(ReplyHolder holder, int position) {
        TweetDto tweetDto = getItem(position);

        Glide.clear(holder.mAvatarIv);
        Glide.with(mContext)
                .load(tweetDto.user.biggerProfileImageUrlHttps())
                .into(holder.mAvatarIv);

        holder.mAuthorNameTv.setText(tweetDto.user.name);
        holder.mAuthorScreenNameTv.setText(tweetDto.user.screen_name);
        holder.mTimeTv.setText(TimeUtils.formatTimestamp(mContext,
                TimeUtils.getTimeFromDate(tweetDto.created_at.trim())));
        // content text
        TweetRenderHelper.renderTxt(mContext, tweetDto, holder.mContentTv);

        // render bottom bar
        TweetRenderHelper.renderBottomBar((Activity)mContext, HttpClient.getInstance(),
                mUserPrefs.getUserScreenName().equals(tweetDto.user.screen_name),
                tweetDto,
                holder.mReplyLl, holder.mRetweetLl, holder.mRetweetIv, holder.mRetweetCountTv,
                holder.mLikeLl, holder.mLikeCountTv, holder. mLikeIv);
    }

    @Override
    protected ReplyHolder onCreateHolder(ViewGroup parent, int viewType) {
        return new ReplyHolder(mInflater.inflate(R.layout.item_reply, parent, false));
    }

    /////////////////////////// view holder /////////////////////////////////////

    static class ReplyHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.author_avatar_iv) CircleImageView mAvatarIv;
        @Bind(R.id.author_name_tv) TextView mAuthorNameTv;
        @Bind(R.id.author_screen_name_tv) TextView mAuthorScreenNameTv;
        @Bind(R.id.time_tv) TextView mTimeTv;
        @Bind(R.id.tweet_content_tv) TextView mContentTv;
        // bottom bar
        @Bind(R.id.action_reply_ll) View mReplyLl;
        @Bind(R.id.action_retweet_ll) View mRetweetLl;
        @Bind(R.id.action_retweet_iv) ImageView mRetweetIv;
        @Bind(R.id.action_retweet_count_tv) TextView mRetweetCountTv;
        @Bind(R.id.action_like_ll) View mLikeLl;
        @Bind(R.id.action_like_count_tv) TextView mLikeCountTv;
        @Bind(R.id.action_like_iv) TwitterLikeImageView mLikeIv;

        public ReplyHolder(@NonNull View parent) {
            super(parent);
            ButterKnife.bind(this, parent);
        }
    }

}
