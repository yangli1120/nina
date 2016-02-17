package crazysheep.io.nina.net_new;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    ////////////////////// callback manager ///////////////////////////

    public static class CallbackManager {

        private static Map<String, NiceCallback> mCallbacks = new HashMap<>();

        public void add(NiceCallback callback) {
            mCallbacks.put(callback.getTag(), callback);
        }

        public void cancel(String tag) {
            NiceCallback callback = mCallbacks.get(tag);
            if(!Utils.isNull(callback)) {
                callback.cancel();
                // remove this callback from map
                mCallbacks.remove(callback);
            }
        }

        public void cancelAll() {
            for(NiceCallback callback : mCallbacks.values()) {
                if(!Utils.isNull(callback)) {
                    callback.cancel();
                    // remove from map
                    mCallbacks.remove(callback);
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////

    private String TAG = UUID.randomUUID().toString();

    private static CallbackManager mCallbackMgr;
    private boolean isCanceled = false;

    public String getTag() {
        return TAG;
    }

    /**
     * return CallbackManager
     * */
    private static CallbackManager getCallbackManager() {
        if(Utils.isNull(mCallbackMgr))
            mCallbackMgr = new CallbackManager();

        return mCallbackMgr;
    }

    public static void cancel(@NonNull String tag) {
        getCallbackManager().cancel(tag);
    }

    public void cancel() {
        isCanceled = true;
    }

    public NiceCallback(String tag) {
        TAG = tag;
        getCallbackManager().add(this);
    }

    @Override
    public void success(T t, Response response) {
        if(isCanceled)
            return;

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
        if(isCanceled)
            return;

        onFailed(new Throwable(Utils.isNull(error) ? "unknow error"
                : printResponse(error.getResponse())));
        onDone();
    }

    private static String printResponse(Response response) {
        if(!Utils.isNull(response)) {
            StringBuilder sb = new StringBuilder();
            sb.append("\"")
                    .append(response.getUrl())
                    .append("\"")
                    .append(" response:");
            // headers
            if(!Utils.isNull(response.getHeaders()))
                sb.append("\n---headers---\n")
                        .append(response.getHeaders().toString());
            // body
            if(!Utils.isNull(response.getBody()))
                sb.append("\n---body---\n")
                        .append(response.getBody().toString());

            return sb.toString();
        }

        return null;
    }

    public abstract void onRespond(T t, Response response);
    public abstract void onFailed(Throwable t);

    public void onDone() {}

}
