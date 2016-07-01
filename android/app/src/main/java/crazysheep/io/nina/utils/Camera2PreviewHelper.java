package crazysheep.io.nina.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import crazysheep.io.nina.compat.APICompat;

/**
 * camera preview helper for camera api 2
 *
 * Created by crazysheep on 16/4/3.
 */
@TargetApi(APICompat.L)
public class Camera2PreviewHelper extends ICameraPreviewHelper {

    private Size mVideoSize;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            DebugHelper.log("state callback, onOpened");

            mCameraDevice = camera;
            startPreview();
            if(!Utils.isNull(mTextureView))
                configureTransform(mVideoSize.getWidth(), mVideoSize.getHeight());
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
            if(!Utils.isNull(mActivity) && !mActivity.isFinishing())
                mActivity.finish();
        }
    };

    public Camera2PreviewHelper(@NonNull Activity activity, @NonNull TextureView textureView) {
        super(activity, textureView);
    }

    @Override
    public void closeCamera() {
        if (!Utils.isNull(mCameraDevice)) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    public void openCamera() {
        CameraManager cameraMgr = (CameraManager) mActivity.getSystemService(
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
            configureTransform(mVideoSize.getWidth(), mVideoSize.getHeight());

            cameraMgr.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException cae) {
            cae.printStackTrace();

            DebugHelper.log("openCamera(), error: " + Log.getStackTraceString(cae));
        }
    }

    @Override
    public void startPreview() {
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
                    DebugHelper.toast(mActivity, "create capture session failed");
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

    @Override
    public void stopRecord() {
        // camera api2 issue, see{@link https://github.com/googlesamples/android-Camera2Video/issues/2}
        try {
            // Abort all pending captures.
            mPreviewSession.abortCaptures();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        super.stopRecord();
    }
}
