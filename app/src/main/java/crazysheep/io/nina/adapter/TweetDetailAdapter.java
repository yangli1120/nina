package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.utils.TimeUtils;
import crazysheep.io.nina.utils.TweetRenderHelper;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * adapter for tweet detail, see{@link crazysheep.io.nina.TweetDetailActivity}
 *
 * Created by crazysheep on 16/5/7.
 */
public class TweetDetailAdapter extends RecyclerViewBaseAdapter<TweetDetailAdapter.ReplyHolder,
        TweetDto> {

    public TweetDetailAdapter(@NonNull Context context, List<TweetDto> items) {
        super(context, items);
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

        public ReplyHolder(@NonNull View parent) {
            super(parent);
            ButterKnife.bind(this, parent);
        }
    }

}
