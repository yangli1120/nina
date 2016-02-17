package crazysheep.io.nina.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.adapter.FragmentPagerBaseAdapter;
import crazysheep.io.nina.adapter.ProfileMediaTimelineAdapter;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.swiperefresh.LoadMoreRecyclerView;
import retrofit2.Call;
import retrofit2.Response;

/**
 * profiel media fragment
 *
 * Created by crazysheep on 16/2/2.
 */
public class ProfileMediaFragment extends BaseFragment
        implements FragmentPagerBaseAdapter.IPagerFragment, BaseFragment.INetworkFragment,
                   LoadMoreRecyclerView.OnLoadMoreListener {

    private static final int PAGE_SIZE = 101;
    private static final int PAGE_SIZE_WANTED = 100;

    @Bind(R.id.data_rv) LoadMoreRecyclerView mMediaRv;
    private ProfileMediaTimelineAdapter mMediaAdapter;

    private String mScreenName;

    private Call<List<TweetDto>> mTimelineCall;

    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.profile_tab_media);
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

        mMediaRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMediaRv.setOnLoadMoreListener(this);
        mMediaAdapter = new ProfileMediaTimelineAdapter(getActivity(), null);
        mMediaRv.setAdapter(mMediaAdapter);

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

    private void requestFirstPage() {
        if(!Utils.isNull(mTimelineCall))
            mTimelineCall.cancel();
        // TODO use NinaTwitterApiClient
        /*mTimelineCall = mTwitter.getUserTimeline(HttpCache.CacheConfig.CACHE_NETWORK, mScreenName,
                PAGE_SIZE, null);
        mTimelineCall.enqueue(new Retrofit2NiceCallback<List<TweetDto>>() {
            @Override
            public void onRespond(Call<List<TweetDto>> call, Response<List<TweetDto>> response) {
                if(response.body().size() > PAGE_SIZE_WANTED) {
                    mMediaRv.setLoadMoreEnable(true);
                    response.body().remove(response.body().size() - 1);
                } else {
                    mMediaRv.setLoadMoreEnable(false);
                }
                mMediaAdapter.setData(filterMediaTweets(response.body()));
            }

            @Override
            public void onFailed(Throwable t) {
                L.d(t.toString());
            }
        });*/
    }

    private void requestNextPage() {
        if(!Utils.isNull(mTimelineCall))
            mTimelineCall.cancel();
        // TODO use NinaTwitterApiClient
        /*long maxId = (mMediaAdapter.getItem(mMediaAdapter.getItemCount() - 1)).id;
        mTimelineCall = mTwitter.getUserTimeline(HttpCache.CacheConfig.CACHE_NETWORK, mScreenName,
                PAGE_SIZE, maxId);
        mTimelineCall.enqueue(new Retrofit2NiceCallback<List<TweetDto>>() {
            @Override
            public void onRespond(Call<List<TweetDto>> call, Response<List<TweetDto>> response) {
                if(response.body().size() > PAGE_SIZE_WANTED) {
                    mMediaRv.setLoadMoreEnable(true);
                } else {
                    mMediaRv.setLoadMoreEnable(false);
                }
                response.body().remove(0);
                mMediaAdapter.addData(filterMediaTweets(response.body()));
            }

            @Override
            public void onFailed(Throwable t) {
                L.d(t.toString());
            }
        });*/
    }

    private List<TweetDto> filterMediaTweets(List<TweetDto> tweets) {
        if(!Utils.isNull(tweets)) {
            ArrayList<TweetDto> filterTweets = new ArrayList<>();
            for(TweetDto tweetDto : tweets)
                if(tweetDto.isPhoto())
                    filterTweets.add(tweetDto);
            return filterTweets;
        }

        return null;
    }

}
