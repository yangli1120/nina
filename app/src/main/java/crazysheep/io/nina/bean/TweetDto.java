package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import crazysheep.io.nina.utils.Utils;

/**
 * tweet data
 *
 * Created by crazysheep on 16/1/23.
 */
@ParcelablePlease
public class TweetDto implements BaseDto, Parcelable {

    ////////////////////////// api ////////////////////////

    public boolean isPhoto() {
        return !Utils.isNull(extended_entities) && !Utils.isNull(extended_entities.media)
                && !Utils.isNull(extended_entities.media.get(0))
                && "photo".equals(extended_entities.media.get(0).type);
    }

    /**
     * if this tweet is retweeted
     * */
    public boolean isRetweeted() {
        return !Utils.isNull(retweeted_status);
    }

    ///////////////////////////////////////////////////////

    /*
    *  {
        "coordinates": null,
        "favorited": false,
        "truncated": false,
        "created_at": "Wed Aug 29 17:12:58 +0000 2012",
        "id_str": "240859602684612608",
        "entities": {
            "urls": [
                {
                    "expanded_url": "https://dev.twitter.com/blog/twitter-certified-products",
                    "url": "https://t.co/MjJ8xAnT",
                    "indices": [
                        52,
                        73
                    ],
                    "display_url": "dev.twitter.com/blog/twitter-c…"
                }
            ],
            "hashtags": [],
            "user_mentions": []
        },
        "in_reply_to_user_id_str": null,
        "contributors": null,
        "text": "Introducing the Twitter Certified Products Program: https://t.co/MjJ8xAnT",
        "retweet_count": 121,
        "in_reply_to_status_id_str": null,
        "id": 240859602684612600,
        "geo": null,
        "retweeted": false,
        "possibly_sensitive": false,
        "in_reply_to_user_id": null,
        "place": null,
        "user": {
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
        },
        "in_reply_to_screen_name": null,
        "source": "",
        "in_reply_to_status_id": null
    },
    * */

    @SerializedName("favorited")
    public boolean favorited;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("text")
    public String text;
    @SerializedName("extended_entities")
    public TweetEntitiesDto extended_entities;
    @SerializedName("retweet_count")
    public int retweet_count;
    @SerializedName("id")
    public long id;
    @SerializedName("retweeted")
    public boolean retweeted;
    @SerializedName("possibly_sensitive")
    public boolean possibly_sensitive;
    @SerializedName("user")
    public UserDto user;
    @SerializedName("retweeted_status")
    public TweetDto retweeted_status;

    ////////////////////////////////// Parcelable //////////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TweetDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TweetDto> CREATOR = new Creator<TweetDto>() {
        public TweetDto createFromParcel(Parcel source) {
            TweetDto target = new TweetDto();
            TweetDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TweetDto[] newArray(int size) {
            return new TweetDto[size];
        }
    };
}
