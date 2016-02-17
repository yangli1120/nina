package crazysheep.io.nina.net_new;

import java.util.List;

import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.bean.UserDto;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * twitter REST api service
 *
 * Created by crazysheep on 16/2/16.
 */
public interface TwitterService {

    /////////////////////// timeline ///////////////////////////

    /**
     * use home timeline
     * */
    @GET("/1.1/statuses/home_timeline.json")
    void getHomeTimeline(@Query("count") Integer count, @Query("max_id") Long maxId,
                         NiceCallback<List<TweetDto>> callback);

    /**
     * use profile timeline
     * */
    @GET("/1.1/statuses/user_timeline.json")
    void getUserTimeline(@Query("count") Integer count, @Query("screen_name") String screenName,
                         @Query("max_id") Long maxId, NiceCallback<List<TweetDto>> callback);

    /////////////////////// uses //////////////////////////////
    /**
     * get target user's profile info
     * */
    @GET("/1.1/users/show.json")
    void getUserInfo(@Query("screen_name") String screenName, NiceCallback<UserDto> callback);
}
