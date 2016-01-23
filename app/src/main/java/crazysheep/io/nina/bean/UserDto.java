package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * tweet user data
 *
 * Created by crazysheep on 16/1/23.
 */
@ParcelablePlease
public class UserDto implements BaseDto, Parcelable {

    /*
    * {
            "profile_sidebar_fill_color": "DDEEF6",
            "profile_sidebar_border_color": "C0DEED",
            "profile_background_tile": false,
            "name": "Twitter API",
            "profile_image_url": "http://a0.twimg.com/profile_images/2284174872/7df3h38zabcvjylnyfe3_normal.png",
            "created_at": "Wed May 23 06:01:13 +0000 2007",
            "location": "San Francisco, CA",
            "follow_request_sent": false,
            "profile_link_color": "0084B4",
            "is_translator": false,
            "id_str": "6253282",
            "entities": {
                "url": {
                    "urls": [
                        {
                            "expanded_url": null,
                            "url": "http://dev.twitter.com",
                            "indices": [
                                0,
                                22
                            ]
                        }
                    ]
                },
                "description": {
                    "urls": []
                }
            },
            "default_profile": true,
            "contributors_enabled": true,
            "favourites_count": 24,
            "url": "http://dev.twitter.com",
            "profile_image_url_https": "https://si0.twimg.com/profile_images/2284174872/7df3h38zabcvjylnyfe3_normal.png",
            "utc_offset": -28800,
            "id": 6253282,
            "profile_use_background_image": true,
            "listed_count": 10775,
            "profile_text_color": "333333",
            "lang": "en",
            "followers_count": 1212864,
            "protected": false,
            "notifications": null,
            "profile_background_image_url_https": "https://si0.twimg.com/images/themes/theme1/bg.png",
            "profile_background_color": "C0DEED",
            "verified": true,
            "geo_enabled": true,
            "time_zone": "Pacific Time (US & Canada)",
            "description": "The Real Twitter API. I tweet about API changes, service issues and happily answer questions about Twitter and our API. Don't get an answer? It's on my website.",
            "default_profile_image": false,
            "profile_background_image_url": "http://a0.twimg.com/images/themes/theme1/bg.png",
            "statuses_count": 3333,
            "friends_count": 31,
            "following": null,
            "show_all_inline_media": false,
            "screen_name": "twitterapi"
        }
    * */

    @SerializedName("profile_sidebar_border_color")
    public String profile_sidebar_fill_color;
    @SerializedName("profile_sidebar_border_color")
    public String profile_sidebar_border_color;
    @SerializedName("profile_background_tile")
    public boolean profile_background_tile;
    @SerializedName("profile_image_url")
    public String profile_image_url;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("location")
    public String location;
    @SerializedName("follow_request_sent")
    public boolean follow_request_sent;
    @SerializedName("profile_link_color")
    public String profile_link_color;
    @SerializedName("is_translator")
    public boolean is_translator;
    @SerializedName("default_profile")
    public boolean default_profile;
    @SerializedName("contributors_enabled")
    public boolean contributors_enabled;
    @SerializedName("favourites_count")
    public int favourites_count;
    @SerializedName("url")
    public String url;
    @SerializedName("profile_image_url_https")
    public String profile_image_url_https;
    @SerializedName("id")
    public long id;
    @SerializedName("profile_use_background_image")
    public boolean profile_use_background_image;
    @SerializedName("listed_count")
    public int listed_count;
    @SerializedName("profile_text_color")
    public String profile_text_color;
    @SerializedName("lang")
    public String lang;
    @SerializedName("followers_count")
    public int followers_count;
    @SerializedName("protected")
    public boolean isProtected;
    @SerializedName("profile_background_image_url_https")
    public String profile_background_image_url_https;
    @SerializedName("profile_background_color")
    public String profile_background_color;
    @SerializedName("verified")
    public boolean verified;
    @SerializedName("geo_enabled")
    public boolean geo_enabled;
    @SerializedName("time_zone")
    public String time_zone;
    @SerializedName("description")
    public String description;
    @SerializedName("default_profile_image")
    public boolean default_profile_image;
    @SerializedName("profile_background_image_url")
    public String profile_background_image_url;
    @SerializedName("statuses_count")
    public int statuses_count;
    @SerializedName("friends_count")
    public int friends_count;
    @SerializedName("show_all_inline_media")
    public boolean show_all_inline_media;
    @SerializedName("screen_name")
    public String screen_name;

    /////////////////////////////////// Parcelable /////////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserDto> CREATOR = new Creator<UserDto>() {
        public UserDto createFromParcel(Parcel source) {
            UserDto target = new UserDto();
            UserDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserDto[] newArray(int size) {
            return new UserDto[size];
        }
    };
}
