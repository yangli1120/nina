package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import crazysheep.io.nina.R;
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

    public static final int TYPE_TXT = R.id.view_holder_txt;
    public static final int TYPE_IMAGE = R.id.view_holder_image;
    public static final int TYPE_GIF = R.id.view_holder_gif;

    /**
     * create view holder
     * */
    @SuppressWarnings("unchecked")
    public static <T extends BaseHolder> T createHolder(@NonNull ViewGroup itemRoot, int viewType) {
        switch (viewType) {
            case TYPE_TXT: {
                return (T) new TxtHolder(itemRoot);
            }

            case TYPE_IMAGE: {
                return (T) new ImageHolder(itemRoot);
            }

            case TYPE_GIF: {
                return (T) new GifHolder(itemRoot);
            }

            default:
                return (T) new TxtHolder(itemRoot);
        }
    }

    /**
     * parse view holder type from tweet data
     * */
    public static int getViewType(@NonNull TweetDto tweetDto) {
        if(!Utils.isNull(tweetDto.extended_entities)
                && !Utils.isNull(tweetDto.extended_entities.media)) {
            if(MEDIA_TYPE_PHOTO.equals(tweetDto.extended_entities.media.get(0).type))
                return TYPE_IMAGE;
            else if(MEDIA_TYPE_ANIMATED_GIF.equals(tweetDto.extended_entities.media.get(0).type))
                return TYPE_GIF;
        }

        return TYPE_TXT;
    }

}
