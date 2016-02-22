package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.Arrays;
import java.util.List;

import crazysheep.io.nina.utils.Utils;

/**
 * post tweet bean
 *
 * Created by crazysheep on 16/2/18.
 */
@ParcelablePlease
@Table(name = "post_tweet")
public class PostTweetBean extends Model implements Parcelable, ITweet {

    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_REPLY_STATUS_ID = "reply_status_id";
    public static final String COLUMN_MEDIA_IDS = "media_ids";
    public static final String COLUMN_PLACE_ID = "place_id";
    public static final String COLUMN_DISPLAY_COORDINATES = "display_coordinates";
    public static final String COLUMN_PHOTO_FILES = "photo_files";
    public static final String COLUMN_VIDEO_FILE = "video_file";
    public static final String COLUMN_POST_STATE = "post_state";
    public static final String COLUMN_RANDOM_ID = "random_id";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String STATE_READY = "state_ready";
    public static final String STATE_POSTING = "state_posting";
    public static final String STATE_FAILED = "state_failed";

    /**
     * tweet status
     * */
    @Column(name = COLUMN_STATUS)
    protected String status;
    /**
     * status id if is a reply tweet
     * */
    @Column(name = COLUMN_REPLY_STATUS_ID)
    protected Long replyStatusId;
    /**
     * media ids, split use char ';', such like "[ids_1];[ids_2];[ids_3];[ids_4]"
     * */
    @Column(name = COLUMN_MEDIA_IDS)
    protected String mediaIds;
    /**
     * place id
     * */
    @Column(name = COLUMN_PLACE_ID)
    protected Long placeId;
    /**
     * posted tweet should display coordinates
     * */
    @Column(name = COLUMN_DISPLAY_COORDINATES)
    protected boolean displayCoordinates;
    /**
     * photos' file path on storage, twitter allow at most 4 photo every tweet
     * split use char ';', such like "[file_1];[file_2];[file_3];[file_4]"
     * */
    @Column(name = COLUMN_PHOTO_FILES)
    protected String photoFiles;
    /**
     * video's file path on storage, twitter allow at most 1 video every tweet
     * */
    @Column(name = COLUMN_VIDEO_FILE)
    protected String videoFile;

    /**
     * for location database use, record current tweet post state, such like
     * "state_posting", "state_failed"
     * */
    @Column(name = COLUMN_POST_STATE)
    public String postState = STATE_READY;

    /**
     * a random id, for unique identification
     * */
    @Column(name = COLUMN_RANDOM_ID)
    public String randomId;

    /**
     * post tweet draft created at
     * */
    @Column(name = COLUMN_CREATED_AT)
    public long created_at;

    public PostTweetBean() {}

    private PostTweetBean(boolean displayCoordinates, String mediaIds, String photoFiles,
                          Long placeId, Long replyStatusId, String status, String videoFile,
                          String randomId, long created_at) {
        this.displayCoordinates = displayCoordinates;
        this.mediaIds = mediaIds;
        this.photoFiles = photoFiles;
        this.placeId = placeId;
        this.replyStatusId = replyStatusId;
        this.status = status;
        this.videoFile = videoFile;
        this.randomId = randomId;
        this.created_at = created_at;
    }

    public String getVideoFile() {
        return videoFile;
    }
    public boolean isDisplayCoordinates() {
        return displayCoordinates;
    }
    public String getMediaIds() {
        return mediaIds;
    }
    public List<String> getPhotoFiles() {
        return Utils.isNull(photoFiles) ? null : Arrays.asList(photoFiles.split(";"));
    }
    public Long getPlaceId() {
        return placeId;
    }
    public Long getReplyStatusId() {
        return replyStatusId;
    }
    public String getStatus() {
        return status;
    }

    public void setPosting() {
        postState = STATE_POSTING;
    }

    public boolean isPosting() {
        return STATE_POSTING.equals(postState);
    }

    public void setFailed() {
        postState = STATE_FAILED;
    }

    public boolean isFailed() {
        return STATE_FAILED.equals(postState);
    }

    public static class Builder {

        private String status;
        private Long replyStatusId;
        private String mediaIds;
        private Long placeId;
        private boolean displayCoordinates;
        private String photoFiles;
        private String videoFile;

        public Builder() {
        }

        public Builder setDisplayCoordinates(boolean displayCoordinates) {
            this.displayCoordinates = displayCoordinates;
            return this;
        }
        public Builder setMediaIds(String mediaIds) {
            this.mediaIds = mediaIds;
            return this;
        }
        public Builder setPhotoFiles(List<String> photoFiles) {
            if(!Utils.isNull(photoFiles) && photoFiles.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for(String s : photoFiles)
                    sb.append(s).append(";");
                sb.deleteCharAt(sb.length() - 1);
                this.photoFiles = sb.toString();
            }
            return this;
        }
        public Builder setPlaceId(Long placeId) {
            this.placeId = placeId;
            return this;
        }
        public Builder setReplyStatusId(Long replyStatusId) {
            this.replyStatusId = replyStatusId;
            return this;
        }
        public Builder setStatus(String status) {
            this.status = status;
            return this;
        }
        public Builder setVideoFile(String videoFile) {
            this.videoFile = videoFile;
            return this;
        }

        public PostTweetBean build() {
            return new PostTweetBean(displayCoordinates, mediaIds, photoFiles, placeId,
                    replyStatusId, status, videoFile, Utils.randomId(), System.currentTimeMillis());
        }
    }

    ///////////////////////////// Parcelable /////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        PostTweetBeanParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<PostTweetBean> CREATOR = new Creator<PostTweetBean>() {
        public PostTweetBean createFromParcel(Parcel source) {
            PostTweetBean target = new PostTweetBean();
            PostTweetBeanParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public PostTweetBean[] newArray(int size) {
            return new PostTweetBean[size];
        }
    };
}
