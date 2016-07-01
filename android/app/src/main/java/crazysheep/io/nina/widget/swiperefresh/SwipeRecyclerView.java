package crazysheep.io.nina.widget.swiperefresh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * swipe recycler view
 *
 * Created by crazysheep on 16/1/27.
 */
public class SwipeRecyclerView extends SwipeRefreshBase<RecyclerView> {

    private LoadMoreRecyclerViewAdapter mSwipeRefreshAdapter;

    public SwipeRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeRecyclerView(Context context) {
        super(context);
    }

    @Override
    protected RecyclerView createRefreshableView() {
        return new RecyclerView(getContext());
    }

    public void setLayoutManager(@NonNull RecyclerView.LayoutManager layoutMgr) {
        getRefreshableView().setLayoutManager(layoutMgr);
    }

    public void setItemAnimator(@NonNull RecyclerView.ItemAnimator itemAnimator) {
        getRefreshableView().setItemAnimator(itemAnimator);
    }

    public void setAdapter(@NonNull RecyclerView.Adapter adapter) {
        mSwipeRefreshAdapter = new LoadMoreRecyclerViewAdapter(getContext(), adapter, enableLoadMore);
        getRefreshableView().setAdapter(mSwipeRefreshAdapter);
    }

    private boolean enableLoadMore = false; // disable by default
    private int mOldLoadMoreLastItemPos= -1;
    private OnLoadMoreListener mOnLoadMoreListener;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if(getRefreshableView().getLayoutManager() != null
                    && getRefreshableView().getAdapter() != null) {
                if(getRefreshableView().getLayoutManager() instanceof LinearLayoutManager) {
                    LinearLayoutManager layoutMgr = (LinearLayoutManager)getRefreshableView()
                            .getLayoutManager();
                    int visibleItemCount = layoutMgr.getChildCount();
                    int totalItemCount = layoutMgr.getItemCount();
                    int firstVisibleItemPos = layoutMgr.findFirstVisibleItemPosition();
                    int lastVisibleItemPos = firstVisibleItemPos + visibleItemCount - 1;
                    if(mOldLoadMoreLastItemPos != lastVisibleItemPos
                            && lastVisibleItemPos == totalItemCount - 1) {
                        mOldLoadMoreLastItemPos = lastVisibleItemPos;
                        if(null != mOnLoadMoreListener)
                            mOnLoadMoreListener.onLoadMore();
                    }
                }
            }
        }
    };

    @Override
    public void setRefreshing(boolean refresh) {
        super.setRefreshing(refresh);

        // when start a new refresh action, reset mOldLoadMoreLastItemPos
        // that mean we cancel previous load more action
        mOldLoadMoreLastItemPos = -1;
    }

    public void setEnableLoadMore(boolean enableLoadMore) {
        if(enableLoadMore)
            getRefreshableView().addOnScrollListener(mScrollListener);
        else
            getRefreshableView().removeOnScrollListener(mScrollListener);
        this.enableLoadMore = enableLoadMore;
        mSwipeRefreshAdapter.setLoadMore(enableLoadMore);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

}
