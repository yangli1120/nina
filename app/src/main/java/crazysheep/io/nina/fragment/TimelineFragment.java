package crazysheep.io.nina.fragment;

import android.os.Bundle;
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
import crazysheep.io.nina.net.ApiService;
import crazysheep.io.nina.net.HttpCache;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.widget.swiperefresh.SwipeRecyclerView;
import crazysheep.io.nina.widget.swiperefresh.SwipeRefreshBase;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * fragment show twitter timeline
 *
 * Created by crazysheep on 16/1/22.
 */
public class TimelineFragment extends BaseFragment {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.data_rv) SwipeRecyclerView mTimelineRv;

    private TimelineAdapter mAdapter;

    private ApiService mHttp;

    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, contentView);

        initUI();
        requestTimeline(false);

        return contentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHttp = mRetrofit.create(ApiService.class);
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
    }

    @SuppressWarnings("unchecked")
    private void requestTimeline(boolean force) {
        int cacheType = force ? HttpCache.CacheConfig.CACHE_NO : HttpCache.CacheConfig.CACHE_IF_HIT;

        DebugHelper.log("request force: " + force);
        mHttp.getHomeTimeline(cacheType, 50).enqueue(new retrofit.Callback<List<TweetDto>>() {
            @Override
            public void onResponse(Response<List<TweetDto>> response, Retrofit retrofit) {
                DebugHelper.log("request done: " + response.toString());
                mTimelineRv.setRefreshing(false);

                mAdapter.setData(response.body());
            }

            @Override
            public void onFailure(Throwable t) {
                mTimelineRv.setRefreshing(false);

                L.d(t.toString());
            }
        });
    }

}
