package crazysheep.io.nina.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * rx worker
 *
 * Created by crazysheep on 16/2/2.
 */
public class RxWorker extends Utils {

    public interface Callback {
        void onSuccess();
        void onFailed(String err);
    }

    /**
     * delay a task on UI thread
     *
     * @param activity The activity start task
     * @param milliseconds Time to delay
     * @param runnable The task
     * */
    public static void delayOnUI(@NonNull Activity activity, int milliseconds,
                                 @NonNull Runnable runnable) {
        final WeakReference<Activity> mActivityRef = new WeakReference<>(activity);
        Observable.just(runnable)
                .delay(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Runnable>() {
                    @Override
                    public void call(Runnable runnable) {
                        if(!isNull(mActivityRef.get()))
                            runnable.run();
                        mActivityRef.clear();
                    }
                });
    }

    /**
     * do background task
     *
     * @param runnable Task should run on background thread
     * @param callback The callback
     * */
    public static void doBackround(@NonNull Runnable runnable, @NonNull final Callback callback) {
        Observable.just(runnable)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Runnable, Boolean>() {
                    @Override
                    public Boolean call(Runnable runnable) {
                        try {
                            runnable.run();
                            return true;
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onFailed(Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (Utils.isNull(aBoolean) || !aBoolean)
                            callback.onFailed("unknow error");
                        else
                            callback.onSuccess();
                    }
                });
    }

}
