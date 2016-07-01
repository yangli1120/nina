package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * video variant
 *
 * Created by crazysheep on 16/2/6.
 */
@ParcelablePlease
public class VariantDto implements BaseDto, Parcelable {

    /*
    * {
            "bitrate": 0,
            "content_type": "video/mp4",
            "url": "https://pbs.twimg.com/tweet_video/Cacz1wkXEAAQWr2.mp4"
        }
    * */
    @SerializedName("bitrate")
    public int bitrate;
    @SerializedName("content_type")
    public String content_type;
    @SerializedName("url")
    public String url;

    ////////////////////////// Parcelable /////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        VariantDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<VariantDto> CREATOR = new Creator<VariantDto>() {
        public VariantDto createFromParcel(Parcel source) {
            VariantDto target = new VariantDto();
            VariantDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public VariantDto[] newArray(int size) {
            return new VariantDto[size];
        }
    };
}
