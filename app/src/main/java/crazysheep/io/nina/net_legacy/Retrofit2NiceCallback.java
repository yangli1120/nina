package crazysheep.io.nina.net_legacy;

import crazysheep.io.nina.utils.Utils;
import retrofit2.Call;
import retrofit2.Response;

/**
 * nice callback implement {@link retrofit.Callback}
 *
 * Created by crazysheep on 16/1/27.
 */
abstract class Retrofit2NiceCallback<T> implements retrofit2.Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.code() == HttpConstants.CODE_200)
            onRespond(call, response);
        else
            onFailed(new Throwable("request failed, status code: " + response.code()
                    + ", response header: " + response.headers().toString()
                    + ", response body: " + (Utils.isNull(response.body())
                    ? null : response.body().toString())));
        onDone();
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onFailed(t);
        onDone();
    }

    public abstract void onRespond(Call<T> call, Response<T> response);

    public abstract void onFailed(Throwable t);

    public void onDone() {}

}
