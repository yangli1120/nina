package crazysheep.io.nina.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;

import crazysheep.io.nina.utils.DebugHelper;

/**
 * custom texture view focus on play a video
 *
 * Created by crazysheep on 16/3/23.
 */
public class TextureVideoView extends TextureView implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener {

    public static final int SCALE_TYPE_CENTER_CROP = 1;
    public static final int SCALE_TYPE_FILL_XY = 2;
    public static final int SCALE_TYPE_TOP = 3;
    public static final int SCALE_TYPE_BOTTOM = 4;

    private int mScaleType = SCALE_TYPE_FILL_XY;

    public static final int PLAY_MODE_ONE = 1;
    public static final int PLAY_MODE_LOOP = 2;

    private MediaPlayer mMediaPlayer;
    private Surface mSurface;
    private int mPlayMode = PLAY_MODE_LOOP;

    private MediaMetadataRetriever mMediaMetadataRetriever;

    private File mVideoFile;
    private int mVideoWidth;
    private int mVideoHeight;

    public TextureVideoView(Context context) {
        super(context);
        init();
    }

    public TextureVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextureVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextureVideoView(Context context, AttributeSet attrs, int defStyleAttr,
                            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mMediaMetadataRetriever = new MediaMetadataRetriever();
        setSurfaceTextureListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {}

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        DebugHelper.log("onSurfaceTextureAvailable()");
        mSurface = new Surface(surface);
        if(null != mVideoFile)
            setupMediaPlayer();

        configureTransform();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configureTransform();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    private void setupMediaPlayer() {
        DebugHelper.log("setupMediaPlayer");
        if(null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setLooping(mPlayMode == PLAY_MODE_LOOP);

        if(null != mVideoFile && null != mSurface) {
            try {
                mMediaPlayer.setDataSource(mVideoFile.getAbsolutePath());
                mMediaPlayer.setSurface(mSurface);
                mMediaPlayer.prepareAsync();
            } catch (IOException ioe) {
                ioe.printStackTrace();

                DebugHelper.log("TextureVideo.prepareVideo() exception: "
                        + Log.getStackTraceString(ioe));
            }
        }
    }

    public void setVideo(@NonNull File file) {
        if(!file.isFile())
            throw new RuntimeException(
                    String.format("TextureVideoView.setVideo(), video file %s is invalid",
                            file.getAbsolutePath()));
        try {
            mMediaMetadataRetriever.setDataSource(file.getAbsolutePath());
            String mimeType = mMediaMetadataRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            if(!TextUtils.isEmpty(mimeType) && !mimeType.startsWith("video/"))
                throw new RuntimeException(
                        String.format("TextureVideoView.setVideo(), file %s is not a video",
                                file.getAbsolutePath()));
            String width = mMediaMetadataRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            if(TextUtils.isDigitsOnly(width))
                mVideoWidth = Integer.parseInt(width);
            String height = mMediaMetadataRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if(TextUtils.isDigitsOnly(height))
                mVideoHeight = Integer.parseInt(height);
        } catch (IllegalArgumentException lae) {
            lae.printStackTrace();
        }

        DebugHelper.log("setVideo(), video: " + file.getAbsolutePath());
        mVideoFile = file;
        if(null != mSurface)
            setupMediaPlayer();
    }

    public void setScaleType(int scaleType) {
        mScaleType = scaleType;
    }

    public boolean isPlaying() {
        return null != mMediaPlayer && mMediaPlayer.isPlaying();
    }

    public void play() {
        if(null != mMediaPlayer)
            mMediaPlayer.start();
    }

    public void pause() {
        if(null != mMediaPlayer)
            mMediaPlayer.pause();
    }

    private void configureTransform() {
        Matrix matrix = new Matrix();
        float scale = Math.max(getWidth() * 1f / mVideoWidth,
                getHeight() * 1f / mVideoHeight);
        RectF viewRectF = new RectF(0, 0, getWidth(), getHeight());
        RectF bufferRectF = new RectF(0, 0, mVideoWidth, mVideoHeight);
        switch (mScaleType) {
            case SCALE_TYPE_CENTER_CROP: {
                matrix.preTranslate(viewRectF.centerX() - bufferRectF.centerY(),
                        viewRectF.centerY() - bufferRectF.centerX());
                matrix.preScale(bufferRectF.height() * 1f / viewRectF.width(),
                        bufferRectF.width() * 1f / viewRectF.height());
                matrix.postScale(scale, scale, viewRectF.centerX(), viewRectF.centerY());
            }break;

            case SCALE_TYPE_TOP: {
                matrix.preScale(bufferRectF.height() * 1f / viewRectF.width(),
                        bufferRectF.width() * 1f / viewRectF.height());
                matrix.postScale(scale, scale, viewRectF.centerX(), viewRectF.centerY());
            }break;

            case SCALE_TYPE_BOTTOM: {
                matrix.preTranslate(0, viewRectF.bottom - bufferRectF.bottom);
                matrix.preScale(bufferRectF.height() * 1f / viewRectF.width(),
                        bufferRectF.width() * 1f / viewRectF.height());
                matrix.postScale(scale, scale, viewRectF.centerX(), viewRectF.centerY());
            }break;

            case SCALE_TYPE_FILL_XY:
            default: {
                // nothing
            }
        }

        setTransform(matrix);
    }

    public String getVideoFile() {
        return null != mVideoFile ? mVideoFile.getAbsolutePath() : null;
    }

    private void release() {
        if(null != mSurface)
            mSurface.release();
        if(null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
