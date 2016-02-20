package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import crazysheep.io.nina.bean.PostTweetBean;

/**
 * draft holder
 *
 * Created by crazysheep on 16/2/19.
 */
public class DraftHolder extends BaseHolder<PostTweetBean> {

    public DraftHolder(@NonNull ViewGroup view) {
        super(view);
    }

    @Override
    public void bindData(int position, PostTweetBean postTweetBean) {
        // TODO bind draft UI with data
    }
}
