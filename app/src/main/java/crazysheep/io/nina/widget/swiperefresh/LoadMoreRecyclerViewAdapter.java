package crazysheep.io.nina.widget.swiperefresh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * RecyclerView.Adapter wrapper for support load more
 *
 * Created by crazysheep on 16/1/27.
 */
public class LoadMoreRecyclerViewAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {

    private static final int ITEM_TYPE_LOAD_MORE = -9527;

    private boolean isEnableLoadMore = false;

    private Context mContext;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            notifyItemMoved(fromPosition, toPosition);
        }
    };

    public LoadMoreRecyclerViewAdapter(@NonNull Context context, @NonNull RecyclerView.Adapter son,
                                       boolean enableLoadMore) {
        mContext = context;
        mAdapter = son; // yes, ur my son
        mAdapter.registerAdapterDataObserver(mObserver);

        isEnableLoadMore = enableLoadMore;
    }

    public void setLoadMore(boolean enableLoadMore) {
        if(isEnableLoadMore != enableLoadMore) {
            isEnableLoadMore = enableLoadMore;
            notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ITEM_TYPE_LOAD_MORE)
            return (T) new LoadMoreViewHolder(createLoadMoreItemView());
        else
            return (T) mAdapter.createViewHolder(parent, viewType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(T holder, int position) {
        if(getItemViewType(position) != ITEM_TYPE_LOAD_MORE)
            mAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if(isEnableLoadMore)
            return position == getItemCount() - 1
                    ? ITEM_TYPE_LOAD_MORE : mAdapter.getItemViewType(position);
        else
            return mAdapter.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return isEnableLoadMore ? mAdapter.getItemCount() + 1 : mAdapter.getItemCount();
    }

    private FrameLayout createLoadMoreItemView() {
        FrameLayout loadMoreRoot = new FrameLayout(mContext);
        RecyclerView.LayoutParams parentParams = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                InnerUtils.dp2Px(mContext, 56) // load more item 56 dp height
        );
        loadMoreRoot.setLayoutParams(parentParams);
        ProgressBar progressBar = new ProgressBar(mContext);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        loadMoreRoot.addView(progressBar, params);

        return loadMoreRoot;
    }

    ////////////////////////////// load more view holder //////////////////////////

    private static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        public LoadMoreViewHolder(@NonNull View view) {
            super(view);
        }
    }

}
