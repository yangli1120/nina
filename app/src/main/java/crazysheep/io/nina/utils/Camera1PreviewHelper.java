package crazysheep.io.nina.utils;

import android.app.Activity;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * helper for camera preview on a TextureView
 * </p> support Camera API 1
 *
 * Created by crazysheep on 16/4/3.
 */
public class Camera1PreviewHelper extends ICameraPreviewHelper {

    private Camera.Size mVideoSize;
    private Camera mCameraDevice;

    public Camera1PreviewHelper(@NonNull Activity activity, @NonNull TextureView textureView) {
        super(activity, textureView);
    }

    @Override
    public void openCamera() {
        Observable.just(true)
                .subscribeOn(HandlerScheduler.from(mBackgroundHandler))
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        try {
                            mCameraDevice = Camera.open();

                            // setup camera parameters
                            Camera.Parameters parameters = mCameraDevice.getParameters();
                            mVideoSize = CameraUtils.chooseVideoSize(
                                    parameters.getSupportedPreviewSizes());
                            parameters.setPictureSize(mVideoSize.width, mVideoSize.height);
                            Camera.Size previewSize = CameraUtils.chooseVideoSize(
                                    parameters.getSupportedPreviewSizes());
                            DebugHelper.log("openCamera(), video size: " + mVideoSize.width
                                    + "*" + mVideoSize.height + ", preview size: "
                                    + previewSize.width + "*" + previewSize.height);
                            parameters.setPreviewSize(previewSize.width, previewSize.height);
                            parameters.setPreviewFrameRate(30);
                            mCameraDevice.setDisplayOrientation(90);

                            mCameraDevice.setParameters(parameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                            DebugHelper.log("openCamera() error: " + Log.getStackTraceString(e));
                            return false;
                        }
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean)
                            startPreview();
                    }
                });
    }

    @Override
    public void startPreview() {
        DebugHelper.log("startPreview()");
        try {
            mRecorderHelper.prepareRecording(mCameraDevice);

            mCameraDevice.setPreviewTexture(mTextureView.getSurfaceTexture());
            mCameraDevice.startPreview();
            configureTransform(mVideoSize.width, mVideoSize.height);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            DebugHelper.log("startPreview(), error: " + Log.getStackTraceString(ioe));
        }
    }

    @Override
    public void closeCamera() {
        if(!Utils.isNull(mCameraDevice)) {
            mCameraDevice.stopPreview();
            mCameraDevice.release();
            mCameraDevice = null;
        }
    }

}
