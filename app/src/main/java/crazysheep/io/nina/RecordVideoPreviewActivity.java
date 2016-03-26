package crazysheep.io.nina;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.fragment.VideoPreviewFragment;
import crazysheep.io.nina.io.RxFile;
import crazysheep.io.nina.widget.TextureVideoView;

/**
 * record video merge and preview
 *
 * Created by crazysheep on 16/3/23.
 */
public class RecordVideoPreviewActivity extends BaseActivity {

    @Bind(R.id.preview_fl) FrameLayout mPreviewFl;

    private String mVideoFile;
    private VideoPreviewFragment mPreviewFt;

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
                params.height = mPreviewFl.getMeasuredWidth();
                mPreviewFl.setLayoutParams(params);
            }
        });

        mPreviewFt = new VideoPreviewFragment();
        mPreviewFt.setVideo(new File(mVideoFile), TextureVideoView.SCALE_TYPE_CENTER_CROP);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.preview_fl, mPreviewFt, VideoPreviewFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.done_tv)
    protected void clickDone() {
        Intent data = new Intent();
        data.putExtra(BundleConstants.EXTRA_VIDEO_RECORD_FINAL_FILE, mVideoFile);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        // user pressed back key mean give up current video, delete it
        if(!TextUtils.isEmpty(mVideoFile)) {
            File finalVideo = new File(mVideoFile);
            RxFile.delete(finalVideo, null);
        }
        super.onBackPressed();
    }
}
