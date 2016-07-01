package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.List;

/**
 * tweet entities dto
 *
 * Created by crazysheep on 16/1/30.
 */
@ParcelablePlease
public class TweetEntitiesDto implements BaseDto, Parcelable {

    /*
    * "extended_entities": {
            "media": [
                {
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
                },
                ...
            ]
       }
    * */

    @SerializedName("media")
    public List<TweetMediaDto> media;

    ////////////////////////////////// Parcelable //////////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TweetEntitiesDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TweetEntitiesDto> CREATOR = new Creator<TweetEntitiesDto>() {
        public TweetEntitiesDto createFromParcel(Parcel source) {
            TweetEntitiesDto target = new TweetEntitiesDto();
            TweetEntitiesDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TweetEntitiesDto[] newArray(int size) {
            return new TweetEntitiesDto[size];
        }
    };

}
