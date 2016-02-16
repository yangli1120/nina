package crazysheep.io.nina.net_new;

/**
 * Created by crazysheep on 16/2/16.
 */

import crazysheep.io.nina.net_legacy.HttpConstants;
import crazysheep.io.nina.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * nice callback implement {@link retrofit.Callback}
 *
 * Created by crazysheep on 16/1/27.
 */
public abstract class NiceCallback<T> implements Callback<T> {

    @Override
    public void success(T t, Response response) {
        if(response.getStatus() == HttpConstants.CODE_200)
            onRespond(t, response);
        else
            onFailed(new Throwable("request failed, status code: " + response.getStatus()
                    + ", response header: " + response.getHeaders().toString()
                    + ", response body: " + (Utils.isNull(response.getBody())
                    ? null : response.getBody().toString())));
        onDone();
    }

    @Override
    public void failure(RetrofitError error) {
        onFailed(new Throwable(Utils.isNull(error) ? "unknow error" : error.toString()));
        onDone();
    }

    public abstract void onRespond(T t, Response response);

    public abstract void onFailed(Throwable t);

    public void onDone() {}

}
