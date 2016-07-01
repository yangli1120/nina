package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * place trend result dto
 *
 * Created by crazysheep on 16/3/6.
 */
@ParcelablePlease
public class PlaceTrendResultDto implements BaseDto, Parcelable {

    /*
          {
                "trends": [
                    {
                        "name": "#UFC196",
                        "url": "http://twitter.com/search?q=%23UFC196",
                        "promoted_content": null,
                        "query": "%23UFC196",
                        "tweet_volume": 874943
                    },
                    {
                        "name": "#MothersDay",
                        "url": "http://twitter.com/search?q=%23MothersDay",
                        "promoted_content": null,
                        "query": "%23MothersDay",
                        "tweet_volume": 60991
                    },
                    {
                        "name": "#marr",
                        "url": "http://twitter.com/search?q=%23marr",
                        "promoted_content": null,
                        "query": "%23marr",
                        "tweet_volume": 11642
                    }
                    ...
                ]
            }
    * */

    @SerializedName("trends")
    public TrendDto[] trends;

    public List<TrendDto> getTrends() {
        return new ArrayList<>(Arrays.asList(trends));
    }

    ///////////////////////// parcelable //////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        PlaceTrendResultDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<PlaceTrendResultDto> CREATOR = new Creator<PlaceTrendResultDto>() {
        public PlaceTrendResultDto createFromParcel(Parcel source) {
            PlaceTrendResultDto target = new PlaceTrendResultDto();
            PlaceTrendResultDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public PlaceTrendResultDto[] newArray(int size) {
            return new PlaceTrendResultDto[size];
        }
    };
}
