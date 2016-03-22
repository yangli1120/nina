package crazysheep.io.nina.fragment;

import android.annotation.TargetApi;
import android.content.Context;
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
import android.os.Handler;
import android.os.HandlerThread;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.R;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.utils.Camera2Utils;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.Utils;

/**
 * use camera2 api capture video
 *
 * Created by crazysheep on 16/3/17.
 */
@TargetApi(APICompat.L)
public class Camera2CaptureVideoFragment extends Fragment
        implements TextureView.SurfaceTextureListener {

    @Bind(R.id.video_auto_fit_tv) TextureView mTextureView;
    @Bind(R.id.action_ll) View mActionLl;
    @Bind(R.id.capture_tv) TextView mCaptureTv;

    private Size mVideoSize;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    private boolean isRecordingVideo = false;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            DebugHelper.log("state callback, onOpended");

            mCameraDevice = camera;
            startPreview();
            if(!Utils.isNull(mTextureView))
                configureTransform();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
            if(!Utils.isNull(getActivity()) && !getActivity().isFinishing())
                getActivity().finish();
        }
    };

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

        startBackgroundThread();
        if(mTextureView.isAvailable())
            openCamera();
        else
            mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

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
    @OnClick(R.id.capture_tv)
    protected void clickCapture() {
        DebugHelper.toast(getActivity(), isRecordingVideo ? "stop" : "start");
        isRecordingVideo = !isRecordingVideo;
        mCaptureTv.setPressed(isRecordingVideo);
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
            mVideoSize = Camera2Utils.chooseVideoSize(
                    configurationMap.getOutputSizes(MediaRecorder.class));
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
            // TODO setup recorder
            // setupMediaRecorder()

            mTextureView.getSurfaceTexture().setDefaultBufferSize(
                    mVideoSize.getWidth(), mVideoSize.getHeight());

            Surface previewSurface = new Surface(mTextureView.getSurfaceTexture());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.addTarget(previewSurface);

            List<Surface> surfaceList = new ArrayList<>();
            surfaceList.add(previewSurface);

            // TODO add recorder surface
            // surfaceList.add(mMediaRecorder.getSurface());
            // mPreviewBuilder.addTarget(mMediaRecorder.getSurface());

            mCameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
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
                public void onConfigureFailed(CameraCaptureSession session) {
                    DebugHelper.toast(getActivity(), "create capture session failed");
                }
            }, mBackgroundHandler);
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
        DebugHelper.log("configureTransform(), viewRectF: " + viewRectF
                + ", bufferRectF: " + bufferRectF);

        mTextureView.setTransform(matrix);
    }

}
