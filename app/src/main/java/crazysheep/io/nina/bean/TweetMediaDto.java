package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * tweet media dto
 *
 * Created by crazysheep on 16/1/30.
 */
@ParcelablePlease
public class TweetMediaDto implements BaseDto, Parcelable {

    /*
    *  {
                    "id": 693084769400131600,
                    "id_str": "693084769400131584",
                    "indices": [
                        4,
                        27
                    ],
                    "media_url": "http://pbs.twimg.com/media/CZ5U7PdUEAAYTfj.jpg",
                    "media_url_https": "https://pbs.twimg.com/media/CZ5U7PdUEAAYTfj.jpg",
                    "url": "https://t.co/89F0qw1hKI",
                    "display_url": "pic.twitter.com/89F0qw1hKI",
                    "expanded_url": "http://twitter.com/Kojima_Hideo/status/693084835053613056/photo/1",
                    "type": "photo",
                    "sizes": {
                        "thumb": {
                            "w": 150,
                            "h": 150,
                            "resize": "crop"
                        },
                        "medium": {
                            "w": 600,
                            "h": 800,
                            "resize": "fit"
                        },
                        "large": {
                            "w": 768,
                            "h": 1024,
                            "resize": "fit"
                        },
                        "small": {
                            "w": 340,
                            "h": 453,
                            "resize": "fit"
                        }
                    }
                    "video_info": {
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
                }
    * */

    @SerializedName("id")
    public long id;
    @SerializedName("id_str")
    public String id_str;
    @SerializedName("media_url_https")
    public String media_url_https;
    @SerializedName("url")
    public String url;
    @SerializedName("display_url")
    public String display_url;
    @SerializedName("expanded_url")
    public String expanded_url;
    @SerializedName("type")
    public String type;
    @SerializedName("sizes")
    public MediaSizesDto sizes;
    @SerializedName("video_info")
    public VideoInfoDto video_info;

    ////////////////////////// Parcelable /////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TweetMediaDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TweetMediaDto> CREATOR = new Creator<TweetMediaDto>() {
        public TweetMediaDto createFromParcel(Parcel source) {
            TweetMediaDto target = new TweetMediaDto();
            TweetMediaDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TweetMediaDto[] newArray(int size) {
            return new TweetMediaDto[size];
        }
    };
}
