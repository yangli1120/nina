package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.List;

/**
 * post tweet bean
 *
 * Created by crazysheep on 16/2/18.
 */
@ParcelablePlease
public class PostTweetBean implements Parcelable, ITweet {

    /**
     * tweet status
     * */
    protected String status;
    /**
     * status id if is a reply tweet
     * */
    protected Long replyStatusId;
    /**
     * media ids
     * */
    protected String mediaIds;
    /**
     * place id
     * */
    protected Long placeId;
    /**
     * posted tweet should display coordinates
     * */
    protected boolean displayCoordinates;
    /**
     * photos' file path on storage, twitter allow at most 4 photo every tweet
     * */
    protected String[] photoFiles;
    /**
     * video's file path on storage, twitter allow at most 1 video every tweet
     * */
    protected String videoFiles;

    private PostTweetBean() {}

    private PostTweetBean(boolean displayCoordinates, String mediaIds, String[] photoFiles,
                         Long placeId, Long replyStatusId, String status, String videoFiles) {
        this.displayCoordinates = displayCoordinates;
        this.mediaIds = mediaIds;
        this.photoFiles = photoFiles;
        this.placeId = placeId;
        this.replyStatusId = replyStatusId;
        this.status = status;
        this.videoFiles = videoFiles;
    }

    public String getVideoFiles() {
        return videoFiles;
    }
    public boolean isDisplayCoordinates() {
        return displayCoordinates;
    }
    public String getMediaIds() {
        return mediaIds;
    }
    public String[] getPhotoFiles() {
        return photoFiles;
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

    public static class Builder {

        private String status;
        private Long replyStatusId;
        private String mediaIds;
        private Long placeId;
        private boolean displayCoordinates;
        private String[] photoFiles;
        private String videoFiles;

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
        public Builder setPhotoFiles(String[] photoFiles) {
            this.photoFiles = photoFiles;
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
        public Builder setVideoFiles(String videoFiles) {
            this.videoFiles = videoFiles;
            return this;
        }

        public PostTweetBean build() {
            return new PostTweetBean(displayCoordinates, mediaIds, photoFiles, placeId,
                    replyStatusId, status, videoFiles);
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
