package crazysheep.io.nina.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
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
import crazysheep.io.nina.holder.timeline.TimelineHolderFactory;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.imagegroup.GridGalleryLayout;
import im.ene.lab.toro.widget.ToroVideoView;

/**
 * tweet detail, see{@link crazysheep.io.nina.TweetDetailActivity}
 *
 * Created by crazysheep on 16/5/7.
 */
public class TweetDetailFragment extends BaseFragment
        implements GridGalleryLayout.OnChildLifeListener, GridGalleryLayout.OnChildClickListener{

    @Nullable @Bind(R.id.tweet_gallery_ggl) GridGalleryLayout mImgsGgl;
    @Nullable @Bind(R.id.tweet_video_fl) FrameLayout mVideoFl;
    @Nullable @Bind(R.id.tweet_video_vv) ToroVideoView mVideoVv;
    @Nullable @Bind(R.id.tweet_video_preview_iv) ImageView mVideoPreviewIv;
    private TweetDto mTweetDto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTweetDto = getArguments().getParcelable(BundleConstants.EXTRA_TWEET);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        int viewType = TimelineHolderFactory.getViewType(mTweetDto);
        int layoutRes;
        switch (viewType) {
            case TimelineHolderFactory.TYPE_IMAGE: {
                layoutRes = R.layout.item_tweet_image;
            }break;

            case TimelineHolderFactory.TYPE_GIF: {
                layoutRes = R.layout.item_tweet_gif;
            }break;

            default:
                layoutRes = 0;
                break;
        }
        if(layoutRes == 0)
            return null;

        View contentView = inflater.inflate(layoutRes, container, false);
        ButterKnife.bind(this, contentView);

        if(viewType == TimelineHolderFactory.TYPE_IMAGE && !Utils.isNull(mImgsGgl)) {
            mImgsGgl.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImgsGgl.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    ViewGroup.LayoutParams params = mImgsGgl.getLayoutParams();
                    // width : height = 16 : 9
                    params.height = Math.round(9 * mImgsGgl.getMeasuredWidth() * 1f / 16);
                    mImgsGgl.setLayoutParams(params);
                }
            });
            mImgsGgl.setOnChildLifeListener(this);
            mImgsGgl.setItemCount(mTweetDto.extended_entities.media.size());
            mImgsGgl.setOnChildClickListener(this);
        }
        if(viewType == TimelineHolderFactory.TYPE_GIF && !Utils.isNull(mVideoVv)
                && !Utils.isNull(mVideoPreviewIv) && !Utils.isNull(mVideoFl)) {
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

            Glide.with(this)
                    .load(mTweetDto.extended_entities.media.get(0).media_url_https)
                    .placeholder(R.color.place_holder_bg)
                    .into(mVideoPreviewIv);

            mVideoVv.setVisibility(View.VISIBLE);
            mVideoVv.setVideoURI(Uri.parse(
                    mTweetDto.extended_entities.media.get(0).video_info.variants.get(0).url));
            mVideoVv.start();
            mVideoVv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Glide.clear(mVideoPreviewIv);
                    mVideoPreviewIv.setVisibility(View.GONE);
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

        return contentView;
    }

    @Override
    public void onAttach(int position, ImageView view) {
        view.setImageResource(0);

        TweetMediaDto mediaDto = mTweetDto.extended_entities.media.get(position);
        Glide.with(this)
                .load(mediaDto.media_url_https)
                .placeholder(R.color.place_holder_bg)
                .override(mediaDto.sizes.medium.w, mediaDto.sizes.medium.h)
                .centerCrop()
                .into(view);
    }

    @Override
    public void onDetach(int position, ImageView view) {
        Glide.clear(view);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClick(int position, ImageView view) {
        if(APICompat.api21()) {
            // transition animation
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    new Pair<View, String>(view, PhotoActivity.SHARED_ELEMENT_PHOTO));
            TweetMediaDto mediaDto = mTweetDto.extended_entities.media.get(position);
            Intent intent = new Intent(getActivity(), PhotoActivity.class);
            intent.putExtra(BundleConstants.EXTRA_PHOTO_URL, mediaDto.media_url_https);
            intent.putExtra(BundleConstants.EXTRA_PHOTO_THUMBNAIL_SIZE,
                    new int[]{mediaDto.sizes.medium.w, mediaDto.sizes.medium.h});
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        } else {
            ActivityUtils.start(getActivity(),
                    ActivityUtils.prepare(getActivity(), PhotoActivity.class)
                            .putExtra(BundleConstants.EXTRA_PHOTO_URL, mTweetDto.extended_entities
                                    .media.get(position).media_url_https));
        }
    }

    private float getWHAspectRatio() {
        return mTweetDto.extended_entities.media.get(0).video_info.aspect_ratio[0] * 1f
                / mTweetDto.extended_entities.media.get(0).video_info.aspect_ratio[1];
    }
}
