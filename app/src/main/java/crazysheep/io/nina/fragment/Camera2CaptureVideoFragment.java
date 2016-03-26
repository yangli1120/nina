package crazysheep.io.nina.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.BaseActivity;
import crazysheep.io.nina.R;
import crazysheep.io.nina.RecordVideoPreviewActivity;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.io.RxFile;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.Camera2Utils;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.DialogUtils;
import crazysheep.io.nina.utils.RxVideo;
import crazysheep.io.nina.utils.ToastUtils;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.utils.VideoRecorderHelper;

/**
 * use camera2 api capture video
 *
 * Created by crazysheep on 16/3/17.
 */
@TargetApi(APICompat.L)
public class Camera2CaptureVideoFragment extends Fragment
        implements TextureView.SurfaceTextureListener, BaseActivity.OnBackPressedListener {

    private static final int REQUEST_VIDEO_PREVIEW = 1;

    @Bind(R.id.video_auto_fit_tv) TextureView mTextureView;
    @Bind(R.id.action_ll) View mActionLl;
    @Bind(R.id.capture_btn) Button mCaptureBtn;
    @Bind(R.id.capture_done_btn) Button mCaptureDoneBtn;

    private Size mVideoSize;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    private boolean isRecordingVideo = false;
    private VideoRecorderHelper mRecorderHelper;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            DebugHelper.log("state callback, onOpened");

            mCameraDevice = camera;
            startPreview();
            if(!Utils.isNull(mTextureView))
                configureTransform();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
            if(!Utils.isNull(getActivity()) && !getActivity().isFinishing())
                getActivity().finish();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File outputDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                getString(R.string.app_name));
        if(!outputDir.isDirectory()) {
            try {
                FileUtils.forceDelete(outputDir);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        if(!outputDir.exists())
            outputDir.mkdirs();
        mRecorderHelper = new VideoRecorderHelper(getActivity(),
                new VideoRecorderHelper.RecorderConfig.Builder()
                        .outputDir(outputDir)
                        .build());
    }

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

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mRecorderHelper.onResume();
        startBackgroundThread();
        if(mTextureView.isAvailable())
            openCamera();
        else
            mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mRecorderHelper.onPause();
        closeCamera();
        stopBackgroundThread();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configureTransform();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.capture_btn)
    protected void clickCapture() {
        DebugHelper.toast(getActivity(), isRecordingVideo ? "stop" : "start");
        isRecordingVideo = !isRecordingVideo;
        mCaptureBtn.setBackgroundResource(isRecordingVideo
                ? R.drawable.capture_button_pressed : R.drawable.video_record_btn);
        mCaptureBtn.setText(getString(isRecordingVideo
                ? R.string.camera_stop_record : R.string.camera_start_record));
        mCaptureBtn.setTextColor(isRecordingVideo ? Color.RED : Color.WHITE);

        if(isRecordingVideo) {
            try {
                mRecorderHelper.startRecording();
            } catch (IllegalStateException e) {
                e.printStackTrace();

                DebugHelper.log("start record video failed: " + Log.getStackTraceString(e));
            }
        } else {
            mRecorderHelper.stopRecording();
            startPreview(); // start preview again
        }
    }

    @SuppressWarnings("unused, unchecked")
    @OnClick(R.id.capture_done_btn)
    protected void clickNextStep() {
        if(Utils.size(mRecorderHelper.getRecordedFiles()) <= 0) {
            ToastUtils.t(getActivity(), getString(R.string.toast_not_recorded_video_file));

            return;
        }

        DebugHelper.log(String.format("recorded files: [ %s ]",
                mRecorderHelper.getRecordedFilesPath()));

        final String targetFilePath = new File(mRecorderHelper.getSessionFileDir(), "final.mp4")
                .getAbsolutePath();
        if(Utils.size(mRecorderHelper.getRecordedFilesPath()) > 1)
            RxVideo.merge(mRecorderHelper.getRecordedFilesPath(),
                    targetFilePath,
                    new RxVideo.Callback() {
                        @Override
                        public void onSuccess(List<String> sources, String targetFilePath) {
                            startForResult(targetFilePath);
                        }

                        @Override
                        public void onFailed(String err) {
                            DebugHelper.log(err);
                        }
                    });
        else if(Utils.size(mRecorderHelper.getRecordedFilesPath()) == 1)
            RxFile.copy(mRecorderHelper.getRecordedFiles().get(0).getAbsolutePath(), targetFilePath,
                    new RxFile.Callback() {
                        @Override
                        public void onSuccess() {
                            startForResult(targetFilePath);
                        }

                        @Override
                        public void onFailed(String err) {
                            DebugHelper.log(err);
                        }
                    });
        else
            ToastUtils.t(getActivity(), getString(R.string.toast_not_recorded_video_file));
    }

    private void startForResult(String targetFilePath) {
        ActivityUtils.startResult(Camera2CaptureVideoFragment.this, REQUEST_VIDEO_PREVIEW,
                ActivityUtils.prepare(getActivity(), RecordVideoPreviewActivity.class)
                        .putExtra(BundleConstants.EXTRA_VIDEO_RECORD_FILE, targetFilePath));
    }

    // step 1, open camera
    private void openCamera() {
        CameraManager cameraMgr = (CameraManager)getActivity().getSystemService(
                Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraMgr.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = cameraMgr.getCameraCharacteristics(
                    cameraId);
            StreamConfigurationMap configurationMap = cameraCharacteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if(!Utils.isNull(configurationMap))
                mVideoSize = Camera2Utils.chooseVideoSize(
                        configurationMap.getOutputSizes(MediaRecorder.class));
            else
                mVideoSize = new Size(640, 480);
            DebugHelper.log("openCamera(), video size: " + mVideoSize);
            configureTransform();

            cameraMgr.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException cae) {
            cae.printStackTrace();

            DebugHelper.log("openCamera(), error: " + Log.getStackTraceString(cae));
        }
    }

    // step 2, start preview
    private void startPreview() {
        if(Utils.isNull(mCameraDevice) || Utils.isNull(mTextureView))
            return;

        DebugHelper.log("startPreview()");
        try {
            // setup recorder
            mRecorderHelper.prepareRecording();

            mTextureView.getSurfaceTexture().setDefaultBufferSize(
                    mVideoSize.getWidth(), mVideoSize.getHeight());

            Surface previewSurface = new Surface(mTextureView.getSurfaceTexture());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mPreviewBuilder.addTarget(previewSurface);

            List<Surface> surfaceList = new ArrayList<>();
            surfaceList.add(previewSurface);
            // add recorder surface
            surfaceList.add(mRecorderHelper.getSurface());
            mPreviewBuilder.addTarget(mRecorderHelper.getSurface());

            mCameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;

                    try {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE,
                                CameraMetadata.CONTROL_MODE_AUTO);
                        mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null,
                                mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();

                        DebugHelper.log("camera access exception: " + Log.getStackTraceString(e));
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    DebugHelper.toast(getActivity(), "create capture session failed");
                }
            }, mBackgroundHandler);
        } catch (IOException ioe) {
            ioe.printStackTrace();

            DebugHelper.log("startPreview, setup media recorder failed: "
                    + Log.getStackTraceString(ioe));
        } catch (CameraAccessException cae) {
            cae.printStackTrace();

            DebugHelper.log("startPreview(), error: " + Log.getStackTraceString(cae));
        }
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("I work for no money!");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    private void closeCamera() {
        if (!Utils.isNull(mCameraDevice)) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    /*
     * use Matrix transform preview surface texture to square
     *
     * video preview from camera is height : width = 4 : 3, yep, not width : height = 4 : 3,
     * if video size is [640 * 480], so that mean width is 480, height is 640, not same as UI,
     * because UI width is 640, height is 480
     */
    private void configureTransform() {
        if (Utils.isNull(mTextureView) || Utils.isNull(mVideoSize)
                || Utils.isNull(getActivity()))
            return;

        float scaleX = mTextureView.getWidth() * 1f / mVideoSize.getWidth();
        float scaleY = mTextureView.getHeight() * 1f / mVideoSize.getHeight();
        float maxScale = Math.max(scaleX, scaleY);

        Matrix matrix = new Matrix();
        RectF viewRectF = new RectF(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
        RectF bufferRectF = new RectF(0, 0, mVideoSize.getWidth(), mVideoSize.getHeight());
        matrix.preTranslate(viewRectF.centerX() - bufferRectF.centerY(),
                viewRectF.centerY() - bufferRectF.centerX());
        matrix.preScale(bufferRectF.height() * 1f / viewRectF.width(),
                bufferRectF.width() * 1f / viewRectF.height());
        matrix.postScale(maxScale, maxScale, viewRectF.centerX(), viewRectF.centerY());

        mTextureView.setTransform(matrix);
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
                        RxFile.delete(new File(mRecorderHelper.getSessionFileDir()), null);
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
}
