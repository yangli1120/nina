package crazysheep.io.nina.net;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.bean.UploadMediaDto;
import crazysheep.io.nina.utils.BitmapUtils;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.NetworkUtils;
import crazysheep.io.nina.utils.Utils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * post tweet helper, do everything background
 * <p/>
 * Created by crazysheep on 16/2/18.
 */
public class RxTweeting {

    //////////////////////// api ////////////////////////////////

    public static class EventPostTweetSuccess {
        private PostTweetBean postTweetBean;
        private TweetDto tweetDto;

        public EventPostTweetSuccess(@NonNull PostTweetBean postTweetBean,
                                     @NonNull TweetDto tweetDto) {
            this.postTweetBean = postTweetBean;
            this.tweetDto = tweetDto;
        }

        public PostTweetBean getPostTweetBean() {
            return this.postTweetBean;
        }

        public TweetDto getTweet() {
            return tweetDto;
        }
    }

    public static class EventPostTweetFailed {
        private PostTweetBean postTweetBean;
        private String error;

        public EventPostTweetFailed(@NonNull PostTweetBean postTweetBean, String err) {
            this.postTweetBean = postTweetBean;
            this.error = err;
        }

        public PostTweetBean getPostTweetBean() {
            return postTweetBean;
        }

        public String getError() {
            return error;
        }
    }

    /////////////////////////////////////////////////////////////

    /**
     * post a tweet background
     *
     * @param postTweet The tweet to post
     */
    public static Subscription postTweet(@NonNull final PostTweetBean postTweet) {
        // first step, check network state if is connected
        if(!NetworkUtils.isConnected(BaseApplication.getAppContext())) {
            EventBus.getDefault().post(
                    new EventPostTweetFailed(postTweet, "network is not avialable"));
            return null;
        }

        return Observable.just(postTweet)
                .subscribeOn(Schedulers.io())
                // second step, check if post tweet have media file to upload to twitter sever
                .map(new Func1<PostTweetBean, PostTweetBean>() {
                    @Override
                    public PostTweetBean call(PostTweetBean postTweetBean) {
                        // TODO upload media file and set media ids to post tweet bean if need
                        String[] uploadParams;
                        int uploadedFileCount = -1;
                        while (!Utils.isNull(uploadParams = uploadParams(postTweetBean))) {
                            uploadedFileCount++;
                            DebugHelper.log(String.format(
                                    "============== %d start upload file %s ==================",
                                    uploadedFileCount, uploadParams[0]));

                            try {
                                Response<UploadMediaDto> response = HttpClient.getInstance()
                                        .getTwitterService()
                                        .uploadPhoto(HttpConstants.UPLOAD_MEDIA_URL,
                                                uploadBody(postTweetBean))
                                        .execute();

                                if(response.code() == HttpConstants.CODE_200
                                        && !Utils.isNull(response.body())) {
                                    // update post tweet bean media ids and remove uploaded file
                                    // from post tweet bean
                                    handleUploadResult(postTweetBean, response);
                                } else {
                                    String err = "upload file \"" + uploadParams[0]
                                            + "\" failed, response: " + printResponse(response);
                                    DebugHelper.log(err);
                                    throw Exceptions.propagate(new Throwable(err));
                                }
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                                DebugHelper.log("upload file failed, error: " + ioe.toString());

                                throw  Exceptions.propagate(ioe);
                            }
                        }

                        return postTweetBean;
                    }
                })
                // final step, every thing is ready, post tweet
                .map(new Func1<PostTweetBean, TweetDto>() {
                    @Override
                    public TweetDto call(PostTweetBean post) {
                        DebugHelper.log("start post tweet: " + post.toString());
                        try {
                            Response<TweetDto> response = HttpClient.getInstance()
                                    .getTwitterService()
                                    .postTweet(post.getStatus(), post.getReplyStatusId(),
                                            post.getPlaceId(), post.isDisplayCoordinates(),
                                            post.getMediaIds())
                                    .execute();

                            if (response.code() == HttpConstants.CODE_200
                                    && !Utils.isNull(response.body()))
                                return response.body();
                            else {
                                // TODO post tweet error handle
                                String err = "request \"" + response.raw().request().url()
                                        + "\" response: " + printResponse(response);
                                DebugHelper.log("final post tweet failed 1, error: " + err);
                                throw Exceptions.propagate(new Throwable(err));
                            }
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                            DebugHelper.log("final post tweet failed 2, error: " + ioe.toString());

                            throw Exceptions.propagate(ioe);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TweetDto>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        // update post tweet bean post state
                        DebugHelper.log("post tweet failed, error: " + e.toString());
                        EventBus.getDefault().post(
                                new EventPostTweetFailed(postTweet, e.toString()));
                    }

                    @Override
                    public void onNext(TweetDto tweetDto) {
                        EventBus.getDefault().post(new EventPostTweetSuccess(postTweet, tweetDto));
                    }
                });
    }

    private static boolean havePhotos(@NonNull PostTweetBean postTweetBean) {
        return !Utils.isNull(postTweetBean.getPhotoFiles())
                && postTweetBean.getPhotoFiles().size() > 0;
    }

    private static boolean haveVideo(@NonNull PostTweetBean postTweetBean) {
        return !TextUtils.isEmpty(postTweetBean.getVideoFile());
    }

    private static String[] uploadParams(@NonNull PostTweetBean postTweetBean) {
        if(havePhotos(postTweetBean)) {
            return new String[] {postTweetBean.getPhotoFiles().get(0), "application/octet-stream"};
        } else if(haveVideo(postTweetBean)) {
            // TODO upload video
            return null;
        } else {
            return null; // no file to upload
        }
    }

    /**
     * create request body from post tweet bean if it contains upload file
     * */
    private static RequestBody uploadBody(@NonNull PostTweetBean postTweetBean) throws IOException {
        String[] uploadParams = uploadParams(postTweetBean);
        if(havePhotos(postTweetBean)) {
            // check if image file is large than 3M,
            // because twitter avoid a image file large than 3M when it attach to a tweet
            // see{@link https://twittercommunity.com/t/getting-media-parameter-is-invalid-after-successfully-uploading-media/58354/5}
            File imageFile = new File(uploadParams[0]);
            if(!imageFile.exists()) {
                throw Exceptions.propagate(new Throwable("file\""
                        + imageFile.getAbsolutePath() + "\" not exist"));
            } else if(imageFile.length() > HttpConstants.MAX_UPLOAD_PHOTO_SIZE) {
                // image file is too large, compress it let small than 3M
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                BitmapUtils.decodeFile2048(uploadParams[0])
                        .compress(Bitmap.CompressFormat.JPEG, 100, bos);
                return RequestBody.create(MediaType.parse(uploadParams[1]), bos.toByteArray());
            } else {
                return RequestBody.create(MediaType.parse(uploadParams[1]), imageFile);
            }
        } else if(haveVideo(postTweetBean)) {
            // TODO create video request body
            return null;
        } else {
            throw new RuntimeException("RxTweeting.uploadBody(), create request body failed");
        }
    }

    /**
     * handle result return by the upload media api
     * */
    private static PostTweetBean handleUploadResult(
            @NonNull PostTweetBean postTweetBean, @NonNull Response<UploadMediaDto> response) {
        String uploadedFile = null;
        if(havePhotos(postTweetBean)) {
            List<String> photoFiles = postTweetBean.getPhotoFiles();
            uploadedFile = photoFiles.remove(0);
            postTweetBean.setPhotoFiles(photoFiles);
        } else if(haveVideo(postTweetBean)) {
            uploadedFile = postTweetBean.getVideoFile();
            postTweetBean.setVideoFile(null);
        }
        // upload file successful, remove uploaded file and update media ids
        postTweetBean.appendMediaId(response.body().media_id_string);
        postTweetBean.save();

        DebugHelper.log("upload successful, remove file: " + uploadedFile
                + ", media ids: " + postTweetBean.getMediaIds());

        return postTweetBean;
    }

    private static String printResponse(@NonNull Response response) throws IOException {
        return new StringBuffer("[\n")
                .append("url:")
                .append(response.raw().request().url().toString())
                .append(", code:")
                .append(response.code())
                .append(", \nheader:")
                .append(Utils.isNull(response.headers()) ? null : response.headers().toString())
                .append(", \nbody:")
                .append(Utils.isNull(response.body()) ? null : response.body().toString())
                .append(", \nerrorBody:")
                .append(Utils.isNull(response.errorBody())
                        ? null : new String(response.errorBody().bytes(), "utf-8"))
                .append("]")
                .toString();
    }

}
