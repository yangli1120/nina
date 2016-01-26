package crazysheep.io.nina.net;

import java.util.List;

import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.net.HttpCache.CacheConfig;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

/**
 * twitter api
 *
 * Created by crazysheep on 16/1/23.
 */
public interface ApiService {

    /**
     * @param cacheType See {@link CacheConfig}
     * */
    @GET("statuses/user_timeline.json")
    Call<List<TweetDto>> getUserTimeline(
            @Header(CacheConfig.PARAM_CACHE_CONTROL) int cacheType,
            @Query("user_id") Long userId, @Query("screen_name") String screenName,
            @Query("since_id") Long sinceId, @Query("count") Integer count,
            @Query("max_id") Long maxId
    );

    @GET("statuses/home_timeline.json")
    Call<List<TweetDto>> getHomeTimeline(
            @Header(CacheConfig.PARAM_CACHE_CONTROL) int cacheType,
            @Query("count") Integer count);

}
