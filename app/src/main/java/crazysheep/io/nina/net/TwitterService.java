package crazysheep.io.nina.net;

import java.util.List;

import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.bean.UserDto;
import crazysheep.io.nina.net.HttpCache.CacheConfig;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

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

}
