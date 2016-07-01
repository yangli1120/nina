package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.ArrayList;
import java.util.Arrays;

import crazysheep.io.nina.utils.Utils;

/**
 * tweet entity dto
 *
 * Created by crazysheep on 16/3/28.
 */
@ParcelablePlease
public class TweetEntityDto implements Parcelable {

    /*
       {
            "hashtags": [],
            "symbols": [],
            "user_mentions": [
                {
                    "screen_name": "MaxHualop",
                    "name": "Max Colin's",
                    "id": 2461857720,
                    "id_str": "2461857720",
                    "indices": [
                        43,
                        53
                    ]
                }
            ],
            "urls": [
                {
                    "url": "https://t.co/WXAtc0DdaY",
                    "expanded_url": "http://www.materialup.com/posts/settings-mercurian-icon-pack",
                    "display_url": "materialup.com/posts/settings…",
                    "indices": [
                        55,
                        78
                    ]
                }
            ],
            "media": [
                {
                    "id": 714332737252024300,
                    "id_str": "714332737252024320",
                    "indices": [
                        79,
                        102
                    ],
                    "media_url": "http://pbs.twimg.com/media/CenR1v9W4AAOgQZ.jpg",
                    "media_url_https": "https://pbs.twimg.com/media/CenR1v9W4AAOgQZ.jpg",
                    "url": "https://t.co/qsNAXK9Zwk",
                    "display_url": "pic.twitter.com/qsNAXK9Zwk",
                    "expanded_url": "http://twitter.com/MaterialUp/status/714332737411407872/photo/1",
                    "type": "photo",
                    "sizes": {
                        "thumb": {
                            "w": 150,
                            "h": 150,
                            "resize": "crop"
                        },
                        "medium": {
                            "w": 600,
                            "h": 450,
                            "resize": "fit"
                        },
                        "small": {
                            "w": 340,
                            "h": 255,
                            "resize": "fit"
                        },
                        "large": {
                            "w": 800,
                            "h": 600,
                            "resize": "fit"
                        }
                    }
                }
            ]
        }
    * */

    @SerializedName("urls")
    public UrlDto[] urls;
    @SerializedName("media")
    public TweetMediaDto[] media;

    public ArrayList<UrlDto> getUrls() {
        return !Utils.isNull(urls) ? new ArrayList<>(Arrays.asList(urls)) : new ArrayList<UrlDto>();
    }

    public ArrayList<TweetMediaDto> getMedias() {
        return !Utils.isNull(media) ? new ArrayList<>(Arrays.asList(media))
                : new ArrayList<TweetMediaDto>();
    }

    //////////////////////////// parcelable ///////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TweetEntityDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TweetEntityDto> CREATOR = new Creator<TweetEntityDto>() {
        public TweetEntityDto createFromParcel(Parcel source) {
            TweetEntityDto target = new TweetEntityDto();
            TweetEntityDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TweetEntityDto[] newArray(int size) {
            return new TweetEntityDto[size];
        }
    };
}
