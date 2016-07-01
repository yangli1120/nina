package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.List;

/**
 * tweet video info
 *
 * Created by crazysheep on 16/2/6.
 */
@ParcelablePlease
public class VideoInfoDto implements BaseDto, Parcelable {

    /*
    * "video_info": {
                    "aspect_ratio": [
                        4,
                        3
                    ],
                    "variants": [
                        {
                            "bitrate": 0,
                            "content_type": "video/mp4",
                            "url": "https://pbs.twimg.com/tweet_video/Cacz1wkXEAAQWr2.mp4"
                        }
                    ]
                }
    * */
    @SerializedName("aspect_ratio")
    public int[] aspect_ratio;
    @SerializedName("variants")
    public List<VariantDto> variants;

    ////////////////////////// Parcelable /////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        VideoInfoDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<VideoInfoDto> CREATOR = new Creator<VideoInfoDto>() {
        public VideoInfoDto createFromParcel(Parcel source) {
            VideoInfoDto target = new VideoInfoDto();
            VideoInfoDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public VideoInfoDto[] newArray(int size) {
            return new VideoInfoDto[size];
        }
    };
}
