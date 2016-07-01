package crazysheep.io.nina.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * rx video
 *
 * Created by crazysheep on 16/3/25.
 */
public class RxVideo {

    public interface Callback {
        void onSuccess(List<String> sources, String targetFilePath);
        void onFailed(String err);
    }

    /**
     * merge videos to one video file
     * */
    public static Subscription merge(
            @NonNull final List<String> sources, final String targetPath,
            @NonNull final Callback callback) {
        return Observable.just(sources)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<String>, Boolean>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        List<Movie> movies = new ArrayList<>();
                        for (String filepath : strings)
                            if (new File(filepath).exists()) {
                                try {
                                    Movie movie = MovieCreator.build(filepath);
                                    movies.add(movie);
                                } catch (IOException e) {
                                    e.printStackTrace();

                                    throw Exceptions.propagate(e);
                                }
                            }
                        List<Track> audioTracks = new ArrayList<>();
                        List<Track> videoTracks = new ArrayList<>();
                        for (Movie movie : movies) {
                            for (Track track : movie.getTracks()) {
                                if (track.getHandler().equals("soun")) {
                                    audioTracks.add(track);
                                } else if (track.getHandler().equals("vide")) {
                                    videoTracks.add(track);
                                }
                            }
                        }
                        Movie result = new Movie();
                        try {
                            if (Utils.size(audioTracks) > 0)
                                result.addTrack(new AppendTrack(audioTracks.toArray(
                                        new Track[audioTracks.size()])));
                            if (Utils.size(videoTracks) > 0)
                                result.addTrack(new AppendTrack(videoTracks.toArray(
                                        new Track[videoTracks.size()])));
                        } catch (IOException ioe) {
                            ioe.printStackTrace();

                            throw Exceptions.propagate(ioe);
                        }

                        Container container = new DefaultMp4Builder().build(result);
                        FileChannel fc = null;
                        try {
                            fc = new RandomAccessFile(targetPath, "rw").getChannel();
                            container.writeContainer(fc);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();

                            throw Exceptions.propagate(ioe);
                        } finally {
                            if (!Utils.isNull(fc))
                                try {
                                    fc.close();
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                }
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
                        if (!Utils.isNull(aBoolean)
                                && aBoolean)
                            callback.onSuccess(sources, targetPath);
                    }
                });
    }

}
