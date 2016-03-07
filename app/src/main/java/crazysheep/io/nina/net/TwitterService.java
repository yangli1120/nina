package crazysheep.io.nina.net;

import java.util.List;

import crazysheep.io.nina.bean.LocationDto;
import crazysheep.io.nina.bean.PlaceTrendResultDto;
import crazysheep.io.nina.bean.SearchResultDto;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.bean.UploadMediaDto;
import crazysheep.io.nina.bean.UserDto;
import crazysheep.io.nina.net.HttpCache.CacheConfig;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * twitter api
 *
 * Created by crazysheep on 16/1/23.
 */
public interface TwitterService {

    /////////////////////// statuses //////////////////////////

    /**
     * create a new tweet
     * */
    @POST("statuses/update.json")
    Call<TweetDto> postTweet(
            @Query("status") String status,
            @Query("in_reply_to_status_id") Long replyStatusId,
            @Query("place_id") Long placeId,
            @Query("display_coordinates") boolean displayCoordinates,
            @Query("media_ids") String mediaIds);

    /**
     * get user timeline
     *
     * @param cacheType See {@link CacheConfig}
     * */
    @GET("statuses/user_timeline.json")
    Call<List<TweetDto>> getUserTimeline(
            @Header(CacheConfig.PARAM_CACHE_CONTROL) int cacheType,
            @Query("screen_name") String screenName,
            @Query("count") Integer count,
            @Query("max_id") Long maxId
    );

    /**
     * get user home timeline
     * */
    @GET("statuses/home_timeline.json")
    Call<List<TweetDto>> getHomeTimeline(
            @Header(CacheConfig.PARAM_CACHE_CONTROL) int cacheType,
            @Query("max_id") Long maxId,
            @Query("count") Integer count);

    /**
     * destroy a own tweet
     * */
    @GET("statuses/destroy/{id}.json")
    Call<TweetDto> detroyTweet(@Path("id") Long tweetId);

    //////////////////// favorites ///////////////////////

    /**
     * get user favorite tweets timeline
     * */
    @GET("favorites/list.json")
    Call<List<TweetDto>> getFavoritesTimeline(
            @Query("screen_name") String screenName, @Query("max_id") Long maxId,
            @Query("count") Integer count);

    ////////////////////////// users /////////////////////

    /**
     * get target user's profile info
     * */
    @GET("users/show.json")
    Call<UserDto> getUserInfo(@Query("screen_name") String screenName);

    ///////////////////////// account /////////////////////

    /**
     * verify if account is credential
     * */
    @GET("account/verify_credentials.json")
    void getUserVerify(@Query("include_email") String email);

    //////////////////////// upload ///////////////////////

    @Multipart
    @POST
    Call<UploadMediaDto> uploadPhoto(@Url String uploadUrl,
                                     @Part("media\"; filename=\"image.jpg\" ")RequestBody body);

    /////////////////////// friendship //////////////////////

    @POST("friendships/create.json")
    Call<UserDto> follow(@Query("screen_name") String screenName);

    @POST("friendships/destroy.json")
    Call<UserDto> unfollow(@Query("screen_name") String screenName);

    ////////////////////// favorites ///////////////////////

    @POST("favorites/create.json")
    Call<TweetDto> like(@Query("id") Long id);

    @POST("favorites/destroy.json")
    Call<TweetDto> unlike(@Query("id") Long id);

    ///////////////////// trends ////////////////////////

    @GET("trends/place.json")
    Call<List<PlaceTrendResultDto>> trend(@Query("id") Long woeid);

    @GET("trends/closest.json")
    Call<List<LocationDto>> closest(@Query("lat") double latitude, @Query("long") double longitude);

    ///////////////////// search //////////////////////////

    @GET("search/tweets.json")
    Call<SearchResultDto> search(@Query("q") String q);

}
