package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import crazysheep.io.nina.bean.TweetDto;

/**
 * timeline holder factory
 *
 * Created by crazysheep on 16/1/23.
 */
public class TimelineHolderFactory {

    public static final int TYPE_TXT = 1;

    /**
     * create view holder
     * */
    @SuppressWarnings("unchecked")
    public static <T extends BaseHolder> T createHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TXT: {
                return (T) new TxtHolder(parent, parent.getContext());
            }

            default:
                return (T) new TxtHolder(parent, parent.getContext());
        }
    }

    /**
     * parse view holder type from tweet data
     * */
    public static int getViewType(@NonNull TweetDto tweetDto) {
        return TYPE_TXT;
    }

}
