package crazysheep.io.nina;

import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.fragment.TweetDetailFragment;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * tweet detail
 *
 * Created by crazysheep on 16/5/2.
 */
public class TweetDetailActivity extends BaseSwipeBackActivity {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.data_rv) RecyclerView mDataRv;
    @Bind(R.id.nested_sv) NestedScrollView mNestedSv;
    @Bind(R.id.tweet_content_fl) FrameLayout mDetailFl;
    @Bind(R.id.author_avatar_iv) CircleImageView mAvatarIv;
    @Bind(R.id.author_name_tv) TextView mAuthorTv;
    @Bind(R.id.author_screen_name_tv) TextView mAuthorScreenTv;
    @Bind(R.id.tweet_content_tv) TextView mContentTv;

    private TweetDto mTweetDto;

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

        Bundle bundle = new Bundle();
        bundle.putParcelable(BundleConstants.EXTRA_TWEET, mTweetDto);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tweet_content_fl,
                        ActivityUtils.newFragment(this, TweetDetailFragment.class, bundle),
                        TweetDetailFragment.TAG)
                .commitAllowingStateLoss();

        initReplyList();

        mNestedSv.postDelayed(new Runnable() {
            @Override
            public void run() {
                mNestedSv.scrollTo(0, 0);
            }
        }, 100);
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

    private void initReplyList() {
        // for about RecyclerView nested scroll in NestedScrollView,
        // see{@link http://stackoverflow.com/questions/32448051/nestedscrolling-with-nestedscrollview-recyclerview-horizontal-inside-a-coord}
        mDataRv.setNestedScrollingEnabled(false);
        mDataRv.setLayoutManager(new LinearLayoutManager(this));
        mDataRv.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new SimpleHolder(createHolder());
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((TextView)holder.itemView).setText(String.format("position %s", position));
            }

            @Override
            public int getItemCount() {
                return 30;
            }
        });
    }

    private View createHolder() {
        TextView text = new TextView(this);
        text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int)Utils.dp2px(getResources(), 56)));
        text.setGravity(Gravity.CENTER);
        text.setTextSize(Utils.dp2px(getResources(), 5));

        return text;
    }

    static class SimpleHolder extends RecyclerView.ViewHolder {

        public SimpleHolder(View itemView) {
            super(itemView);
        }

    }

}
