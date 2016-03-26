package crazysheep.io.nina.io;

import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import crazysheep.io.nina.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * rx file
 *
 * Created by crazysheep on 16/3/25.
 */
public class RxFile {

    public interface Callback {
        void onSuccess();
        void onFailed(String err);
    }

    /**
     * copy source file to target file
     * */
    public static Subscription copy(@NonNull final String source, @NonNull final String dest,
                                    @NonNull final Callback callback) {
        return Observable.just(source)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        try {
                            FileUtils.copyFile(new File(source), new File(dest), false);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();

                            throw Exceptions.propagate(ioe);
                        }
                        return true;
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
                        callback.onSuccess();
                    }
                });
    }

    /**
     * delete target file
     * */
    public static Subscription delete(@NonNull File file, final Callback callback) {
        return Observable.just(file)
                .subscribeOn(Schedulers.io())
                .map(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        try {
                            FileUtils.forceDelete(file);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();

                            throw  Exceptions.propagate(ioe);
                        }
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!Utils.isNull(callback))
                            callback.onFailed(Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if(!Utils.isNull(callback))
                            callback.onSuccess();
                    }
                });
    }

}
