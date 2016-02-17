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
import crazysheep.io.nina.net_new.NiceCallback;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.widget.swiperefresh.SwipeRecyclerView;
import crazysheep.io.nina.widget.swiperefresh.SwipeRefreshBase;
import retrofit.client.Response;

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
        requestTimeline();

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
                requestTimeline();
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
    private void requestTimeline() {
        NiceCallback.cancel(TAG);
        mTwitter.getHomeTimeline(PAGE_SIZE, null, new NiceCallback<List<TweetDto>>(TAG) {
            @Override
            public void onRespond(List<TweetDto> tweetDtos, retrofit.client.Response response) {
                mAdapter.setData(tweetDtos);
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

        NiceCallback.cancel(TAG);
        mTwitter.getHomeTimeline(NEXT_PAGE_SIZE, oldestTweetDto.id,
                new NiceCallback<List<TweetDto>>(TAG) {
                    @Override
                    public void onRespond(List<TweetDto> tweetDtos, Response response) {
                        tweetDtos.remove(0); // remove repeat one
                        mAdapter.addData(tweetDtos);
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
        requestTimeline();
    }

}
