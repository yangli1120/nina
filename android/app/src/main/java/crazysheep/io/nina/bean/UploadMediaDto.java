package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * result of upload media
 *
 * Created by crazysheep on 16/2/25.
 */
@ParcelablePlease
public class UploadMediaDto implements Parcelable, BaseDto {

    /*
       {
          "media_id": 553656900508606464,
          "media_id_string": "553656900508606464",
          "size": 998865,
          "image": {
            "w": 2234,
            "h": 1873,
            "image_type": "image/jpeg"
          }
        }
    * */
    @SerializedName("media_id")
    public long media_id;
    @SerializedName("media_id_string")
    public String media_id_string;
    @SerializedName("size")
    public long size;
    @SerializedName("image")
    public ImageDto image;

    ////////////////////////////// parcelable /////////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UploadMediaDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UploadMediaDto> CREATOR = new Creator<UploadMediaDto>() {
        public UploadMediaDto createFromParcel(Parcel source) {
            UploadMediaDto target = new UploadMediaDto();
            UploadMediaDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UploadMediaDto[] newArray(int size) {
            return new UploadMediaDto[size];
        }
    };
}
