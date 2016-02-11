package crazysheep.io.nina.fragment;

import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.MainActivity;
import crazysheep.io.nina.R;
import crazysheep.io.nina.adapter.TimelineAdapter;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.net.HttpCache;
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.widget.swiperefresh.SwipeRecyclerView;
import crazysheep.io.nina.widget.swiperefresh.SwipeRefreshBase;
import retrofit2.Call;
import retrofit2.Response;

/**
 * fragment show twitter timeline
 *
 * Created by crazysheep on 16/1/22.
 */
public class TimelineFragment extends BaseNetworkFragment {

    private static final int PAGE_SIZE = 20; // tweet count every request
    private static final int NEXT_PAGE_SIZE = PAGE_SIZE + 1; // because twitter api will also return
                                                             // max_id tweet

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.data_rv) SwipeRecyclerView mTimelineRv;

    private TimelineAdapter mAdapter;

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        View contentView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, contentView);

        initUI();
        requestTimeline(false);

        return contentView;
    }

    private void initUI() {
        if(getActivity() instanceof MainActivity)
            ((MainActivity)getActivity()).setToolbar(mToolbar);

        mAdapter = new TimelineAdapter(getActivity(), null);
        mTimelineRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTimelineRv.setItemAnimator(new DefaultItemAnimator());
        mTimelineRv.setAdapter(mAdapter);
        mTimelineRv.setOnRefreshListener(new SwipeRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestTimeline(true);
            }
        });
        mTimelineRv.setOnLoadMoreListener(new SwipeRefreshBase.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                requestTimelineNextPage();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void requestTimeline(boolean force) {
        int cacheType = force ? HttpCache.CacheConfig.CACHE_NETWORK
                : HttpCache.CacheConfig.CACHE_IF_HIT;

        mHttp.getHomeTimeline(cacheType, null, PAGE_SIZE)
                .enqueue(new NiceCallback<List<TweetDto>>() {
                    @Override
                    public void onRespond(Call<List<TweetDto>> call,
                                          Response<List<TweetDto>> response) {
                        mAdapter.setData(response.body());
                        mTimelineRv.setEnableLoadMore(true);
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        showError();
                        L.d(t.toString());
                    }

                    @Override
                    public void onDone() {
                        mTimelineRv.setRefreshing(false);
                    }
                });
    }

    // load more
    @SuppressWarnings("unchecked")
    private void requestTimelineNextPage() {
        TweetDto oldestTweetDto = (TweetDto)mAdapter.getItem(mAdapter.getItemCount() - 1);

        mHttp.getHomeTimeline(HttpCache.CacheConfig.CACHE_NETWORK, oldestTweetDto.id,
                        NEXT_PAGE_SIZE)
                .enqueue(new NiceCallback<List<TweetDto>>() {
                    @Override
                    public void onRespond(Call<List<TweetDto>> call,
                                          Response<List<TweetDto>> response) {
                        response.body().remove(0); // remove repeat one
                        mAdapter.addData(response.body());
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        showError();
                        L.d(t.toString());
                    }
                });
    }

    @Override
    protected void onErrorClick() {
        requestTimeline(false);
    }

}
