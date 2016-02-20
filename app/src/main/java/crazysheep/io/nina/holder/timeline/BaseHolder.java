package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import crazysheep.io.nina.bean.ITweet;

/**
 * base holder for all view holder
 *
 * Created by crazysheep on 16/2/20.
 */
public class BaseHolder<T extends ITweet> extends RecyclerView.ViewHolder {

    public BaseHolder(@NonNull ViewGroup parent) {
        super(parent);
    }

    public void bindData(int position, T t) {}
}
