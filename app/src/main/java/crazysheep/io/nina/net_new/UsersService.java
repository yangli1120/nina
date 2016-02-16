package crazysheep.io.nina.net_new;

import crazysheep.io.nina.bean.UserDto;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * twitter REST api :uses service
 *
 * Created by crazysheep on 16/2/16.
 */
public interface UsersService {

    /**
     * get target user's profile info
     * */
    @GET("/1.1/users/show")
    void getUserInfo(@Query("screen_name") String screenName, Callback<UserDto> callback);
}
