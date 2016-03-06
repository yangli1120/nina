package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * place trend result dto
 *
 * Created by crazysheep on 16/3/6.
 */
@ParcelablePlease
public class PlaceTrendResultDto implements BaseDto, Parcelable {

    /*
      {
            "as_of": "2012-08-24T23:25:43Z",
            "created_at": "2012-08-24T23:24:14Z",
            "locations": [
              {
                "name": "Worldwide",
                "woeid": 1
              }
            ],
            "trends": [
              {
                "tweet_volume": 3200,
                "events": null,
                "name": "#GanaPuntosSi",
                "promoted_content": null,
                "query": "%23GanaPuntosSi",
                "url": "http://twitter.com/search/?q=%23GanaPuntosSi"
              },
              {
                "tweet_volume": 4200,
                "events": null,
                "name": "#WordsThatDescribeMe",
                "promoted_content": null,
                "query": "%23WordsThatDescribeMe",
                "url": "http://twitter.com/search/?q=%23WordsThatDescribeMe"
              },
              {
                "tweet_volume": 1200,
                "events": null,
                "name": "#10PersonasQueExtra\u00f1oMucho",
                "promoted_content": null,
                "query": "%2310PersonasQueExtra%C3%B1oMucho",
                "url": "http://twitter.com/search/?q=%2310PersonasQueExtra%C3%B1oMucho"
              },
              {
                "tweet_volume": 500,
                "events": null,
                "name": "Apple $1.5",
                "promoted_content": null,
                "query": "%22Apple%20$1.5%22",
                "url": "http://twitter.com/search/?q=%22Apple%20$1.5%22"
              },
              {
                "tweet_volume": 3100,
                "events": null,
                "name": "Zelko",
                "promoted_content": null,
                "query": "Zelko",
                "url": "http://twitter.com/search/?q=Zelko"
              },
              {
                "tweet_volume": 3200,
                "events": null,
                "name": "LWWY",
                "promoted_content": null,
                "query": "LWWY",
                "url": "http://twitter.com/search/?q=LWWY"
              },
              {
                "tweet_volume": 7700,
                "events": null,
                "name": "Lance Armstrong",
                "promoted_content": null,
                "query": "%22Lance%20Armstrong%22",
                "url": "http://twitter.com/search/?q=%22Lance%20Armstrong%22"
              },
              {
                "tweet_volume": 3700,
                "events": null,
                "name": "Gonzo",
                "promoted_content": null,
                "query": "Gonzo",
                "url": "http://twitter.com/search/?q=Gonzo"
              },
              {
                "tweet_volume": 3700,
                "events": null,
                "name": "Premium Rush",
                "promoted_content": null,
                "query": "%22Premium%20Rush%22",
                "url": "http://twitter.com/search/?q=%22Premium%20Rush%22"
              },
              {
                "tweet_volume": 2200,
                "events": null,
                "name": "Sweet Dreams",
                "promoted_content": null,
                "query": "%22Sweet%20Dreams%22",
                "url": "http://twitter.com/search/?q=%22Sweet%20Dreams%22"
              }
            ]
          }
    * */

    @SerializedName("as_of")
    public String as_of;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("locations")
    public String[] locations;
    @SerializedName("trends")
    public TrendDto[] trends;

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
