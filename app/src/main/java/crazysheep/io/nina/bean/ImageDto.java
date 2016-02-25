package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by crazysheep on 16/2/25.
 */
@ParcelablePlease
public class ImageDto implements Parcelable, BaseDto {

    /*
         {
            "w": 2234,
            "h": 1873,
            "image_type": "image/jpeg"
          }
    * */
    @SerializedName("w")
    public int w;
    @SerializedName("h")
    public int h;
    @SerializedName("image_type")
    public String image_type;

    //////////////////////////// parcelable ///////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ImageDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ImageDto> CREATOR = new Creator<ImageDto>() {
        public ImageDto createFromParcel(Parcel source) {
            ImageDto target = new ImageDto();
            ImageDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ImageDto[] newArray(int size) {
            return new ImageDto[size];
        }
    };
}
