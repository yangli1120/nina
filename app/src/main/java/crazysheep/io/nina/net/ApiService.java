package crazysheep.io.nina.net;

import java.util.List;

import crazysheep.io.nina.bean.TweetDto;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * twitter api
 *
 * Created by crazysheep on 16/1/23.
 */
public interface ApiService {

    @GET("statuses/user_timeline.json")
    Call<List<TweetDto>> getUserTimeline(
            @Query("user_id") Long userId, @Query("screen_name") String screenName,
            @Query("since_id") Long sinceId, @Query("count") Integer count,
            @Query("max_id") Long maxId
    );
}
