package crazysheep.io.nina;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.widget.TextureVideoView;

/**
 * record video merge and preview
 *
 * Created by crazysheep on 16/3/23.
 */
public class RecordVideoPreviewActivity extends BaseActivity {

    @Bind(R.id.preview_fl) FrameLayout mPreviewFl;
    @Bind(R.id.preview_tv) TextureVideoView mPreviewTvv;
    @Bind(R.id.play_iv) ImageView mPlayIv;

    private String mVideoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video_preview);
        ButterKnife.bind(this);

        mVideoFile = getIntent().getStringExtra(BundleConstants.EXTRA_VIDEO_RECORD_FILE);
        if(TextUtils.isEmpty(mVideoFile))
            finish();

        mPreviewFl.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPreviewFl.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ViewGroup.LayoutParams params = mPreviewFl.getLayoutParams();
                params.height = mPreviewTvv.getMeasuredWidth();
                mPreviewFl.setLayoutParams(params);
            }
        });

        // may be should need merge multi video files to one
        mPreviewTvv.setScaleType(TextureVideoView.SCALE_TYPE_CENTER_CROP);
        mPreviewTvv.setVideo(new File(mVideoFile));
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
            mPlayIv.setImageResource(R.drawable.ic_animated_play_pause);
            if(mPlayIv.getDrawable() instanceof AnimatedVectorDrawable)
                ((AnimatedVectorDrawable)mPlayIv.getDrawable()).start();
            mPreviewTvv.play();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.done_tv)
    protected void clickDone() {
        Intent data = new Intent();
        data.putExtra(BundleConstants.EXTRA_VIDEO_RECORD_FINAL_FILE, mPreviewTvv.getVideoFile());
        setResult(Activity.RESULT_OK, data);
        finish();
    }

}
