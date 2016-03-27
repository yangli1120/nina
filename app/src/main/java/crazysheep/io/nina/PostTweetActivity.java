package crazysheep.io.nina;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.twitter.Extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.GalleryActivity.Options;
import crazysheep.io.nina.adapter.PreviewGalleryAdapter;
import crazysheep.io.nina.bean.MediaStoreImageBean;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.fragment.VideoPreviewFragment;
import crazysheep.io.nina.service.BatmanService;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.ImeUtils;
import crazysheep.io.nina.utils.Utils;
import me.imid.swipebacklayout.lib.SwipeBackLayout;

/**
 * create a tweet
 *
 * Created by crazysheep on 16/2/17.
 */
public class PostTweetActivity extends BaseSwipeBackActivity implements TextWatcher {

    public static final int REQUEST_CHOOSE_IMAGE_OR_CAPTURE_VIDEO = 100;

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.edit_tweet_et) EditText mTweetEt;
    @Bind(R.id.metioned_tips_tv) TextView mMetionedTipsTv;
    @Bind(R.id.send_tweet_btn) Button mSendBtn;
    @Bind(R.id.image_preview_rv) RecyclerView mPhotoPreviewRv;
    @Bind(R.id.video_preview_fl) View mVideoPreviewFl;
    private PreviewGalleryAdapter mPreviewAdapter;

    private VideoPreviewFragment mVideoPreviewFt;

    // if post a reply tweet
    private long replayStatusId;
    private Extractor mTweetExtractor;
    private List<String> metionedScreenNames;

    private ArrayList<MediaStoreImageBean> mSelectedImages;

    private boolean isServiceBinding = false;
    private BatmanService mBatmanService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isServiceBinding = true;
            mBatmanService = ((BatmanService.BatmanBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBinding = false;
            mBatmanService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_tweet);
        ButterKnife.bind(this);

        replayStatusId = getIntent().getLongExtra(BundleConstants.EXTRA_REPLY_STATUS_ID, -1);
        metionedScreenNames = getIntent().getStringArrayListExtra(
                BundleConstants.EXTRA_METIONED_NAMES);
        mTweetExtractor = new Extractor();

        setSupportActionBar(mToolbar);
        if(!Utils.isNull(getSupportActionBar())) {
            getSupportActionBar().setTitle(null);

            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
        }

        mSendBtn.setEnabled(false);
        mTweetEt.addTextChangedListener(this);
        if(Utils.size(metionedScreenNames) > 0) {
            mMetionedTipsTv.setVisibility(View.VISIBLE);
            mMetionedTipsTv.setText(getString(R.string.metioned_user, metionedScreenNames.get(0)));

            StringBuilder sb = new StringBuilder();
            for(String screenName: metionedScreenNames)
                sb.append("@").append(screenName).append(" ");
            mTweetEt.setText(sb.toString());
            mTweetEt.setSelection(sb.length());
        }

        mPreviewAdapter = new PreviewGalleryAdapter(this, null);
        mPreviewAdapter.setOnItemRemoveListener(new PreviewGalleryAdapter.OnItemRemoveListener() {
            @Override
            public void onRemoved(int position) {
                updateSendButton();
            }
        });
        mPhotoPreviewRv.setLayoutManager(new GridLayoutManager(this, 4));
        mPhotoPreviewRv.setAdapter(mPreviewAdapter);

        getSwipeBackLayout().addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                if(state != SwipeBackLayout.STATE_IDLE)
                    ImeUtils.hide(getActivity());
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {}

            @Override
            public void onScrollOverThreshold() {}
        });

        // bind service
        bindService(new Intent(this, BatmanService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isServiceBinding)
            unbindService(mConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CHOOSE_IMAGE_OR_CAPTURE_VIDEO: {
                    // return selected images or capture video, show preview
                    if(Utils.size(data.getParcelableArrayListExtra(
                            BundleConstants.EXTRA_SELECTED_IMAGES)) > 0) {
                        // clear view preview
                        if(!Utils.isNull(mVideoPreviewFt))
                            getSupportFragmentManager().beginTransaction()
                                    .remove(mVideoPreviewFt)
                                    .commitAllowingStateLoss();
                        mVideoPreviewFl.setVisibility(View.GONE);

                        mPhotoPreviewRv.setVisibility(View.VISIBLE);
                        mSelectedImages = data.getParcelableArrayListExtra(
                                BundleConstants.EXTRA_SELECTED_IMAGES);
                        mPreviewAdapter.setData(mSelectedImages);
                    } else if(!TextUtils.isEmpty(data.getStringExtra(
                            BundleConstants.EXTRA_VIDEO_RECORD_FINAL_FILE))) {
                        // clear selected images if have
                        mSelectedImages = null;
                        mPhotoPreviewRv.setVisibility(View.GONE);

                        String videoFilepath = data.getStringExtra(
                                BundleConstants.EXTRA_VIDEO_RECORD_FINAL_FILE);
                        mVideoPreviewFl.setVisibility(View.VISIBLE);
                        mVideoPreviewFt = new VideoPreviewFragment();
                        mVideoPreviewFt.setVideo(new File(videoFilepath));
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.video_preview_fl, mVideoPreviewFt,
                                        VideoPreviewFragment.class.getSimpleName())
                                .commitAllowingStateLoss();
                    }

                    updateSendButton();
                }break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mSendBtn.setEnabled(!TextUtils.isEmpty(s.toString()));

        // TODO check if words length is too long that twitter forbid a tweet status length more than 140
    }

    @OnClick(R.id.add_image_iv)
    protected void addImage() {
        ActivityUtils.startResult(this, REQUEST_CHOOSE_IMAGE_OR_CAPTURE_VIDEO,
                ActivityUtils.prepare(this, GalleryActivity.class)
                        .putParcelableArrayListExtra(BundleConstants.EXTRA_SELECTED_IMAGES,
                                mSelectedImages)
                        .putExtra(BundleConstants.EXTRA_GALLERY_OPTIONS,
                                new Options.Builder()
                                        .chooseImage()
                                        .takePhoto()
                                        .captureVideo()
                                        .build()));
    }

    @OnClick(R.id.send_tweet_btn)
    protected void postTweet() {
        // TODO post a tweet, txt, photo, or video
        PostTweetBean.Builder builder = new PostTweetBean.Builder();
        // set selected photos if have
        if(Utils.size(mSelectedImages) > 0) {
            List<String> photoFiles = new ArrayList<>(mSelectedImages.size());
            for(MediaStoreImageBean item : mSelectedImages)
                photoFiles.add(item.filepath);
            builder.setPhotoFiles(photoFiles);
        }
        // set tweet text
        if(!TextUtils.isEmpty(mTweetEt.getEditableText().toString()))
            builder.setStatus(mTweetEt.getEditableText().toString());
        if(replayStatusId > 0
                && !TextUtils.isEmpty(mTweetExtractor.extractReplyScreenname(
                    mTweetEt.getEditableText().toString())))
            builder.setReplyStatusId(replayStatusId);
        // set selected video file if have
        DebugHelper.log("postTweet(), mVideoPreviewFt is null? " + Utils.isNull(mVideoPreviewFt)
                + ", video path: " + mVideoPreviewFt.getVideo());
        if(!Utils.isNull(mVideoPreviewFt))
            builder.setVideoFile(mVideoPreviewFt.getVideo());
        PostTweetBean postTweet = builder.build();
        mBatmanService.postTweet(postTweet);

        // set result to TimelineFragment to show draft item UI in timeline
        DebugHelper.log(String.format("return post tweet, model id %s", postTweet.getId()));
        Intent data = new Intent();
        data.putExtra(BundleConstants.EXTRA_POST_TWEET, postTweet);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void updateSendButton() {
        mSendBtn.setEnabled(!TextUtils.isEmpty(mTweetEt.getEditableText().toString())
                || Utils.size(mSelectedImages) > 0
                || (!Utils.isNull(mVideoPreviewFt)
                        && !TextUtils.isEmpty(mVideoPreviewFt.getVideo())));
    }

}
