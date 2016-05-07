package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * adapter for tweet detail, see{@link crazysheep.io.nina.TweetDetailActivity}
 *
 * Created by crazysheep on 16/5/7.
 */
public class TweetDetailAdapter<VH extends RecyclerView.ViewHolder, DT>
        extends RecyclerViewBaseAdapter<VH, DT> {

    public TweetDetailAdapter(@NonNull Context context, List<DT> items) {
        super(context, items);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {

    }

    @Override
    protected VH onCreateHolder(ViewGroup parent, int viewType) {
        return null;
    }

}
