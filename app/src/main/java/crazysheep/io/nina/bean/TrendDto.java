package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * trend dto
 *
 * Created by crazysheep on 16/3/6.
 */
@ParcelablePlease
public class TrendDto implements BaseDto, Parcelable {

    /*
          {
                "name": "#UFC196",
                "url": "http://twitter.com/search?q=%23UFC196",
                "promoted_content": null,
                "query": "%23UFC196",
                "tweet_volume": 874943
            }
    * */
    @SerializedName("tweet_volume")
    public int tweet_volume;
    @SerializedName("name")
    public String name;
    @SerializedName("query")
    public String query;
    @SerializedName("url")
    public String url;

    //////////////////////// parcelable /////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TrendDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TrendDto> CREATOR = new Creator<TrendDto>() {
        public TrendDto createFromParcel(Parcel source) {
            TrendDto target = new TrendDto();
            TrendDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TrendDto[] newArray(int size) {
            return new TrendDto[size];
        }
    };
}
