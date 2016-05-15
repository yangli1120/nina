package crazysheep.io.nina;

import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.adapter.TweetDetailAdapter;
import crazysheep.io.nina.bean.SearchResultDto;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.fragment.TweetDetailFragment;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.RxUtils;
import crazysheep.io.nina.utils.TweetRenderHelper;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.TwitterLikeImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * tweet detail
 *
 * Created by crazysheep on 16/5/2.
 */
public class TweetDetailActivity extends BaseSwipeBackActivity
        implements BaseActivity.ITwitterServiceActivity {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.data_rv) RecyclerView mDataRv;
    @Bind(R.id.nested_sv) NestedScrollView mNestedSv;
    @Bind(R.id.tweet_content_fl) FrameLayout mDetailFl;
    @Bind(R.id.author_avatar_iv) CircleImageView mAvatarIv;
    @Bind(R.id.author_name_tv) TextView mAuthorTv;
    @Bind(R.id.author_screen_name_tv) TextView mAuthorScreenTv;
    @Bind(R.id.tweet_content_tv) TextView mContentTv;
    @Bind(R.id.action_reply_ll) View mReplyLl;
    @Bind(R.id.action_retweet_ll) View mRetweetLl;
    @Bind(R.id.action_retweet_iv) ImageView mRetweetIv;
    @Bind(R.id.action_retweet_count_tv) TextView mRetweetCountTv;
    @Bind(R.id.action_like_ll) View mLikeLl;
    @Bind(R.id.action_like_count_tv) TextView mLikeCountTv;
    @Bind(R.id.action_like_iv) TwitterLikeImageView mLikeIv;

    private TweetDetailAdapter mAdapter;
    private TweetDto mTweetDto;

    private Subscription mSearchSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);
        ButterKnife.bind(this);

        mTweetDto = getIntent().getParcelableExtra(BundleConstants.EXTRA_TWEET);
        if(Utils.isNull(mTweetDto))
            finish();

        setSupportActionBar(mToolbar);
        if(!Utils.isNull(getSupportActionBar())) {
            getSupportActionBar().setTitle(getString(R.string.tweet));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        }

        Glide.with(this)
                .load(mTweetDto.user.biggerProfileImageUrlHttps())
                .into(mAvatarIv);
        mAuthorTv.setText(mTweetDto.user.name);
        mAuthorScreenTv.setText(mTweetDto.user.screen_name);
        mContentTv.setText(mTweetDto.text);

        // render bottom bar
        TweetRenderHelper.renderBottomBar(this, mHttpClient.get(),
                new UserPrefs(this).getUserScreenName().equals(mTweetDto.user.screen_name),
                mTweetDto,
                mReplyLl, mRetweetLl, mRetweetIv, mRetweetCountTv, mLikeLl, mLikeCountTv, mLikeIv);

        Bundle bundle = new Bundle();
        bundle.putParcelable(BundleConstants.EXTRA_TWEET, mTweetDto);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tweet_content_fl,
                        ActivityUtils.newFragment(this, TweetDetailFragment.class, bundle),
                        TweetDetailFragment.TAG)
                .commitAllowingStateLoss();

        // for about RecyclerView nested scroll in NestedScrollView,
        // see{@link http://stackoverflow.com/questions/32448051/nestedscrolling-with-nestedscrollview-recyclerview-horizontal-inside-a-coord}
        mDataRv.setNestedScrollingEnabled(false);
        mDataRv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TweetDetailAdapter(this, null);
        mDataRv.setAdapter(mAdapter);

        mNestedSv.postDelayed(new Runnable() {
            @Override
            public void run() {
                mNestedSv.scrollTo(0, 0);
            }
        }, 100);

        final List<TweetDto> replies = new ArrayList<>();
        mSearchSub = mRxTwitter.reply(String.format("@%s", mTweetDto.user.screen_name))
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<SearchResultDto, Observable<TweetDto>>() {
                    @Override
                    public Observable<TweetDto> call(SearchResultDto searchResultDto) {
                        return Observable.from(searchResultDto.getStatuses());
                    }
                })
                .filter(new Func1<TweetDto, Boolean>() {
                    @Override
                    public Boolean call(TweetDto tweetDto) {
                        return mTweetDto.idStr.equals(tweetDto.in_reply_to_status_id_str);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TweetDto>() {
                    @Override
                    public void onCompleted() {
                        mAdapter.setData(replies);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(TweetDto tweetDto) {
                        replies.add(tweetDto);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RxUtils.unsubscribe(mSearchSub);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }break;
        }
        return true;
    }

}
