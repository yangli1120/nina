package crazysheep.io.nina;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.Utils;
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

    private List<String> mVideoFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video_preview);
        ButterKnife.bind(this);

        mVideoFiles = getIntent().getStringArrayListExtra(BundleConstants.EXTRA_VIDEO_RECORD_FILES);
        if(Utils.size(mVideoFiles) <= 0)
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

        // TODO may be should need merge multi video files to one
        DebugHelper.log("RecordVideoPreviewActivity.onCreate(), video file: " + mVideoFiles.get(0));
        mPreviewTvv.setVideo(new File(mVideoFiles.get(0)));
        mPreviewTvv.setScaleType(TextureVideoView.SCALE_TYPE_CENTER_CROP);
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
        // TODO done
        DebugHelper.toast(this, "done");
    }

}
