package crazysheep.io.nina.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by crazysheep on 16/3/28.
 */
@ParcelablePlease
public class ConfigurationDto implements BaseDto, Parcelable {

    /*
      {
          "characters_reserved_per_media": 24,
          "dm_text_character_limit": 10000,
          "max_media_per_upload": 1,
          "photo_size_limit": 3145728,
          "photo_sizes": {
            "large": {
              "h": 2048,
              "resize": "fit",
              "w": 1024
            },
            "medium": {
              "h": 1200,
              "resize": "fit",
              "w": 600
            },
            "small": {
              "h": 480,
              "resize": "fit",
              "w": 340
            },
            "thumb": {
              "h": 150,
              "resize": "crop",
              "w": 150
            }
          },
          "short_url_length": 23,
          "short_url_length_https": 23,
          "non_username_paths": [
            "about",
            "account",
            "accounts",
            "activity",
            "all",
            "announcements",
            "anywhere",
            "api_rules",
            "api_terms",
            "apirules",
            "apps",
            "auth",
            "badges",
            "blog",
            "business",
            "buttons",
            "contacts",
            "devices",
            "direct_messages",
            "download",
            "downloads",
            "edit_announcements",
            "faq",
            "favorites",
            "find_sources",
            "find_users",
            "followers",
            "following",
            "friend_request",
            "friendrequest",
            "friends",
            "goodies",
            "help",
            "home",
            "i",
            "im_account",
            "inbox",
            "invitations",
            "invite",
            "jobs",
            "list",
            "login",
            "logo",
            "logout",
            "me",
            "mentions",
            "messages",
            "mockview",
            "newtwitter",
            "notifications",
            "nudge",
            "oauth",
            "phoenix_search",
            "positions",
            "privacy",
            "public_timeline",
            "related_tweets",
            "replies",
            "retweeted_of_mine",
            "retweets",
            "retweets_by_others",
            "rules",
            "saved_searches",
            "search",
            "sent",
            "sessions",
            "settings",
            "share",
            "signup",
            "signin",
            "similar_to",
            "statistics",
            "terms",
            "tos",
            "translate",
            "trends",
            "tweetbutton",
            "twttr",
            "update_discoverability",
            "users",
            "welcome",
            "who_to_follow",
            "widgets",
            "zendesk_auth",
            "media_signup"
          ]
        }
    * */

    @SerializedName("characters_reserved_per_media")
    public int characters_reserved_per_media;
    @SerializedName("dm_text_character_limit")
    public int dm_text_character_limit;
    @SerializedName("max_media_per_upload")
    public int max_media_per_upload;
    @SerializedName("photo_size_limit")
    public int photo_size_limit;
    @SerializedName("short_url_length")
    public int short_url_length;
    @SerializedName("short_url_length_https")
    public int short_url_length_https;

    ////////////////////////////// parcelable //////////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ConfigurationDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ConfigurationDto> CREATOR = new Creator<ConfigurationDto>() {
        public ConfigurationDto createFromParcel(Parcel source) {
            ConfigurationDto target = new ConfigurationDto();
            ConfigurationDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ConfigurationDto[] newArray(int size) {
            return new ConfigurationDto[size];
        }
    };
}
