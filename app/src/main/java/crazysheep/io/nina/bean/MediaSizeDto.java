package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * media size dto
 *
 * Created by crazysheep on 16/2/10.
 */
@ParcelablePlease
public class MediaSizeDto implements BaseDto, Parcelable {

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

    @SerializedName("w")
    public int w;
    @SerializedName("h")
    public int h;
    @SerializedName("resize")
    public String resize;

    ////////////////////////// Parcelable /////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MediaSizeDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MediaSizeDto> CREATOR = new Creator<MediaSizeDto>() {
        public MediaSizeDto createFromParcel(Parcel source) {
            MediaSizeDto target = new MediaSizeDto();
            MediaSizeDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public MediaSizeDto[] newArray(int size) {
            return new MediaSizeDto[size];
        }
    };
}
