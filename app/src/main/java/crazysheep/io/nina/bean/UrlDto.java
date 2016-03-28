package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by crazysheep on 16/3/28.
 */
@ParcelablePlease
public class UrlDto implements BaseDto, Parcelable {

    /*
           [
                {
                    "url": "https://t.co/WXAtc0DdaY",
                    "expanded_url": "http://www.materialup.com/posts/settings-mercurian-icon-pack",
                    "display_url": "materialup.com/posts/settings…",
                    "indices": [
                        55,
                        78
                    ]
                }
            ]
    * */

    @SerializedName("url")
    public String url;
    @SerializedName("expanded_url")
    public String expanded_url;
    @SerializedName("display_url")
    public String display_url;

    ////////////////////////// parcelable ////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UrlDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UrlDto> CREATOR = new Creator<UrlDto>() {
        public UrlDto createFromParcel(Parcel source) {
            UrlDto target = new UrlDto();
            UrlDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UrlDto[] newArray(int size) {
            return new UrlDto[size];
        }
    };
}
