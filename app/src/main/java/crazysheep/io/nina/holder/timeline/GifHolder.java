package crazysheep.io.nina.holder.timeline;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.adapter.RecyclerViewBaseAdapter;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.widget.GlideSimpleViewTarget;

/**
 * timeline view holder for "animated_gif" type tweets
 *
 * Created by crazysheep on 16/2/5.
 */
public class GifHolder extends BaseHolder
        implements RecyclerViewBaseAdapter.OnViewHolderLifeCallback<GifHolder> {

    @Bind(R.id.tweet_video_fl) FrameLayout mVideoFl;
    @Bind(R.id.tweet_video_vv) VideoView mVideoVv;
    @Bind(R.id.tweet_gif_label_tv) TextView mLabelTv;

    public GifHolder(@NonNull ViewGroup view) {
        super(view);
        ButterKnife.bind(this, view);

        mVideoFl.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoFl.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mVideoFl
                        .getLayoutParams();
                // calculate video parent aspect height
                params.height = Math.round(mVideoFl.getMeasuredWidth() / getWHAspectRatio());
                mVideoFl.setLayoutParams(params);
            }
        });
    }

    @Override
    public int getContentViewRes() {
        return R.layout.item_tweet_gif;
    }

    @Override
    public void bindData(int position, @NonNull TweetDto tweetDto) {
        super.bindData(position, tweetDto);

        mLabelTv.setVisibility(View.VISIBLE);
        Glide.clear(mVideoVv);
        Glide.with(mContext)
                .load(mTweetDto.extended_entities.media.get(0).media_url_https)
                .placeholder(R.color.place_holder_bg)
                .into(GlideSimpleViewTarget.createViewTarget(mVideoVv));
    }

    @Override
    public void onViewRecycled(GifHolder holder) {
        mVideoVv.stopPlayback();
        mVideoVv.setVisibility(View.GONE);
    }

    @Override
    public void onViewAttached(GifHolder holder) {
        animateLabel();
        mVideoVv.setVisibility(View.VISIBLE);
        mVideoVv.setVideoURI(Uri.parse(
                mTweetDto.extended_entities.media.get(0).video_info.variants.get(0).url));
        mVideoVv.start();
        mVideoVv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Glide.clear(mVideoVv);
                clearLabelAnimation();
                mVideoVv.setBackground(null);
            }
        });
        mVideoVv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoVv.seekTo(0);
                mVideoVv.start();
            }
        });
    }

    @Override
    public void onViewDetached(GifHolder holder) {
        mVideoVv.stopPlayback();
        mVideoVv.setVisibility(View.GONE);
    }

    private float getWHAspectRatio() {
        return mTweetDto.extended_entities.media.get(0).video_info.aspect_ratio.get(0) * 1f
                / mTweetDto.extended_entities.media.get(0).video_info.aspect_ratio.get(1);
    }

    private void animateLabel() {
        mLabelTv.setVisibility(View.VISIBLE);
        ViewCompat.animate(mLabelTv)
                .alpha(0.2f)
                .setDuration(1000)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        reverseAnimateLabel();
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }
                })
                .start();
    }

    private void reverseAnimateLabel() {
        ViewCompat.animate(mLabelTv)
                .alpha(1f)
                .setDuration(1000)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        animateLabel();
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }
                })
                .start();
    }

    private void clearLabelAnimation() {
        ViewCompat.animate(mLabelTv)
                .setListener(null)
                .cancel();
        ViewCompat.setAlpha(mLabelTv, 1f);
    }

}
