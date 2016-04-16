package crazysheep.io.nina.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import crazysheep.io.nina.net.HttpClient;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

/**
 * nina's glide module
 *
 * Created by crazysheep on 16/2/2.
 */
public class NinaGlideModel implements GlideModule {

    public static final int DISK_CACHE_SIZE = 500 * 1024 * 1024; // 500m

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888)
                .setDiskCache(new ExternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        // let Glide integration with okhttp client
        glide.register(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(HttpClient.getInstance().getOkHttpClient()));
    }

    /**
     * ui progress listener
     *
     * see{@link https://gist.github.com/TWiStErRob/08d5807e396740e52c90}
      */
    public static class ProgressInterceptor implements Interceptor {

        private static final Map<String, UIProgressListener> LISTENERS = new HashMap<>();
        private static final Map<String, Long> PROGRESSES = new HashMap<>();

        public static void listen(@NonNull String url, @NonNull UIProgressListener listener) {
            if(!TextUtils.isEmpty(url)) {
                LISTENERS.put(url, listener);
            }
        }

        public static void forget(@NonNull String url) {
            if(!TextUtils.isEmpty(url)) {
                LISTENERS.remove(url);
                PROGRESSES.remove(url);
            }
        }

        private Handler mHandler;

        public ProgressInterceptor() {
            mHandler = new Handler(Looper.getMainLooper());
        }

        protected void update(@NonNull HttpUrl httpUrl, final long currentBytesRead,
                              final long totalBytes) {
            String url = httpUrl.toString();
            final UIProgressListener listenerRefs = LISTENERS.get(url);
            if(listenerRefs == null)
                return;

            if(needDispatch(url, currentBytesRead, totalBytes,
                    listenerRefs.getGranularityPercentage()))
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listenerRefs.onProgress(currentBytesRead, totalBytes);
                    }
                });

            if(currentBytesRead >= totalBytes)
                forget(url);
        }

        private boolean needDispatch(@NonNull String url, long currentBytesRead, long totalBytes,
                                  float percentage) {
            if(currentBytesRead == 0 || totalBytes == 0 || currentBytesRead >= totalBytes)
                return true;

            if(!PROGRESSES.containsKey(url)) {
                PROGRESSES.put(url, currentBytesRead);
                return true;
            }

            long lastProgress = PROGRESSES.get(url);
            if(currentBytesRead - lastProgress >= Math.round(totalBytes * percentage)
                    || currentBytesRead >= totalBytes) {
                PROGRESSES.put(url, currentBytesRead);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            return response.newBuilder()
                    .body(new OkHttpProgressResponseBody(request.url(), response.body(), this))
                    .build();
        }

    }

    /**
     * custom {@link ResponseBody} to listen request progress
     * */
    private static class OkHttpProgressResponseBody extends ResponseBody {

        private HttpUrl mHttpUrl;
        private ResponseBody mResponseBody;
        private ProgressInterceptor mInterceptor;
        private BufferedSource mSource;

        public OkHttpProgressResponseBody(
                @NonNull HttpUrl httpUrl, @NonNull ResponseBody responseBody,
                @NonNull ProgressInterceptor progressInterceptor) {
            mHttpUrl = httpUrl;
            mResponseBody = responseBody;
            mInterceptor = progressInterceptor;
        }

        @Override
        public long contentLength() {
            return mResponseBody.contentLength();
        }

        @Override
        public MediaType contentType() {
            return mResponseBody.contentType();
        }

        @Override
        public BufferedSource source() {
            if(mSource == null) {
                mSource = Okio.buffer(new ForwardingSource(mResponseBody.source()) {

                    private long mCurrentBytesTotalRead = 0L;

                    @Override
                    public long read(Buffer sink, long byteCount) throws IOException {
                        long bytesRead = super.read(sink, byteCount);
                        long fullLength = mResponseBody.contentLength();
                        if(bytesRead <= 1) {
                            mCurrentBytesTotalRead = fullLength;
                        } else {
                            mCurrentBytesTotalRead += bytesRead;
                        }
                        mInterceptor.update(mHttpUrl, mCurrentBytesTotalRead, fullLength);

                        return bytesRead;
                    }
                });
            }

            return mSource;
        }
    }

    public interface UIProgressListener {
        /**
         * callback about current request progress
         * */
        void onProgress(long currentBytesRead, long totalBytes);
        /**
         * Control how often the listener needs an update. 0% and 100% will always be dispatched.
         * @return in percentage (0.2 = call {@link #onProgress} around every 0.2 percent of progress)
         */
        float getGranularityPercentage();
    }

    ///////////////////////// glide cutsom target /////////////////////////////////
    public abstract static class WrappingTarget<Z> implements Target<Z> {
        protected final Target<Z> target;

        public WrappingTarget(Target<Z> target) {
            this.target = target;
        }

        @Override
        public void getSize(SizeReadyCallback cb) {
            target.getSize(cb);
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            target.onLoadStarted(placeholder);
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            target.onLoadFailed(e, errorDrawable);
        }

        @Override
        public void onResourceReady(Z resource, GlideAnimation<? super Z> glideAnimation) {
            target.onResourceReady(resource, glideAnimation);
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            target.onLoadCleared(placeholder);
        }

        @Override
        public com.bumptech.glide.request.Request getRequest() {
            return target.getRequest();
        }

        @Override
        public void setRequest(com.bumptech.glide.request.Request request) {
            target.setRequest(request);
        }

        @Override
        public void onStart() {
            target.onStart();
        }

        @Override
        public void onStop() {
            target.onStop();
        }

        @Override
        public void onDestroy() {
            target.onDestroy();
        }
    }

    public static class ProgressTarget<T, Z> extends WrappingTarget<Z> implements UIProgressListener {
        private T model;
        private boolean ignoreProgress = true;

        public ProgressTarget(T model, Target<Z> target) {
            super(target);
            this.model = model;
        }

        public final T getModel() {
            return model;
        }

        public final void setModel(T model) {
            Glide.clear(this); // indirectly calls cleanup
            this.model = model;
        }

        /**
         * Convert a model into an Url string that is used to match up the OkHttp requests. For explicit
         * {@link com.bumptech.glide.load.model.GlideUrl GlideUrl} loads this needs to return
         * {@link com.bumptech.glide.load.model.GlideUrl#toStringUrl toStringUrl}. For custom models do the same as your
         * {@link com.bumptech.glide.load.model.stream.BaseGlideUrlLoader BaseGlideUrlLoader} does.
         *
         * @param model return the representation of the given model, DO NOT use {@link #getModel()} inside this method.
         * @return a stable Url representation of the model, otherwise the progress reporting won't work
         */
        protected String toUrlString(T model) {
            return String.valueOf(model);
        }

        @Override
        public float getGranularityPercentage() {
            return 0.2f;
        }

        @Override
        public void onProgress(long bytesRead, long expectedLength) {
            if (ignoreProgress) {
                return;
            }
            if (expectedLength == Long.MAX_VALUE) {
                onConnecting();
            } else if (bytesRead == expectedLength) {
                onDownloaded();
            } else {
                onDownloading(bytesRead, expectedLength);
            }
        }

        /**
         * Called when the Glide load has started.
         * At this time it is not known if the Glide will even go and use the network to fetch the image.
         */
        protected void onConnecting() {}

        /**
         * Called when there's any progress on the download; not called when loading from cache.
         * At this time we know how many bytes have been transferred through the wire.
         */
        protected void onDownloading(long bytesRead, long expectedLength) {}

        /**
         * Called when the bytes downloaded reach the length reported by the server; not called when loading from cache.
         * At this time it is fairly certain, that Glide either finished reading the stream.
         * This means that the image was either already decoded or saved the network stream to cache.
         * In the latter case there's more work to do: decode the image from cache and transform.
         * These cannot be listened to for progress so it's unsure how fast they'll be, best to show indeterminate progress.
         */
        protected void onDownloaded() {}

        /**
         * Called when the Glide load has finished either by successfully loading the image or failing to load or cancelled.
         * In any case the best is to hide/reset any progress displays.
         */
        protected void onDelivered() {}

        private void start() {
            ProgressInterceptor.listen(toUrlString(model), this);
            ignoreProgress = false;
            onProgress(0, Long.MAX_VALUE);
        }

        private void cleanup() {
            ignoreProgress = true;
            T model = this.model; // save in case it gets modified
            onDelivered();
            ProgressInterceptor.forget(toUrlString(model));
            this.model = null;
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            super.onLoadStarted(placeholder);
            start();
        }

        @Override
        public void onResourceReady(Z resource, GlideAnimation<? super Z> animation) {
            cleanup();
            super.onResourceReady(resource, animation);
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            cleanup();
            super.onLoadFailed(e, errorDrawable);
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            cleanup();
            super.onLoadCleared(placeholder);
        }
    }

}
