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
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.swiperefresh.LoadMoreRecyclerView;
import retrofit2.Call;
import retrofit2.Response;

/**
 * profile like fragment
 *
 * Created by crazysheep on 16/2/2.
 */
public class ProfileLikeFragment extends BaseFragment
        implements FragmentPagerBaseAdapter.IPagerFragment, BaseFragment.INetworkFragment,
                   LoadMoreRecyclerView.OnLoadMoreListener{

    private static final int PAGE_SIZE = 21;
    private static final int PAGE_SIZE_WANTED = PAGE_SIZE - 1;

    @Bind(R.id.data_rv) LoadMoreRecyclerView mTimelineRv;
    private TimelineAdapter mAdapter;
    private Call<List<TweetDto>> mTimelineCall;
    private String mScreenName;

    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.profile_tab_like);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScreenName = getArguments().getString(BundleConstants.EXTRA_USER_SCREEN_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_profile_media_timeline, container,
                false);
        ButterKnife.bind(this, contentView);

        mTimelineRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTimelineRv.setOnLoadMoreListener(this);
        mAdapter = new TimelineAdapter(getActivity(), null);
        mTimelineRv.setAdapter(mAdapter);

        return contentView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(Utils.isNull(mTimelineCall) && isVisibleToUser)
            requestFirstPage();
    }

    @Override
    public void onLoadMore() {
        requestNextPage();
    }

    @SuppressWarnings("unchecked")
    private void requestFirstPage() {
        if(!Utils.isNull(mTimelineCall))
            mTimelineCall.cancel();
        // TODO use NinaTwitterApiClient
        mTimelineCall = mTwitter.getFavoritesTimeline(mScreenName, null, PAGE_SIZE);
        mTimelineCall.enqueue(new NiceCallback<List<TweetDto>>() {
            @Override
            public void onRespond(Response<List<TweetDto>> response) {
                if (response.body().size() > PAGE_SIZE_WANTED) {
                    mTimelineRv.setLoadMoreEnable(true);
                    response.body().remove(response.body().size() - 1);
                } else {
                    mTimelineRv.setLoadMoreEnable(false);
                }
                mAdapter.setData(response.body());
            }

            @Override
            public void onFailed(Throwable t) {
                L.d(t.toString());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void requestNextPage() {
        if(!Utils.isNull(mTimelineCall))
            mTimelineCall.cancel();
        long maxId = ((TweetDto)mAdapter.getItem(mAdapter.getItemCount() - 1)).id;
        mTimelineCall = mTwitter.getFavoritesTimeline(mScreenName, maxId, PAGE_SIZE);
        mTimelineCall.enqueue(new NiceCallback<List<TweetDto>>() {
            @Override
            public void onRespond(Response<List<TweetDto>> response) {
                if(response.body().size() > PAGE_SIZE_WANTED) {
                    mTimelineRv.setLoadMoreEnable(true);
                } else {
                    mTimelineRv.setLoadMoreEnable(false);
                }
                response.body().remove(0);
                mAdapter.addData(response.body());
            }

            @Override
            public void onFailed(Throwable t) {
                L.d(t.toString());
            }
        });
    }

}
