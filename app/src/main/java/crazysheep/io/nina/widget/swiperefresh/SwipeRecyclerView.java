package crazysheep.io.nina.widget.swiperefresh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * swipe recycler view
 *
 * Created by crazysheep on 16/1/27.
 */
public class SwipeRecyclerView extends SwipeRefreshBase<RecyclerView> {

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

    public void setAdapter(@NonNull RecyclerView.Adapter adapter) {
        getRefreshableView().setAdapter(adapter);
    }

    public void setItemAnimator(@NonNull RecyclerView.ItemAnimator itemAnimator) {
        getRefreshableView().setItemAnimator(itemAnimator);
    }

    // TODO implement load more

}
