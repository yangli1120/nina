package crazysheep.io.nina.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
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
import crazysheep.io.nina.widget.AutoFitTextureView;

/**
 * use camera2 api capture video
 *
 * Created by crazysheep on 16/3/17.
 */
@TargetApi(APICompat.L)
public class Camera2CaptureVideoFragment extends Fragment {

    @Bind(R.id.video_auto_fit_tv) AutoFitTextureView mVideoAutoFitTv;
    @Bind(R.id.action_ll) View mActionLl;
    @Bind(R.id.capture_tv) TextView mCaptureTv;

    private Size mVideoSize;
    private Size mPreviewSize;

    private MediaRecorder mMediaRecorder;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            DebugHelper.log("state callback, onOpended");
            startPreview();
            if(!Utils.isNull(mVideoAutoFitTv))
                configureTransform(mVideoAutoFitTv.getWidth(), mVideoAutoFitTv.getHeight());
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

        mVideoAutoFitTv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoAutoFitTv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float h_w_factor;
                if (getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT)
                    h_w_factor = 4 * 1f / 3; // h : w = 4 : 3
                else
                    h_w_factor = 3 * 1f / 4; // h : w = 3 : 4;
                ViewGroup.LayoutParams params = mVideoAutoFitTv.getLayoutParams();
                params.height = Math.round(mVideoAutoFitTv.getMeasuredWidth() * h_w_factor);
                mVideoAutoFitTv.setLayoutParams(params);
            }
        });
        mActionLl.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mActionLl.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ViewGroup.LayoutParams params = mActionLl.getLayoutParams();
                params.height = getResources().getDisplayMetrics().heightPixels
                        - getResources().getDisplayMetrics().widthPixels;
                mActionLl.setLayoutParams(params);
            }
        });

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        startBackgroundThread();
        if(mVideoAutoFitTv.isAvailable()) {
            openCamera(mVideoAutoFitTv.getWidth(), mVideoAutoFitTv.getHeight());
        } else {
            mVideoAutoFitTv.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                                      int width, int height) {
                    DebugHelper.log("onSurfaceTextureAvailable, width: " + width + ", height: " + height);
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                        int width, int height) {
                    DebugHelper.log("onSurfaceTextureSizeChanged, width: " + width + ", height: " + height);
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        closeCamera();
        stopBackgroundThread();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.capture_tv)
    protected void clickCapture() {
        DebugHelper.toast(getActivity(), "start capture");
    }

    // step 1, open camera
    private void openCamera(int width, int height) {
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
            mPreviewSize = Camera2Utils.chooseOptimalSize(
                    configurationMap.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);
            DebugHelper.log("openCamera(), video size: " + mVideoSize + ", preview size: " + mPreviewSize);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mVideoAutoFitTv.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mVideoAutoFitTv.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(width, height);
            DebugHelper.log("openCamera(), config transform, texture.width: " + mVideoAutoFitTv.getWidth()
                    + ", texture.height: " + mVideoAutoFitTv.getHeight());

            // mMediaRecorder = new MediaRecorder();
            cameraMgr.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException cae) {
            cae.printStackTrace();

            DebugHelper.log("openCamera(), error: " + Log.getStackTraceString(cae));
        }
    }

    // step 2, start preview
    private void startPreview() {
        if(Utils.isNull(mCameraDevice) || Utils.isNull(mVideoAutoFitTv.getSurfaceTexture())
                || Utils.isNull(mPreviewSize))
            return;

        try {
            // TODO setup recorder
            // setupMediaRecorder()

            mVideoAutoFitTv.getSurfaceTexture().setDefaultBufferSize(
                    mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface previewSurface = new Surface(mVideoAutoFitTv.getSurfaceTexture());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
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

    // transform video size to preview size
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mVideoAutoFitTv || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mVideoAutoFitTv.setTransform(matrix);
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
        if (!Utils.isNull(mMediaRecorder)) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

}
