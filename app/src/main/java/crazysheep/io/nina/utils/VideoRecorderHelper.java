package crazysheep.io.nina.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.prefs.UserPrefs;

/**
 * video recorder helper
 *
 * Created by crazysheep on 16/3/22.
 */
public class VideoRecorderHelper {

    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int VIDEO_SOURCE = MediaRecorder.VideoSource.SURFACE;
    public static final int OUTPUT_FORMAT = MediaRecorder.OutputFormat.MPEG_4;
    public static final int VIDEO_ENCODING_BIT_RATE = 1000 * 1000 * 4;
    public static final int VIDEO_FRAME_RATE = 30;
    public static final int[] VIDEO_SIZE = new int[] {640, 480};
    public static final int VIDEO_ENCODER = MediaRecorder.VideoEncoder.H264;
    public static final int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC;

    /////////////////// config ///////////////////////////

    public static class RecorderConfig {

        private int audioSource = AUDIO_SOURCE;
        private int videoSource = VIDEO_SOURCE;
        private int outputFormat = OUTPUT_FORMAT;
        private File outputDir;
        private int videoEncodingBitRate = VIDEO_ENCODING_BIT_RATE;
        private int videoFrameRate = VIDEO_FRAME_RATE;
        private int[] videoSize = VIDEO_SIZE;
        private int videoEncoder = VIDEO_ENCODER;
        private int audioEncoder = AUDIO_ENCODER;

        private RecorderConfig() {}

        public static class Builder {

            private int audioSource = AUDIO_SOURCE;
            private int videoSource = VIDEO_SOURCE;
            private int outputFormat = OUTPUT_FORMAT;
            private File outputDir;
            private int videoEncodingBitRate = VIDEO_ENCODING_BIT_RATE;
            private int videoFrameRate = VIDEO_FRAME_RATE;
            private int[] videoSize = VIDEO_SIZE;
            private int videoEncoder = VIDEO_ENCODER;
            private int audioEncoder = AUDIO_ENCODER;

            public Builder() {
            }

            public Builder audioSource(int audioSource) {
                this.audioSource = audioSource;
                return this;
            }

            public Builder videoSource(int videoSource) {
                this.videoSource = videoSource;
                return this;
            }

            public Builder outputFormat(int outputFormat) {
                this.outputFormat = outputFormat;
                return this;
            }

            public Builder outputDir(@NonNull File outputDir) {
                if(!outputDir.isDirectory())
                    throw new RuntimeException(
                            "RecorderConfig.Builder.outputDir(), outputDir must be directory");
                this.outputDir = outputDir;
                return this;
            }

            public Builder videoEncodingBitRate(int videoEncodingBitRate) {
                this.videoEncodingBitRate = videoEncodingBitRate;
                return this;
            }

            public Builder videoFrameRate(int videoFrameRate) {
                this.videoFrameRate = videoFrameRate;
                return this;
            }

            public Builder videoSize(int[] videoSize) {
                this.videoSize = videoSize;
                return this;
            }

            public Builder videoEncoder(int videoEncoder) {
                this.videoEncoder = videoEncoder;
                return this;
            }

            public Builder audioEncoder(int audioEncoder) {
                this.audioEncoder = audioEncoder;
                return this;
            }

            public RecorderConfig build() {
                RecorderConfig config = new RecorderConfig();

                config.audioEncoder = this.audioEncoder;
                config.audioSource = this.audioSource;
                config.outputDir = this.outputDir;
                config.outputFormat = this.outputFormat;

                config.videoSize = this.videoSize;
                config.videoEncoder = this.videoEncoder;
                config.videoEncodingBitRate = this.videoEncodingBitRate;
                config.videoSource = this.videoSource;
                config.videoFrameRate = this.videoFrameRate;

                if(Utils.isNull(outputDir) || !outputDir.isDirectory())
                    throw new RuntimeException("VideoRecorderHelper.RecorderConfig.Builder, " +
                            "outputDir must been directory or can not been null");

                return config;
            }

        }

    }

    //////////////////////////////////////////////////////

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private RecorderConfig mConfig;
    private MediaRecorder mMediaRecorder;
    private WeakReference<Activity> mActivityRefs;
    private File mSessionDir; // session directory to save current video record files

    private List<File> mRecordedFiles;
    private int mVideoPartIndex = 0;

    private boolean isRecording = false;

    public VideoRecorderHelper(@NonNull Activity activity, @NonNull RecorderConfig config) {
        mActivityRefs = new WeakReference<>(activity);
        mConfig = config;

        mVideoPartIndex = 0;

        // create new video recorder session directory to save current record video files
        String userScreenName = new UserPrefs(activity).getUserScreenName();
        mSessionDir = new File(mConfig.outputDir, String.format("%s_%s",
                createSessionDirNamePrefix(mConfig.outputDir, userScreenName), userScreenName));
        mSessionDir.mkdir();
        mRecordedFiles = new ArrayList<>();
    }

    private String createSessionDirNamePrefix(File outputDir, String userScreenName) {
        String prefix = TimeUtils.formatDate(System.currentTimeMillis(), "yyyy_MM_dd_HH_mm");
        if(new File(outputDir, String.format("%s_%s", prefix, userScreenName)).exists())
            prefix = prefix + "_1_";
        return prefix;
    }

    public String getSessionFileDir() {
        return mSessionDir.getAbsolutePath();
    }

    public List<File> getRecordedFiles() {
        // last file is invalid, do not return
        ArrayList<File> files = new ArrayList<>();
        for(File file : mRecordedFiles)
            files.add(file);
        if(Utils.size(files) >= 1)
            files.remove(files.size() - 1);

        return files;
    }

    public List<String> getRecordedFilesPath() {
        ArrayList<String> filepaths = new ArrayList<>();
        for(File file : mRecordedFiles)
            filepaths.add(file.getAbsolutePath());
        if(Utils.size(filepaths) >= 1)
            filepaths.remove(filepaths.size() - 1);

        return filepaths;
    }

    @TargetApi(APICompat.L)
    public Surface getSurface() {
        return mMediaRecorder.getSurface();
    }

    public void prepareRecording() throws IOException {
        mMediaRecorder.setAudioSource(mConfig.audioSource);
        mMediaRecorder.setVideoSource(mConfig.videoSource);
        mMediaRecorder.setOutputFormat(mConfig.outputFormat);
        File newRecordFile = new File(mSessionDir, String.format("record_part_%s.mp4nina",
                mVideoPartIndex));
        if(!mRecordedFiles.contains(newRecordFile))
            mRecordedFiles.add(newRecordFile);
        mMediaRecorder.setOutputFile(newRecordFile.getAbsolutePath());
        mMediaRecorder.setVideoEncodingBitRate(mConfig.videoEncodingBitRate);
        mMediaRecorder.setVideoFrameRate(mConfig.videoFrameRate);
        mMediaRecorder.setVideoSize(mConfig.videoSize[0], mConfig.videoSize[1]);
        mMediaRecorder.setVideoEncoder(mConfig.videoEncoder);
        mMediaRecorder.setAudioEncoder(mConfig.audioEncoder);

        if(!Utils.isNull(mActivityRefs.get()) && !mActivityRefs.get().isFinishing()) {
            int rotation = mActivityRefs.get().getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation);
            mMediaRecorder.setOrientationHint(orientation);
        }
        mMediaRecorder.prepare();
    }

    public void startRecording() throws IllegalStateException {
        mMediaRecorder.start();
        isRecording = true;
    }

    public void stopRecording() throws IllegalStateException {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mVideoPartIndex++; // new to next video part file
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void onResume() {
        mMediaRecorder = new MediaRecorder();
    }

    public void onPause() {
        if(!Utils.isNull(mMediaRecorder)) {
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException ise) {
                ise.printStackTrace();
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void release() {
        mActivityRefs.clear();
        mActivityRefs = null;

        if(!Utils.isNull(mMediaRecorder)) {
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

}
