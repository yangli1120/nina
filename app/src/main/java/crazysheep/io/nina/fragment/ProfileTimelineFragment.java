package crazysheep.io.nina.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.adapter.FragmentPagerBaseAdapter;
import crazysheep.io.nina.adapter.TimelineAdapter;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.net_new.NiceCallback;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.widget.swiperefresh.LoadMoreRecyclerView;
import retrofit.client.Response;

/**
 * profile timeline fragment
 *
 * Created by crazysheep on 16/2/2.
 */
public class ProfileTimelineFragment extends BaseFragment
        implements FragmentPagerBaseAdapter.IPagerFragment, BaseFragment.INetworkFragment,
                   LoadMoreRecyclerView.OnLoadMoreListener {

    private static final int PAGE_SIZE = 21; // tweet count every request
    // tweet count every request we wanted
    private static final int PAGE_SIZE_WANTED = PAGE_SIZE - 1;

    @Bind(R.id.data_rv) LoadMoreRecyclerView mTimelineRv;
    private TimelineAdapter mTimelineAdapter;

    private String mScreenName;
    private boolean hasRequestFirstPage = false;

    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.profile_tab_tweet);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScreenName = getArguments().getString(BundleConstants.EXTRA_USER_SCREEN_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_profile_timeline, container, false);
        ButterKnife.bind(this, contentView);

        mTimelineAdapter = new TimelineAdapter(getActivity(), null);
        mTimelineRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTimelineRv.setAdapter(mTimelineAdapter);
        mTimelineRv.setOnLoadMoreListener(this);

        return contentView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(!hasRequestFirstPage && isVisibleToUser)
            requestFirstPage();
    }

    @Override
    public void onLoadMore() {
        requestNextPage();
    }

    @SuppressWarnings("unchecked")
    public void requestFirstPage() {
        hasRequestFirstPage = true;

        NiceCallback.cancel(TAG);
        mTwitter.getUserTimeline(PAGE_SIZE, mScreenName, null,
                new NiceCallback<List<TweetDto>>(TAG) {
                    @Override
                    public void onRespond(List<TweetDto> tweetDtos, Response response) {
                        // if server return tweet count equal PAGE_SIZE,
                        // that mean timeline have more tweets
                        if (tweetDtos.size() > PAGE_SIZE_WANTED) {
                            mTimelineRv.setLoadMoreEnable(true);
                            //tweetDtos.remove(tweetDtos.size() - 1); // remove lasted tweet
                        } else {
                            mTimelineRv.setLoadMoreEnable(false);
                        }
                        mTimelineAdapter.setData(tweetDtos);
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        L.d(t.toString());
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public void requestNextPage() {
        NiceCallback.cancel(TAG);
        long maxId = ((TweetDto)mTimelineAdapter.getItem(mTimelineAdapter.getItemCount() - 1)).id;
        mTwitter.getUserTimeline(PAGE_SIZE, mScreenName, maxId,
                new NiceCallback<List<TweetDto>>(TAG) {
                    @Override
                    public void onRespond(List<TweetDto> tweetDtos, Response response) {
                        if (tweetDtos.size() > PAGE_SIZE_WANTED)
                            mTimelineRv.setLoadMoreEnable(true);
                        else
                            mTimelineRv.setLoadMoreEnable(false);
                        tweetDtos.remove(0); // remove maxId tweet
                        mTimelineAdapter.addData(tweetDtos);
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        L.d(t.toString());
                    }
                });
    }

}
