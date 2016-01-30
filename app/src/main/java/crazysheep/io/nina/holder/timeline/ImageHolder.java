package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.widget.imagegroup.GridGalleryLayout;

/**
 * image type tweet view holder
 *
 * Created by crazysheep on 16/1/29.
 */
public class ImageHolder extends BaseHolder implements GridGalleryLayout.OnChildLifeListener,
        GridGalleryLayout.OnChildClickListener{

    @Bind(R.id.tweet_gallery_ggl) GridGalleryLayout imgsGgl;
    @Bind(R.id.tweet_content_tv) TextView contentTv;

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
        contentTv.setText(tweetDto.text);
    }

    @Override
    public void onAttach(int position, ImageView view) {
        // cancel before task and clear before images
        Glide.clear(view);
        view.setImageResource(0);
        // load new one
        Glide.with(mContext)
                .load(mTweetDto.extended_entities.media.get(position).media_url_https)
                .into(view);
    }

    @Override
    public void onDetach(int position, ImageView view) {
        // cancel
        Glide.clear(view);
        view.setImageResource(0);
    }

    @Override
    public void onClick(int position, ImageView view) {
        // TODO view big image
        Toast.makeText(mContext, "click position: " + position, Toast.LENGTH_SHORT).show();
    }

}
