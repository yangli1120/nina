package crazysheep.io.nina.utils;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import crazysheep.io.nina.R;
import crazysheep.io.nina.compat.APICompat;

/**
 * interface define camera preview logic
 *
 * Created by crazysheep on 16/4/3.
 */
public abstract class ICameraPreviewHelper implements TextureView.SurfaceTextureListener {

    ///////////////////// abstract api /////////////////////////////

    public abstract void openCamera();
    public abstract void startPreview();
    public abstract void closeCamera();

    //////////////////////////////////////////////////////////////

    public static ICameraPreviewHelper newInstance(@NonNull Activity activity,
                                                   @NonNull TextureView textureView) {
        if(APICompat.api21())
            return new Camera2PreviewHelper(activity, textureView);
        else
            return new Camera1PreviewHelper(activity, textureView);
    }

    private HandlerThread mBackgroundHandlerThread;
    protected Handler mBackgroundHandler;

    protected Activity mActivity;
    protected TextureView mTextureView;
    protected VideoRecorderHelper mRecorderHelper;

    public ICameraPreviewHelper(@NonNull Activity activity, @NonNull TextureView textureView) {
        mActivity = activity;
        mTextureView = textureView;

        File outputDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                activity.getString(R.string.app_name));
        if(!outputDir.isDirectory()) {
            try {
                FileUtils.forceDelete(outputDir);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        if(!outputDir.exists())
            outputDir.mkdirs();
        mRecorderHelper = new VideoRecorderHelper(mActivity,
                new VideoRecorderHelper.RecorderConfig.Builder()
                        .outputDir(outputDir)
                        .videoSource(APICompat.api21() ? MediaRecorder.VideoSource.SURFACE
                                : MediaRecorder.VideoSource.CAMERA)
                        .build());
    }

    public VideoRecorderHelper getVideoRecorderHelper() {
        return mRecorderHelper;
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera();
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        closeCamera();
        return true;
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // TODO configure texture matrix
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    // attach activity or fragment life circle
    public void onResume() {
        startBackgroundThread();
        mRecorderHelper.onResume();
        if(mTextureView.isAvailable()) {
            mTextureView.setSurfaceTextureListener(null);
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    public void onPause() {
        mRecorderHelper.onPause();
        closeCamera();
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("I work for no money!");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        if(APICompat.api18())
            mBackgroundHandlerThread.quitSafely();
        else
            mBackgroundHandlerThread.quit();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public void startRecord() {
        try {
            mRecorderHelper.startRecording();
        } catch (IllegalStateException e) {
            e.printStackTrace();

            ToastUtils.t(mActivity, "start record failed");
            DebugHelper.log("start record video failed: " + Log.getStackTraceString(e));
        }
    }

    public void stopRecord() {
        mRecorderHelper.stopRecording();
        startPreview();
    }

    public boolean isRecording() {
        return !Utils.isNull(mRecorderHelper) && mRecorderHelper.isRecording();
    }

    /*
     * use Matrix transform preview surface texture to square
     *
     * video preview from camera is height : width = 4 : 3, yep, not width : height = 4 : 3,
     * if video size is [640 * 480], so that mean width is 480, height is 640, not same as UI,
     * because UI width is 640, height is 480
     */
    protected final void configureTransform(int width, int height) {
        if (Utils.isNull(mTextureView) || width <= 0 || height <= 0)
            return;

        float scaleX = mTextureView.getWidth() * 1f / width;
        float scaleY = mTextureView.getHeight() * 1f / height;
        float maxScale = Math.max(scaleX, scaleY);

        Matrix matrix = new Matrix();
        RectF viewRectF = new RectF(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
        RectF bufferRectF = new RectF(0, 0, width, height);
        matrix.preTranslate(viewRectF.centerX() - bufferRectF.centerY(),
                viewRectF.centerY() - bufferRectF.centerX());
        matrix.preScale(bufferRectF.height() * 1f / viewRectF.width(),
                bufferRectF.width() * 1f / viewRectF.height());
        matrix.postScale(maxScale, maxScale, viewRectF.centerX(), viewRectF.centerY());

        mTextureView.setTransform(matrix);
    }

}
