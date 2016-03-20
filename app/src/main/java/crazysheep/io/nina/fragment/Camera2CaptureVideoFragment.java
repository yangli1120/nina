package crazysheep.io.nina.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.R;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.grafika.FullFrameRect;
import crazysheep.io.nina.grafika.Texture2dProgram;
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
        implements SurfaceTexture.OnFrameAvailableListener {

    public static int TARGET_PREVIEW_WIDTH = 1280;
    public static int TARGET_PREVIEW_HEIGHT = 960;

    @Bind(R.id.video_auto_fit_tv) GLSurfaceView mGLSurface;
    @Bind(R.id.action_ll) View mActionLl;
    @Bind(R.id.capture_tv) TextView mCaptureTv;

    private Size mVideoSize;
    private Size mPreviewSize;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private SurfaceTexture mPreviewSurfaceTexture;

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    private boolean isCameraOpened = false;
    private boolean isPreviewSurfaceCreated = false;

    private static class CameraHandler extends Handler {

        public static final int MSG_SET_SURFACE_TEXTURE = 1;

        private WeakReference<? extends Fragment> mFragmentRef;

        public CameraHandler(@NonNull Camera2CaptureVideoFragment fragment) {
            mFragmentRef = new WeakReference<>(fragment);
        }

        public void invalidHandler() {
            mFragmentRef.clear();
        }

        @Override
        public void handleMessage(Message msg) {
            if(Utils.isNull(mFragmentRef.get()) || Utils.isNull(mFragmentRef.get().getActivity())
                    || mFragmentRef.get().getActivity().isFinishing())
                return;

            switch (msg.what) {
                case MSG_SET_SURFACE_TEXTURE: {
                    // surface texture created, notify if can start preview
                    ((Camera2CaptureVideoFragment) mFragmentRef.get())
                            .setPreviewSurfaceTexture((SurfaceTexture) msg.obj);
                }break;

                default:
                    throw new RuntimeException("WTF, unknow message type");
            }
        }
    }
    private CameraHandler mCameraHandler;
    private CameraRenderer mCameraRenderer;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            DebugHelper.log("state callback, onOpended");

            isCameraOpened = true;
            mCameraDevice = camera;
            ensureIfCanStartPreview();
            if(!Utils.isNull(mGLSurface))
                configureTransform(mGLSurface.getWidth(), mGLSurface.getHeight());
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            isCameraOpened = false;
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            isCameraOpened = false;
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

        mGLSurface.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mGLSurface.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ViewGroup.LayoutParams params = mGLSurface.getLayoutParams();
                params.height = Math.round(mGLSurface.getMeasuredWidth() * 4f / 3);
                mGLSurface.setLayoutParams(params);
            }
        });
        mGLSurface.setEGLContextClientVersion(2);
        mCameraHandler = new CameraHandler(this);
        mCameraRenderer = new CameraRenderer(mCameraHandler);
        mGLSurface.setRenderer(mCameraRenderer);
        mGLSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        TARGET_PREVIEW_WIDTH = getResources().getDisplayMetrics().widthPixels;
        TARGET_PREVIEW_HEIGHT = Math.round(TARGET_PREVIEW_WIDTH * 3f / 4);

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        startBackgroundThread();
        openCamera(TARGET_PREVIEW_WIDTH, TARGET_PREVIEW_HEIGHT);
        mGLSurface.onResume();
        mGLSurface.queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.setCameraPreviewSize(mPreviewSize.getWidth(),
                        mPreviewSize.getHeight());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        closeCamera();
        stopBackgroundThread();
        mPreviewSurfaceTexture.release();
        mPreviewSurfaceTexture = null;
        mGLSurface.queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.notifyPausing();
            }
        });
        mGLSurface.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCameraHandler.invalidHandler();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.capture_tv)
    protected void clickCapture() {
        DebugHelper.toast(getActivity(), "start capture");
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // notify CameraRenderer.onDrawFrame()
        mGLSurface.requestRender();
    }

    protected void setPreviewSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
        surfaceTexture.setOnFrameAvailableListener(this);
        DebugHelper.log("setPreviewSurfaceTexture()");
        isPreviewSurfaceCreated = true;
        mPreviewSurfaceTexture = surfaceTexture;
        ensureIfCanStartPreview();
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

            cameraMgr.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException cae) {
            cae.printStackTrace();

            DebugHelper.log("openCamera(), error: " + Log.getStackTraceString(cae));
        }
    }

    // step 2, start preview
    private void ensureIfCanStartPreview() {
        if(Utils.isNull(mCameraDevice) || Utils.isNull(mGLSurface)
                || Utils.isNull(mPreviewSize))
            return;

        if(!isCameraOpened || !isPreviewSurfaceCreated)
            return;

        DebugHelper.log("ensureIfCanStartPreview()");
        try {
            // TODO setup recorder
            // setupMediaRecorder()

            mPreviewSurfaceTexture.setDefaultBufferSize(
                    mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface previewSurface = new Surface(mPreviewSurfaceTexture);
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

            DebugHelper.log("ensureIfCanStartPreview(), error: " + Log.getStackTraceString(cae));
        }
    }

    // transform video size to preview size
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mGLSurface || null == mPreviewSize || null == activity) {
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
        // TODO mGLSurface.setTransform(matrix);
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
        isCameraOpened = false;

        if (!Utils.isNull(mCameraDevice)) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    static class CameraRenderer implements GLSurfaceView.Renderer {

        private CameraHandler mCameraHandler;

        private FullFrameRect mFullFrameRect;
        private SurfaceTexture mSurfaceTexture;
        private int mTextureId;

        private boolean mIncomingSizeUpdated = false;
        private int mIncomingWidth;
        private int mIncomingHeight;

        private final float[] mSTMatrix = new float[16];

        public CameraRenderer(CameraHandler cameraHandler) {
            mCameraHandler = cameraHandler;

            mIncomingWidth = -1;
            mIncomingHeight = -1;
            mIncomingSizeUpdated = false;
        }

        public void setCameraPreviewSize(int width, int height) {
            mIncomingWidth = width;
            mIncomingHeight = height;
            mIncomingSizeUpdated = true;
        }

        public void notifyPausing() {
            if(!Utils.isNull(mSurfaceTexture)) {
                mSurfaceTexture.release();
                mSurfaceTexture = null;
            }
            if(!Utils.isNull(mFullFrameRect)) {
                mFullFrameRect.release(false);
                mFullFrameRect = null;
            }
            mIncomingWidth = mIncomingHeight = -1;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mSurfaceTexture.updateTexImage();

            if(mIncomingWidth <= 0 || mIncomingHeight <= 0)
                return;
            // TODO use filter if could

            if(mIncomingSizeUpdated) {
                mFullFrameRect.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
                mIncomingSizeUpdated = false;
            }

            // draw video frame
            mSurfaceTexture.getTransformMatrix(mSTMatrix);
            mFullFrameRect.drawFrame(mTextureId, mSTMatrix);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            DebugHelper.log("CameraRenderer.onSurfaceCreated()");

            // TODO surface texture is ready, notify to start camera preview
            mFullFrameRect = new FullFrameRect(
                    new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
            mTextureId = mFullFrameRect.createTextureObject();
            mSurfaceTexture = new SurfaceTexture(mTextureId);

            mCameraHandler.sendMessage(
                    mCameraHandler.obtainMessage(CameraHandler.MSG_SET_SURFACE_TEXTURE,
                            mSurfaceTexture));
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            DebugHelper.log("CameraRenderer.onSurfaceChanged(), width: " + width + ", height: " + height);
            gl.glViewport(0, 0, width, height);
        }

    }

}
