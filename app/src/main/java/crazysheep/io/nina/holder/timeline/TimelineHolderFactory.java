package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.ITweet;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.utils.Utils;

/**
 * timeline holder factory
 *
 * Created by crazysheep on 16/1/23.
 */
public class TimelineHolderFactory {

    private static final String MEDIA_TYPE_PHOTO = "photo";
    private static final String MEDIA_TYPE_ANIMATED_GIF = "animated_gif";

    public static final int TYPE_DRAFT = R.id.view_holder_draft;
    public static final int TYPE_TXT = R.id.view_holder_txt;
    public static final int TYPE_IMAGE = R.id.view_holder_image;
    public static final int TYPE_GIF = R.id.view_holder_gif;

    /**
     * create view holder
     * */
    @SuppressWarnings("unchecked")
    public static <T extends BaseHolder> T createHolder(@NonNull LayoutInflater inflater,
                                                        @NonNull ViewGroup parent, int viewType) {
        ViewGroup itemRoot;
        if(isNormalHolder(viewType))
            itemRoot = (ViewGroup)inflater.inflate(R.layout.item_timeline_base, parent, false);
        else if(isDraftHolder(viewType))
            itemRoot = (ViewGroup)inflater.inflate(R.layout.item_timeline_draft_base, parent, false);
        else
            throw new RuntimeException(
                    "TimelineHolderFactory.createHolder(), unknow viewType: " + viewType);

        switch (viewType) {
            ///////////////// normal draft ////////////////////
            case TYPE_TXT: {
                return (T) new TxtHolder(itemRoot);
            }

            case TYPE_IMAGE: {
                return (T) new ImageHolder(itemRoot);
            }

            case TYPE_GIF: {
                return (T) new GifHolder(itemRoot);
            }

            ///////////////// draft item ///////////////////

            case TYPE_DRAFT: {
                return (T) new DraftHolder(itemRoot);
            }

            default:
                return (T) new TxtHolder(itemRoot);
        }
    }

    private static boolean isDraftHolder(int viewType) {
        switch (viewType) {
            case TYPE_DRAFT:
                return true;
        }

        return false;
    }

    private static boolean isNormalHolder(int viewType) {
        switch (viewType) {
            case TYPE_TXT:
            case TYPE_GIF:
            case TYPE_IMAGE:
                return true;
        }

        return false;
    }

    /**
     * parse view holder type from tweet data
     * */
    public static int getViewType(@NonNull ITweet iTweet) {
        // TODO more draft type may be, for good UX
        if(iTweet instanceof PostTweetBean) {
            return TYPE_DRAFT;
        } else if(iTweet instanceof TweetDto) {
            TweetDto tweetDto = (TweetDto) iTweet;
            if(!Utils.isNull(tweetDto.extended_entities)
                    && !Utils.isNull(tweetDto.extended_entities.media)) {
                if(MEDIA_TYPE_PHOTO.equals(tweetDto.extended_entities.media.get(0).type))
                    return TYPE_IMAGE;
                else if(MEDIA_TYPE_ANIMATED_GIF.equals(
                        tweetDto.extended_entities.media.get(0).type))
                    return TYPE_GIF;
            }
        }

        return TYPE_TXT;
    }

}
