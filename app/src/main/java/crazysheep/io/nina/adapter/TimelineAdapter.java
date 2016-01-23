package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.List;

import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.holder.timeline.BaseHolder;
import crazysheep.io.nina.holder.timeline.TimelineHolderFactory;

/**
 * adapter for twitter timeline, see{@link crazysheep.io.nina.fragment.TimelineFragment}
 *
 * Created by crazysheep on 16/1/23.
 */
public class TimelineAdapter<T extends BaseHolder> extends RecyclerViewBaseAdapter<T, TweetDto> {

    public TimelineAdapter(@NonNull Context context, List<TweetDto> data) {
        super(context, data);
    }

    @Override
    protected T onCreateHolder(ViewGroup parent, int viewType) {
        return TimelineHolderFactory.createHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(T holder, int position) {
        holder.bindData(getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineHolderFactory.getViewType(getItem(position));
    }

}
