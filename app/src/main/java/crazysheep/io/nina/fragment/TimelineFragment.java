package crazysheep.io.nina.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.activeandroid.Model;
import com.activeandroid.query.Select;

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
import crazysheep.io.nina.SearchActivity;
import crazysheep.io.nina.adapter.TimelineAdapter;
import crazysheep.io.nina.bean.ITweet;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.constants.EventBusConstants;
import crazysheep.io.nina.holder.timeline.DraftBaseHolder;
import crazysheep.io.nina.net.HttpCache;
import crazysheep.io.nina.net.RxTweeting;
import crazysheep.io.nina.service.BatmanService;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.SystemUIHelper;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.recyclerviewhelper.SimpleItemTouchHelperCallback;
import crazysheep.io.nina.widget.swiperefresh.SwipeRecyclerView;
import crazysheep.io.nina.widget.swiperefresh.SwipeRefreshBase;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

    private Observable<List<ITweet>> mTimelineObser;

    private BatmanService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((BatmanService.BatmanBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        View contentView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, contentView);

        initUI();
        setPostTweetFailedFirstTime();

        return contentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        EventBus.getDefault().register(this);
        getActivity().bindService(new Intent(getActivity(), BatmanService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        getActivity().unbindService(mConnection);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_timeline, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search: {
                ActivityUtils.start(getActivity(), SearchActivity.class);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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
                requestFirstPage();
            }
        });
        mTimelineRv.setOnLoadMoreListener(new SwipeRefreshBase.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                requestNextPage();
            }
        });

        // let recyclerview support swipe dismiss
        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(mAdapter);
        callback.setSwipeEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mTimelineRv.getRefreshableView());

        // hack translucent NavigationBar
        if(SystemUIHelper.hasNavBar(getResources())
                && SystemUIHelper.isNavBarTranslucent(getResources())) {
            RecyclerView rv = mTimelineRv.getRefreshableView();
            rv.setClipToPadding(false);
            rv.setPadding(rv.getPaddingLeft(), rv.getPaddingTop(), rv.getPaddingRight(),
                    rv.getPaddingBottom() + SystemUIHelper.getNavBarSize(getResources())
                            + Math.round(Utils.dp2px(getResources(), 4)));
        }
    }

    // when we back to app, make drafts post state is 'post_failed'
    private void setPostTweetFailedFirstTime() {
        showLoading();
        Observable.just(true)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        List<PostTweetBean> drafts = new Select()
                                .all()
                                .from(PostTweetBean.class)
                                .execute();
                        if (Utils.size(drafts) > 0)
                            for (PostTweetBean postTweet : drafts) {
                                postTweet.setFailed();
                                postTweet.save();
                            }
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        requestFirstPage();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void requestFirstPage() {
        if(!Utils.isNull(mTimelineObser))
            mTimelineObser.unsubscribeOn(AndroidSchedulers.mainThread());

        mTimelineObser = mRxTwitter
                .getHomeTimeline(HttpCache.CACHE_IF_HIT, null, PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<TweetDto>, List<ITweet>>() {
                    @Override
                    public List<ITweet> call(List<TweetDto> tweetDtos) {
                        return new ArrayList<ITweet>(tweetDtos);
                    }
                });
        final List<ITweet> draftsAndTweets = new ArrayList<>();
        Observable.merge(queryDrafts(), mTimelineObser)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ITweet>>() {
                    @Override
                    public void onCompleted() {
                        hideLoading();
                        mTimelineRv.setRefreshing(false);

                        mAdapter.setData(draftsAndTweets);
                        mTimelineRv.setEnableLoadMore(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoading();
                        showError();
                        L.d(e.toString());
                    }

                    @Override
                    public void onNext(List<ITweet> iTweets) {
                        draftsAndTweets.addAll(iTweets);
                    }
                });
    }

    // load more
    @SuppressWarnings("unchecked")
    private void requestNextPage() {
        if(!Utils.isNull(mTimelineObser))
            mTimelineObser.unsubscribeOn(AndroidSchedulers.mainThread());

        TweetDto oldestTweetDto = (TweetDto)mAdapter.getItem(mAdapter.getItemCount() - 1);
        mTimelineObser = mRxTwitter
                .getHomeTimeline(HttpCache.CACHE_NETWORK, oldestTweetDto.id, NEXT_PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<TweetDto>, List<ITweet>>() {
                    @Override
                    public List<ITweet> call(List<TweetDto> tweetDtos) {
                        return new ArrayList<ITweet>(tweetDtos);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
        mTimelineObser.subscribe(new Action1<List<ITweet>>() {
            @Override
            public void call(List<ITweet> iTweets) {
                if (iTweets.size() > 0) {
                    iTweets.remove(0); // remove repeat one
                    mAdapter.addData(iTweets);
                }
            }
        });
    }

    // query drafts from database
    private Observable<List<ITweet>> queryDrafts() {
        return Observable
                .create(new Observable.OnSubscribe<List<ITweet>>() {
                    @Override
                    public void call(Subscriber<? super List<ITweet>> subscriber) {
                        try {
                            List<PostTweetBean> drafts = new Select().from(PostTweetBean.class)
                                    .orderBy(PostTweetBean.COLUMN_CREATED_AT + " DESC")
                                    .execute();
                            subscriber.onNext(new ArrayList<ITweet>(drafts));
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }

                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    protected void onErrorClick() {
        showLoading();
        requestFirstPage();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_POST_TWEET: {
                    // update timeline ui, show draft item
                    PostTweetBean postTweetBean = data.getParcelableExtra(
                            BundleConstants.EXTRA_POST_TWEET);
                    if(!Utils.isNull(postTweetBean)) {
                        // re-query from database, synchronized post state
                        postTweetBean = Model.load(PostTweetBean.class, postTweetBean.getId());

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

    @SuppressWarnings("unused, unchecked")
    @Subscribe
    public void onEvent(@NonNull DraftBaseHolder.EventRemoveDraft event) {
        for(PostTweetBean postTweetBean : (List<PostTweetBean>)mAdapter.getDraftItems())
            if(event.getPostTweetBean().randomId.equals(postTweetBean.randomId)) {
                mAdapter.removeItem(postTweetBean);
            }
    }

    @SuppressWarnings("unused, unchecked")
    @Subscribe
    public void onEvent(@NonNull DraftBaseHolder.EventReSendDraft event) {
        for(PostTweetBean postTweetBean : (List<PostTweetBean>)mAdapter.getDraftItems())
            if(event.getPostTweetBean().randomId.equals(postTweetBean.randomId)) {
                postTweetBean.setPosting();
                mAdapter.notifyItemChanged(mAdapter.findItemPosition(postTweetBean));
                mService.postTweet(postTweetBean);
            }
    }

}
