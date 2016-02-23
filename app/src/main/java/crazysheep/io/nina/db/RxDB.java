package crazysheep.io.nina.db;

import android.content.ContentResolver;
import android.support.annotation.NonNull;

import java.util.List;

import crazysheep.io.nina.bean.MediaStoreImageBean;
import crazysheep.io.nina.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * rx java database operation
 *
 * Created by crazysheep on 16/2/23.
 */
public class RxDB {

    //////////////////////// callback ///////////////////////////

    public interface Callback<T> {
        void onResult(T t);
        void onFailed(String err);
    }
    /////////////////////////////////////////////////////////////

    /**
     * query system media store, get all images on external storage
     * */
    public static Subscription getAllImages(
            @NonNull ContentResolver resolver,
            @NonNull final Callback<List<MediaStoreImageBean>> callback) {
        return Observable.just(resolver)
                .subscribeOn(Schedulers.io())
                .map(new Func1<ContentResolver, List<MediaStoreImageBean>>() {
                    @Override
                    public List<MediaStoreImageBean> call(ContentResolver resolver) {
                        return MediaStoreHelper.getAllImages(resolver);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<MediaStoreImageBean>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onFailed(Utils.isNull(e) ? "query images failed" : e.toString());
                    }

                    @Override
                    public void onNext(List<MediaStoreImageBean> mediaStoreImageBeans) {
                        callback.onResult(mediaStoreImageBeans);
                    }
                });
    }

}
