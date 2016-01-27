package crazysheep.io.nina.net;

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
        onRespond(response, retrofit);
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
