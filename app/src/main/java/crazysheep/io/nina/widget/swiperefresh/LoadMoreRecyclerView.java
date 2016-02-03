package crazysheep.io.nina.widget.swiperefresh;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * only load more RecyclerView, without pull to refresh
 *
 * Created by crazysheep on 16/2/2.
 */
public class LoadMoreRecyclerView extends RecyclerView {

    ///////////////////////// listener /////////////////////////////

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
    ///////////////////////////////////////////////////////////////

    private boolean isEnableLoadMore = false;
    private int mOldLoadMoreLastItemPos= -1;
    private LoadMoreRecyclerViewAdapter mLoadMoreAdapter;
    private RecyclerView.Adapter mRawAdapter;
    private OnLoadMoreListener mOnLoadMoreListener;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if(!InnerUtils.isNull(getLayoutManager())
                    && !InnerUtils.isNull(getAdapter())) {
                if(getLayoutManager() instanceof LinearLayoutManager) {
                    LinearLayoutManager layoutMgr = (LinearLayoutManager) getLayoutManager();
                    int visibleItemCount = layoutMgr.getChildCount();
                    int totalItemCount = layoutMgr.getItemCount();
                    int firstVisibleItemPos = layoutMgr.findFirstVisibleItemPosition();
                    int lastVisibleItemPos = firstVisibleItemPos + visibleItemCount - 1;
                    if(mOldLoadMoreLastItemPos != lastVisibleItemPos
                            && lastVisibleItemPos == totalItemCount - 1) {
                        mOldLoadMoreLastItemPos = lastVisibleItemPos;
                        if(!InnerUtils.isNull(mOnLoadMoreListener))
                            mOnLoadMoreListener.onLoadMore();
                    }
                }
            }
        }
    };

    public LoadMoreRecyclerView(Context context) {
        super(context);
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mRawAdapter = adapter;
        mLoadMoreAdapter = new LoadMoreRecyclerViewAdapter(getContext(), mRawAdapter,
                isEnableLoadMore);

        super.setAdapter(mLoadMoreAdapter);
    }

    @Override
    public Adapter getAdapter() {
        return mRawAdapter;
    }

    public void setLoadMoreEnable(boolean enable) {
        isEnableLoadMore = enable;
        if(isEnableLoadMore)
            addOnScrollListener(mScrollListener);
        else
            removeOnScrollListener(mScrollListener);
        if(!InnerUtils.isNull(mLoadMoreAdapter))
            mLoadMoreAdapter.setLoadMore(isEnableLoadMore);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

}
