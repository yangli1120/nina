package crazysheep.io.nina.net_new;

import java.util.List;

import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.bean.UserDto;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * twitter REST api service
 *
 * Created by crazysheep on 16/2/16.
 */
public interface TwitterService {

    /////////////////////// timeline ///////////////////////////

    @GET("/1.1/statuses/home_timeline.json")
    void getHomeTimeline(@Query("count") Integer count, @Query("max_id") Long maxId,
                         Callback<List<TweetDto>> tweets);

    /////////////////////// uses //////////////////////////////
    /**
     * get target user's profile info
     * */
    @GET("/1.1/users/show")
    void getUserInfo(@Query("screen_name") String screenName, Callback<UserDto> callback);
}
