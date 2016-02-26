package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.widget.imagegroup.GridGalleryLayout;

/**
 * image type draft holder
 *
 * Created by crazysheep on 16/2/25.
 */
public class DraftImageHolder extends DraftBaseHolder implements
        GridGalleryLayout.OnChildLifeListener {

    @Bind(R.id.tweet_gallery_ggl)
    GridGalleryLayout imgsGgl;

    public DraftImageHolder(@NonNull ViewGroup view) {
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
    public void bindData(int position, PostTweetBean postTweetBean) {
        super.bindData(position, postTweetBean);

        imgsGgl.setOnChildLifeListener(this);
        imgsGgl.setItemCount(postTweetBean.getPhotoPreviewFiles().size());
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.item_tweet_image;
    }

    @Override
    public void onAttach(int position, ImageView view) {
        Glide.clear(view);
        view.setImageResource(0);
        Glide.with(mContext)
                .load(new File(mPostTweetBean.getPhotoPreviewFiles().get(position)))
                .placeholder(R.color.place_holder_bg)
                .into(view);
    }

    @Override
    public void onDetach(int position, ImageView view) {
        Glide.clear(view);
        view.setImageResource(0);
    }
}
