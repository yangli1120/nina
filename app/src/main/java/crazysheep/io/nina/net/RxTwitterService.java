package crazysheep.io.nina.net;

import java.util.List;

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

}
