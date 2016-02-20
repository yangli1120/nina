package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import crazysheep.io.nina.bean.ITweet;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.holder.timeline.BaseHolder;
import crazysheep.io.nina.holder.timeline.TimelineHolderFactory;

/**
 * adapter for twitter timeline, see{@link crazysheep.io.nina.fragment.TimelineFragment}
 *
 * Created by crazysheep on 16/1/23.
 */
public class TimelineAdapter<T extends BaseHolder> extends RecyclerViewBaseAdapter<T, ITweet> {

    public TimelineAdapter(@NonNull Context context, List<ITweet> data) {
        super(context, data);
    }

    @Override
    protected T onCreateHolder(ViewGroup parent, int viewType) {
        return TimelineHolderFactory.createHolder(mInflater, parent, viewType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(T holder, int position) {
        holder.bindData(position, getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineHolderFactory.getViewType(getItem(position));
    }

    public List<ITweet> getDraftItems() {
        List<ITweet> drafts = new ArrayList<>();
        for(ITweet iTweet : getData())
            if(iTweet instanceof PostTweetBean)
                drafts.add(iTweet);

        return drafts;
    }

}
