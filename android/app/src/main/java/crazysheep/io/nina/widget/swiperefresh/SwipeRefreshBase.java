package crazysheep.io.nina.widget.swiperefresh;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A FrameLayout to wrap SwipeRefresh parent
 *
 * Created by crazysheep on 16/1/27.
 *
 * */
public abstract class SwipeRefreshBase<T extends View> extends FrameLayout {

    private VerticalSwipeRefreshLayout mSwipeParent;
    private T mRefreshableView;
    private OnRefreshListener mListener;

    private boolean swipeRefreshEnabled = true;

    public SwipeRefreshBase(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SwipeRefreshBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeRefreshBase(Context context) {
        super(context);
        init();
    }

    protected void init() {
        mRefreshableView = createRefreshableView();
        //add swipe parent to myself
        mSwipeParent = new VerticalSwipeRefreshLayout(getContext());
        mSwipeParent.setEnabled(swipeRefreshEnabled);
        addView(mSwipeParent, -1, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mSwipeParent.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (mListener != null)
                    mListener.onRefresh();
            }
        });
        mSwipeParent.setProgressViewOffset(true, 0, 100);

        if(mRefreshableView != null)
            addRefreshableView(mRefreshableView);
    }

    public boolean isSwipeRefreshEnabled() {
        return swipeRefreshEnabled;
    }

    public void setSwipeRefreshEnabled(boolean enabled) {
        swipeRefreshEnabled = enabled;
    }

    public OnRefreshListener getOnRefreshListener(){
    	return mListener;
    }
    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public void setRefreshing(boolean refresh) {
        if(mSwipeParent != null) {
            mSwipeParent.setRefreshing(refresh);
        }
    }

    /**
     * do refreshing action
     * 
     * @param delayMillis The time to delay
     * */
    public void doRefreshing(long delayMillis) {
        if(mListener != null) {
            if(delayMillis > 0)
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mListener.onRefresh();
                    }
                }, delayMillis);
            else
                mListener.onRefresh();
        }
    }

    protected abstract T createRefreshableView();

    public T getRefreshableView() {
        return mRefreshableView;
    }

    // add child view to swipe parent
    private void addRefreshableView(View view) {
        if(mSwipeParent != null) {
            mSwipeParent.addView(view, -1, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }else{
            addView(view, -1, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    /////////////////////////Listener////////////////////////////
    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

}
