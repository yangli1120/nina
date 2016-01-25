package crazysheep.io.nina.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.L;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * fragment show twitter timeline
 *
 * Created by crazysheep on 16/1/22.
 */
public class TimelineFragment extends BaseFragment {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.data_rv) RecyclerView mTimelineRv;

    private TimelineAdapter mAdapter;

    private Call<List<TweetDto>> mTweetsCall;

    private UserPrefs mUserPrefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, contentView);

        initUI();
        mTweetsCall.enqueue(new retrofit.Callback<List<TweetDto>>() {
            @Override
            public void onResponse(Response<List<TweetDto>> response, Retrofit retrofit) {
                L.d(response.toString());

                mAdapter.setData(response.body());
            }

            @Override
            public void onFailure(Throwable t) {
                L.d(t.toString());
            }
        });

        return contentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserPrefs = new UserPrefs(getActivity());
        mTweetsCall = mRetrofit.create(ApiService.class).getUserTimeline(
                null, mUserPrefs.getUsername(), null, 20, null);
    }

    private void initUI() {
        if(getActivity() instanceof MainActivity)
            ((MainActivity)getActivity()).setToolbar(mToolbar);

        mAdapter = new TimelineAdapter(getActivity(), null);
        mTimelineRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTimelineRv.setItemAnimator(new DefaultItemAnimator());
        mTimelineRv.setAdapter(mAdapter);
    }

}
