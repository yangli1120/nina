package crazysheep.io.nina.net;

import crazysheep.io.nina.utils.Utils;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * nice callback implement {@link retrofit.Callback}
 *
 * Created by crazysheep on 16/1/27.
 */
public abstract class NiceCallback<T> implements retrofit.Callback<T> {

    @Override
    public void onResponse(Response<T> response, Retrofit retrofit) {
        if(response.code() == HttpConstants.CODE_200)
            onRespond(response, retrofit);
        else
            onFailed(new Throwable("request failed, status code: " + response.code()
                    + ", response header: " + response.headers().toString()
                    + ", response body: " + (Utils.isNull(response.body())
                            ? null : response.body().toString())));
        onDone();
    }

    @Override
    public void onFailure(Throwable t) {
        onFailed(t);
        onDone();
    }

    public abstract void onRespond(Response<T> response, Retrofit retrofit);

    public abstract void onFailed(Throwable t);

    public void onDone() {}

}
