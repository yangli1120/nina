package crazysheep.io.nina.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.BaseActivity;
import crazysheep.io.nina.R;
import crazysheep.io.nina.RecordVideoPreviewActivity;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.io.RxFile;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.DialogUtils;
import crazysheep.io.nina.utils.ICameraPreviewHelper;
import crazysheep.io.nina.utils.RxVideo;
import crazysheep.io.nina.utils.SystemUIHelper;
import crazysheep.io.nina.utils.ToastUtils;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.ExProgressBar;

/**
 * use camera2 api capture video
 *
 * Created by crazysheep on 16/3/17.
 */
public class CameraCaptureVideoFragment extends Fragment
        implements BaseActivity.OnBackPressedListener {

    private static final int REQUEST_VIDEO_PREVIEW = 1;
    private static int MAX_VIDEO_DURATION = 30; // 30s
    private static int MIN_VIDEO_DURATION = 5; // 5s

    @Bind(R.id.video_auto_fit_tv) TextureView mTextureView;
    @Bind(R.id.action_ll) View mActionLl;
    @Bind(R.id.capture_btn) Button mCaptureBtn;
    @Bind(R.id.ex_pb) ExProgressBar mExPb;
    @Bind(R.id.delete_tv) TextView mDeleteTv;

    private ICameraPreviewHelper mCameraPreviewHelper;

    private Dialog mLoadingDlg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_capture_video, container, false);
        ButterKnife.bind(this, contentView);

        mTextureView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTextureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ViewGroup.LayoutParams params = mTextureView.getLayoutParams();
                params.height = mTextureView.getMeasuredWidth();
                mTextureView.setLayoutParams(params);
            }
        });
        // hack mActionFl
        if(SystemUIHelper.hasNavBar(getResources())
                && SystemUIHelper.isNavBarTranslucent(getResources()))
            mActionLl.setPadding(mActionLl.getPaddingLeft(), mActionLl.getPaddingTop(),
                    mActionLl.getPaddingRight(),
                    mActionLl.getPaddingBottom() + SystemUIHelper.getNavBarSize(getResources()));

        mCaptureBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case MotionEvent.ACTION_DOWN: {
                        if(mExPb.isReachMaxProgress())
                            showMaxDurationDialog();
                        else
                           startRecord();
                    }
                    break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        stopRecord();
                    }
                    break;
                }

                return true;
            }
        });
        // setup exprogressbar
        mExPb.setMaxProgress(MAX_VIDEO_DURATION);
        mExPb.setMinProgress(0);
        mExPb.setWarningProgress(MIN_VIDEO_DURATION);
        mExPb.setOnProgressListener(new ExProgressBar.OnProgressListener() {
            @Override
            public void onStart(int currentProgress) {
            }

            @Override
            public void onEnd(int maxProgress) {
                // reach max record duration
                stopRecord();

                showMaxDurationDialog();
            }
        });

        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCameraPreviewHelper = ICameraPreviewHelper.newInstance(getActivity(), mTextureView);
    }

    @Override
    public void onResume() {
        super.onResume();

        mCameraPreviewHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mCameraPreviewHelper.onPause();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.delete_tv)
    protected void clickDelete() {
        if(!mExPb.isPrepareDelete()) {
            mExPb.prepareDelete();
        } else {
            mExPb.delete();
            mCameraPreviewHelper.getVideoRecorderHelper().markDeleteLastPart();
        }
    }

    @SuppressWarnings("unused, unchecked")
    @OnClick(R.id.capture_done_btn)
    protected void clickNextStep() {
        if(Utils.size(mCameraPreviewHelper.getVideoRecorderHelper().getRecordedFiles()) <= 0) {
            ToastUtils.t(getActivity(), getString(R.string.toast_not_recorded_video_file));

            return;
        }
        if(mExPb.isInvalid()) {
            ToastUtils.t(getActivity(), getString(R.string.toast_record_video_duration_too_short));

            return;
        }

        DebugHelper.log(String.format("recorded files: [ %s ]",
                mCameraPreviewHelper.getVideoRecorderHelper().getRecordedFilesPath()));

        final String targetFilePath = new File(mCameraPreviewHelper.getVideoRecorderHelper()
                .getSessionFileDir(), "final.mp4")
                .getAbsolutePath();
        if(Utils.size(mCameraPreviewHelper.getVideoRecorderHelper().getRecordedFilesPath()) > 1) {
            mLoadingDlg = DialogUtils.showLoadingDialog(getActivity());
            RxVideo.merge(mCameraPreviewHelper.getVideoRecorderHelper().getRecordedFilesPath(),
                    targetFilePath,
                    new RxVideo.Callback() {
                        @Override
                        public void onSuccess(List<String> sources, String targetFilePath) {
                            DialogUtils.dismissDialog(mLoadingDlg);
                            startForResult(targetFilePath);
                        }

                        @Override
                        public void onFailed(String err) {
                            DialogUtils.dismissDialog(mLoadingDlg);
                            DebugHelper.log(err);
                        }
                    });
        } else if(Utils.size(mCameraPreviewHelper.getVideoRecorderHelper().getRecordedFilesPath()) == 1) {
            mLoadingDlg = DialogUtils.showLoadingDialog(getActivity());
            RxFile.copy(mCameraPreviewHelper.getVideoRecorderHelper().getRecordedFiles()
                            .get(0).getAbsolutePath(), targetFilePath,
                    new RxFile.Callback() {
                        @Override
                        public void onSuccess() {
                            DialogUtils.dismissDialog(mLoadingDlg);
                            startForResult(targetFilePath);
                        }

                        @Override
                        public void onFailed(String err) {
                            DialogUtils.dismissDialog(mLoadingDlg);
                            DebugHelper.log(err);
                        }
                    });
        } else {
            ToastUtils.t(getActivity(), getString(R.string.toast_not_recorded_video_file));
        }
    }

    private void startForResult(String targetFilePath) {
        ActivityUtils.startResult(CameraCaptureVideoFragment.this, REQUEST_VIDEO_PREVIEW,
                ActivityUtils.prepare(getActivity(), RecordVideoPreviewActivity.class)
                        .putExtra(BundleConstants.EXTRA_VIDEO_RECORD_FILE, targetFilePath));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_VIDEO_PREVIEW: {
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                }break;
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        DialogUtils.showConfirmDialog(getActivity(),
                getString(R.string.dialog_give_up_record_video), null,
                new DialogUtils.ButtonAction() {
                    @Override
                    public String getTitle() {
                        return getString(R.string.ok_btn);
                    }

                    @Override
                    public void onClick(DialogInterface dialog) {
                        RxFile.delete(new File(mCameraPreviewHelper.getVideoRecorderHelper()
                                .getSessionFileDir()), null);
                        getActivity().finish();
                    }
                },
                new DialogUtils.ButtonAction() {
                    @Override
                    public String getTitle() {
                        return getString(R.string.cancel_btn);
                    }

                    @Override
                    public void onClick(DialogInterface dialog) {
                        getActivity().finish();
                    }
                });

        return true;
    }

    private void startRecord() {
        try {
            mCameraPreviewHelper.startRecord();

            mExPb.start();
            mCaptureBtn.setBackgroundResource(R.drawable.capture_button_pressed);
            mCaptureBtn.setText(getString(R.string.camera_stop_record));
            mCaptureBtn.setTextColor(Color.RED);
        } catch (IllegalStateException e) {
            e.printStackTrace();

            ToastUtils.t(getActivity(), "start record failed");
            DebugHelper.log("start record video failed: "
                    + Log.getStackTraceString(e));
        }
    }

    private void stopRecord() {
        if(mCameraPreviewHelper.isRecording()) {
            mCameraPreviewHelper.stopRecord();

            mExPb.stop();
            mCaptureBtn.setBackgroundResource(R.drawable.video_record_btn);
            mCaptureBtn.setText(getString(R.string.camera_start_record));
            mCaptureBtn.setTextColor(Color.WHITE);
        }
    }

    private void showMaxDurationDialog() {
        DialogUtils.showConfirmDialog(getActivity(),
                getString(R.string.dialog_max_record_duration, MAX_VIDEO_DURATION), null,
                new DialogUtils.SimpleButtonAction() {
                    @Override
                    public String getTitle() {
                        return getString(R.string.ok_btn);
                    }
                },
                null);
    }

}
