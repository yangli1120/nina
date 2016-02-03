package crazysheep.io.nina.utils;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * rx worker
 *
 * Created by crazysheep on 16/2/2.
 */
public class RxWorker extends Utils {

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
}
