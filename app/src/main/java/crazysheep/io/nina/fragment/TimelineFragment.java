package crazysheep.io.nina.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.MainActivity;
import crazysheep.io.nina.PostTweetActivity;
import crazysheep.io.nina.R;
import crazysheep.io.nina.adapter.TimelineAdapter;
import crazysheep.io.nina.bean.ITweet;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.constants.EventBusConstants;
import crazysheep.io.nina.net.HttpCache;
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.net.RxTweeting;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.Utils;
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

    private static final int REQUEST_POST_TWEET = 111;

    private static final int PAGE_SIZE = 20; // tweet count every request
    private static final int NEXT_PAGE_SIZE = PAGE_SIZE + 1; // because twitter api will also return
                                                             // max_id tweet

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.data_rv) SwipeRecyclerView mTimelineRv;

    private TimelineAdapter mAdapter;

    private Call<List<TweetDto>> mTimelineCall;

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        View contentView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, contentView);

        initUI();
        requestTimeline();

        return contentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
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
        if(!Utils.isNull(mTimelineCall))
            mTimelineCall.cancel();

        mTimelineCall = mTwitter.getHomeTimeline(HttpCache.CACHE_IF_HIT, null, PAGE_SIZE);
        mTimelineCall.enqueue(new NiceCallback<List<TweetDto>>() {
            @Override
            public void onRespond(Response<List<TweetDto>> response) {
                // filter draft from adapter, add to front
                List<ITweet> items = new ArrayList<>();
                items.addAll(mAdapter.getDraftItems());
                for (TweetDto tweetDto : response.body())
                    items.add(tweetDto);
                mAdapter.setData(items);
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
        if(!Utils.isNull(mTimelineCall))
            mTimelineCall.cancel();

        TweetDto oldestTweetDto = (TweetDto)mAdapter.getItem(mAdapter.getItemCount() - 1);
        mTimelineCall = mTwitter.getHomeTimeline(HttpCache.CACHE_NETWORK,
                oldestTweetDto.id, NEXT_PAGE_SIZE);
        mTimelineCall.enqueue(new NiceCallback<List<TweetDto>>() {
            @Override
            public void onRespond(Response<List<TweetDto>> response) {
                response.body().remove(0); // remove repeat one
                mAdapter.addData(response.body());
            }

            @Override
            public void onFailed(Throwable t) {
                L.d(t.toString());
            }
        });
    }

    @Override
    protected void onErrorClick() {
        requestTimeline();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_POST_TWEET: {
                    // TODO update timeline ui, show draft item
                    PostTweetBean postTweetBean = data.getParcelableExtra(
                            BundleConstants.EXTRA_POST_TWEET);
                    if(!Utils.isNull(postTweetBean)) {
                        mAdapter.addDataToFirst(postTweetBean);
                        mTimelineRv.getRefreshableView().smoothScrollToPosition(0);
                    }
                }break;
            }
        }
    }

    @OnClick(R.id.action_fab)
    protected void clickFab() {
        ActivityUtils.startResult(this, REQUEST_POST_TWEET,
                ActivityUtils.prepare(getActivity(), PostTweetActivity.class));
    }

    @SuppressWarnings("unchecked, unused")
    @Subscribe(priority = EventBusConstants.PRIORITY_LOW)
    public void onEvent(@NonNull RxTweeting.EventPostTweetSuccess event) {
        // post tweet successful, replace draft UI to tweet UI
        for(PostTweetBean postTweetBean : (List<PostTweetBean>)mAdapter.getDraftItems())
            if(postTweetBean.randomId.equals(event.getPostTweetBean().randomId)) {
                mAdapter.removeItem(postTweetBean);
                mAdapter.insertData(mAdapter.getDraftItems().size(), event.getTweet());
            }
    }

    @SuppressWarnings("unchecked, unused")
    @Subscribe(priority = EventBusConstants.PRIORITY_LOW)
    public void onEvent(@NonNull RxTweeting.EventPostTweetFailed event) {
        // post tweet failed, update draft UI to failed state
        for(PostTweetBean postTweetBean : (List<PostTweetBean>) mAdapter.getDraftItems())
            if(postTweetBean.randomId.equals(event.getPostTweetBean().randomId)) {
                postTweetBean.setFailed();
                mAdapter.notifyItemChanged(mAdapter.findItemPosition(postTweetBean));
            }
    }

}
