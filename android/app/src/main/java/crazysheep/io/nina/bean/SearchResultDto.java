package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * tweets return by search api
 *
 * Created by crazysheep on 16/3/7.
 */
@ParcelablePlease
public class SearchResultDto implements BaseDto, Parcelable {

    @SerializedName("statuses")
    public TweetDto[] statuses;

    public List<TweetDto> getStatuses() {
        return new ArrayList<>(Arrays.asList(statuses));
    }

    ////////////////////////////////// parcelable /////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SearchResultDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<SearchResultDto> CREATOR = new Creator<SearchResultDto>() {
        public SearchResultDto createFromParcel(Parcel source) {
            SearchResultDto target = new SearchResultDto();
            SearchResultDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public SearchResultDto[] newArray(int size) {
            return new SearchResultDto[size];
        }
    };
}
