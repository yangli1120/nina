package crazysheep.io.nina.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.R;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.TextureVideoView;

/**
 * fragment focus on show video preview
 *
 * Created by crazysheep on 16/3/26.
 */
public class VideoPreviewFragment extends Fragment {

    @Bind(R.id.preview_tvv) TextureVideoView mPreviewTvv;
    @Bind(R.id.preview_iv) ImageView mPreviewIv;
    @Bind(R.id.play_iv) ImageView mPlayIv;

    private MediaMetadataRetriever mMediaRetriever;
    private Bitmap mPreviewBm;

    private File videoFile;
    private int scaleType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_video_preview, container, false);
        ButterKnife.bind(this, contentView);

        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPreviewTvv.setScaleType(scaleType);
        mPreviewTvv.setVideo(videoFile);

        mMediaRetriever = new MediaMetadataRetriever();
        try {
            mMediaRetriever.setDataSource(videoFile.getAbsolutePath());
            mPreviewBm = mMediaRetriever.getFrameAtTime();
            mPreviewIv.setImageBitmap(mPreviewBm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(!Utils.isNull(mPreviewBm)) {
            mPreviewBm.recycle();
            mPreviewBm = null;
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.play_iv)
    protected void clickPlay() {
        if(mPreviewTvv.isPlaying()) {
            mPlayIv.setImageResource(R.drawable.ic_animated_pause_play);
            if(mPlayIv.getDrawable() instanceof AnimatedVectorDrawable)
                ((AnimatedVectorDrawable)mPlayIv.getDrawable()).start();
            mPreviewTvv.pause();
        } else {
            mPreviewIv.setVisibility(View.GONE);
            mPlayIv.setImageResource(R.drawable.ic_animated_play_pause);
            if(mPlayIv.getDrawable() instanceof AnimatedVectorDrawable)
                ((AnimatedVectorDrawable)mPlayIv.getDrawable()).start();
            mPreviewTvv.play();
        }
    }

    public String getVideo() {
        return Utils.isNull(videoFile) ? null : videoFile.getAbsolutePath();
    }

    public void setVideo(@NonNull File video, int scaleType) {
        videoFile = video;
        this.scaleType = scaleType;
    }

    public void setVideo(@NonNull File video) {
        videoFile = video;
        this.scaleType = TextureVideoView.SCALE_TYPE_CENTER_CROP;
    }

}
