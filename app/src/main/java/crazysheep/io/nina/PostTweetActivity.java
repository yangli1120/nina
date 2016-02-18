package crazysheep.io.nina;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.net.RxTweeting;
import crazysheep.io.nina.utils.ImeUtils;
import crazysheep.io.nina.utils.ToastUtils;
import crazysheep.io.nina.utils.Utils;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import rx.Subscription;

/**
 * create a tweet
 *
 * Created by crazysheep on 16/2/17.
 */
public class PostTweetActivity extends BaseSwipeBackActivity implements TextWatcher {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.edit_tweet_et) EditText mTweetEt;
    @Bind(R.id.send_tweet_btn) Button mSendBtn;

    // if post a reply tweet
    private long replayStatusId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_tweet);
        ButterKnife.bind(this);

        replayStatusId = getIntent().getLongExtra(BundleConstants.EXTRA_REPLY_STATUS_ID, -1);

        setSupportActionBar(mToolbar);
        if(!Utils.isNull(getSupportActionBar())) {
            getSupportActionBar().setTitle(null);

            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_grey);
        }

        mSendBtn.setEnabled(false);
        mTweetEt.addTextChangedListener(this);

        getSwipeBackLayout().addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                if(state != SwipeBackLayout.STATE_IDLE)
                    ImeUtils.hide(getActivity());
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {}

            @Override
            public void onScrollOverThreshold() {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mSendBtn.setEnabled(!TextUtils.isEmpty(s.toString()));
    }

    @OnClick(R.id.send_tweet_btn)
    protected void postTweet() {
        // TODO post a tweet
        PostTweetBean postTweet = new PostTweetBean.Builder()
                .setStatus(mTweetEt.getEditableText().toString())
                .build();
        Subscription subscription = RxTweeting.postTweet(postTweet);
        if(subscription instanceof RxTweeting.ErrorSubscription)
            ToastUtils.t(this, ((RxTweeting.ErrorSubscription) subscription).getError());
        else
            finish();
    }

}
