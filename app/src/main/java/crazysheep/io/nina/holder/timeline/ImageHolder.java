package crazysheep.io.nina.holder.timeline;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.PhotoActivity;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.bean.TweetMediaDto;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.widget.imagegroup.GridGalleryLayout;

/**
 * image type tweet view holder
 *
 * Created by crazysheep on 16/1/29.
 */
public class ImageHolder extends NormalBaseHolder implements GridGalleryLayout.OnChildLifeListener,
        GridGalleryLayout.OnChildClickListener{

    @Bind(R.id.tweet_gallery_ggl) GridGalleryLayout imgsGgl;

    public ImageHolder(@NonNull ViewGroup view) {
        super(view);
        ButterKnife.bind(this, view);

        // init imgsGgl's height reply to screen width
        imgsGgl.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imgsGgl.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ViewGroup.LayoutParams params = imgsGgl.getLayoutParams();
                // width : height = 16 : 9
                params.height = Math.round(9 * imgsGgl.getMeasuredWidth() * 1f / 16);
                imgsGgl.setLayoutParams(params);
            }
        });
    }

    @Override
    public int getContentViewRes() {
        return R.layout.item_tweet_image;
    }

    @Override
    public void bindData(int position, @NonNull TweetDto tweetDto) {
        super.bindData(position, tweetDto);

        imgsGgl.setOnChildLifeListener(this);
        imgsGgl.setItemCount(tweetDto.extended_entities.media.size());
        imgsGgl.setOnChildClickListener(this);
    }

    @Override
    public void onAttach(int childIndex, ImageView view) {
        // cancel before task and clear before images
        Glide.clear(view);
        view.setImageResource(0);

        TweetMediaDto mediaDto = mTweetDto.extended_entities.media.get(childIndex);
        // in timeline, photo should use small size to save memory, make timeline smooth
        Glide.with(mContext)
                .load(mediaDto.media_url_https)
                .override(mediaDto.sizes.small.w, mediaDto.sizes.small.h)
                .centerCrop()
                .into(view);
    }

    @Override
    public void onDetach(int position, ImageView view) {
        // cancel
        Glide.clear(view);
        view.setImageResource(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClick(int position, ImageView view) {
        if(APICompat.api21()) {
            // transition animation
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    (Activity) mContext,
                    new Pair<View, String>(view, PhotoActivity.SHARED_ELEMENT_PHOTO));
            TweetMediaDto mediaDto = mTweetDto.extended_entities.media.get(position);
            Intent intent = new Intent(mContext, PhotoActivity.class);
            intent.putExtra(BundleConstants.EXTRA_PHOTO_URL, mediaDto.media_url_https);
            intent.putExtra(BundleConstants.EXTRA_PHOTO_THUMBNAIL_SIZE,
                    new int[]{mediaDto.sizes.small.w, mediaDto.sizes.small.h});
            ActivityCompat.startActivity((Activity)mContext, intent, options.toBundle());
        } else {
            ActivityUtils.start(mContext,
                    ActivityUtils.prepare(mContext, PhotoActivity.class)
                            .putExtra(BundleConstants.EXTRA_PHOTO_URL, mTweetDto.extended_entities
                                    .media.get(position).media_url_https));
        }
    }

}
