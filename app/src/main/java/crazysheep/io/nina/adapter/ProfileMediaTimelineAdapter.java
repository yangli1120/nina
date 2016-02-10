package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;

/**
 * profile media timeline adapter
 *
 * Created by crazysheep on 16/2/10.
 */
public class ProfileMediaTimelineAdapter extends RecyclerViewBaseAdapter<
        ProfileMediaTimelineAdapter.ImageHolder, TweetDto> {

    public ProfileMediaTimelineAdapter(@NonNull Context context, List<TweetDto> items) {
        super(context, items);
    }

    @Override
    protected ImageHolder onCreateHolder(ViewGroup parent, int viewType) {
        return new ImageHolder(
                mInflater.inflate(R.layout.item_media_timeline_image, parent, false));
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, final int position) {
        Glide.clear(holder.imgIv);
        Glide.with(mContext)
                .load(getItem(position).extended_entities.media.get(0).media_url_https)
                .placeholder(R.color.place_holder_bg)
                .fitCenter()
                .into(holder.imgIv);

        holder.imgIv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.imgIv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ViewGroup.LayoutParams params = holder.imgIv.getLayoutParams();
                params.height = Math.round(holder.imgIv.getMeasuredWidth()
                        / getWHAspectRatio(getItem(position)));
                holder.imgIv.setLayoutParams(params);
            }
        });
    }

    private float getWHAspectRatio(TweetDto tweetDto) {
        return tweetDto.extended_entities.media.get(0).sizes.large.w * 1f
                / tweetDto.extended_entities.media.get(0).sizes.large.h;
    }

    @Override
    public void onViewAttachedToWindow(final ImageHolder holder) {
        super.onViewAttachedToWindow(holder);

        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION
                && holder.imgIv.getWidth() > 0) {
            ViewGroup.LayoutParams params = holder.imgIv.getLayoutParams();
            params.height = Math.round(holder.imgIv.getWidth()
                    / getWHAspectRatio(getItem(holder.getAdapterPosition())));
            holder.imgIv.setLayoutParams(params);
        }
    }

    ///////////////////////////// view holder ////////////////////////////////////

    static class ImageHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tweet_img_iv) ImageView imgIv;

        ImageHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
