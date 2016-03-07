package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * location dto, contains woeid
 *
 * Created by crazysheep on 16/3/7.
 */
@ParcelablePlease
public class LocationDto implements BaseDto, Parcelable {

    /*
        {
            "name": "Gwangju",
            "placeType": {
                "code": 7,
                "name": "Town"
            },
            "url": "http://where.yahooapis.com/v1/place/1132481",
            "parentid": 23424868,
            "country": "Korea",
            "woeid": 1132481,
            "countryCode": "KR"
        }
    * */
    @SerializedName("country")
    public String country;
    @SerializedName("countryCode")
    public String countryCode;
    @SerializedName("parentid")
    public long parentid;
    @SerializedName("url")
    public String url;
    @SerializedName("woeid")
    public long woeid;

    ///////////////////////////// parcelable /////////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        LocationDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<LocationDto> CREATOR = new Creator<LocationDto>() {
        public LocationDto createFromParcel(Parcel source) {
            LocationDto target = new LocationDto();
            LocationDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public LocationDto[] newArray(int size) {
            return new LocationDto[size];
        }
    };
}
