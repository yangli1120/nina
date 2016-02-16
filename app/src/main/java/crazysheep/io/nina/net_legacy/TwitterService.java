package crazysheep.io.nina.net_legacy;

import java.util.List;

import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.bean.UserDto;
import crazysheep.io.nina.net_legacy.HttpCache.CacheConfig;
import retrofit2.http.GET;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * twitter api
 *
 * Created by crazysheep on 16/1/23.
 */
public interface TwitterService {

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
     * get user favorite tweets timeline
     * */
    @GET("favorites/list")
    Call<List<TweetDto>> getFavoritesTimeline(
            @Query("screen_name") String screenName, @Query("max_id") Long maxId,
            @Query("count") Integer count);

    /**
     * get target user's profile info
     * */
    @GET("users/show")
    Call<UserDto> getUserInfo(@Query("screen_name") String screenName);

    /**
     * verify if account is credential
     * */
    @GET("account/verify_credentials")
    void getUserVerify(@Query("include_email") String email);

}
