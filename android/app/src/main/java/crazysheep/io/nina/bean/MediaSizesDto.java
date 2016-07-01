package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 *
 * Created by crazysheep on 16/2/10.
 */
@ParcelablePlease
public class MediaSizesDto implements BaseDto, Parcelable {

    /*
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
    * */

    @SerializedName("thumb")
    public MediaSizeDto thumb;
    @SerializedName("medium")
    public MediaSizeDto medium;
    @SerializedName("large")
    public MediaSizeDto large;
    @SerializedName("small")
    public MediaSizeDto small;

    ////////////////////////// Parcelable /////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MediaSizesDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MediaSizesDto> CREATOR = new Creator<MediaSizesDto>() {
        public MediaSizesDto createFromParcel(Parcel source) {
            MediaSizesDto target = new MediaSizesDto();
            MediaSizesDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public MediaSizesDto[] newArray(int size) {
            return new MediaSizesDto[size];
        }
    };
}
