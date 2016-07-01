package crazysheep.io.nina.net;

import java.util.List;

import crazysheep.io.nina.bean.LocationDto;
import crazysheep.io.nina.bean.PlaceTrendResultDto;
import crazysheep.io.nina.bean.SearchResultDto;
import crazysheep.io.nina.bean.TweetDto;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;

/**
 * rxjava version for {@link com.twitter.sdk.android.Twitter}
 *
 * Created by crazysheep on 16/2/22.
 */
public interface RxTwitterService {

    ///////////////////////// status ///////////////////////
    /**
     * rxjava type get user home timeline
     * */
    @GET("statuses/home_timeline.json")
    Observable<List<TweetDto>> getHomeTimeline(
            @Header(HttpCache.CacheConfig.PARAM_CACHE_CONTROL) int cacheType,
            @Query("max_id") Long maxId,
            @Query("count") Integer count);

    ///////////////////// trends ////////////////////////

    @GET("trends/place.json")
    Observable<List<PlaceTrendResultDto>> trend(@Query("id") Long woeid);

    @GET("trends/closest.json")
    Observable<List<LocationDto>> closest(@Query("lat") double latitude,
                                          @Query("long") double longitude);

    ///////////////////////// searchReply //////////////////////////

    @GET("search/tweets.json?result_type=recent&count=100")
    Observable<SearchResultDto> reply(@Query("q") String q);

}
