package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * geo query dto
 *
 * Created by crazysheep on 16/3/6.
 */
@ParcelablePlease
public class GeoQueryDto implements BaseDto, Parcelable {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        GeoQueryDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<GeoQueryDto> CREATOR = new Creator<GeoQueryDto>() {
        public GeoQueryDto createFromParcel(Parcel source) {
            GeoQueryDto target = new GeoQueryDto();
            GeoQueryDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public GeoQueryDto[] newArray(int size) {
            return new GeoQueryDto[size];
        }
    };
}
