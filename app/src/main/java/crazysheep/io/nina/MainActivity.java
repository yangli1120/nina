package crazysheep.io.nina;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.fragment.TimelineFragment;
import crazysheep.io.nina.net.HttpConstants;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.drawer) DrawerLayout mDrawer;
    @Bind(R.id.nav_layout) NavigationView mNav;
    private CircleImageView mAvatarCiv;
    private TextView mUserNameTv;
    private TextView mUserScreenNameTv;

    private UserPrefs mUserPrefs;
    private Twitter mTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUserPrefs = new UserPrefs(this);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(false)
                .setOAuthConsumerKey(HttpConstants.NINA_CONSUMER_KEY)
                .setOAuthConsumerSecret(HttpConstants.NINA_CONSUMER_SECRET)
                .setOAuthAccessToken(mUserPrefs.getAuthToken())
                .setOAuthAccessTokenSecret(mUserPrefs.getSecret());
        TwitterFactory tf = new TwitterFactory(cb.build());
        mTwitter = tf.getInstance();

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // every back to this activity, refresh user information
        Observable.just(mTwitter)
                .map(new Func1<Twitter, User>() {
                    @Override
                    public User call(Twitter twitter) {
                        try {
                            return mTwitter.showUser(mUserPrefs.getId());
                        } catch (TwitterException te) {
                            L.d("fetch user exception: " + te);
                        }

                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (!Utils.isNull(user)) {
                            if(!user.getName().equals(mUserPrefs.getUsername())) {
                                mUserPrefs.setUsername(user.getName());
                                mUserNameTv.setText(user.getName());
                            }
                            String profileImageUrl = user.getOriginalProfileImageURLHttps();
                            if(!TextUtils.isEmpty(profileImageUrl)
                                    && !profileImageUrl.equals(mUserPrefs.getUserAvatar())) {
                                mUserPrefs.setUserAvatar(profileImageUrl);
                                Glide.with(getActivity())
                                        .load(profileImageUrl)
                                        .into(mAvatarCiv);
                            }
                        }
                    }
                });
    }

    private void initUI() {
        //init nav view, NavigationView can not use ButterKnife...
        //see{@link https://code.google.com/p/android/issues/detail?id=190226}
        mUserNameTv = ButterKnife.findById(mNav.getHeaderView(0), R.id.user_name_tv);
        mUserScreenNameTv = ButterKnife.findById(mNav.getHeaderView(0), R.id.user_screen_name_tv);
        mAvatarCiv = ButterKnife.findById(mNav.getHeaderView(0), R.id.avatar_iv);
        mAvatarCiv.setOnClickListener(this);

        mUserNameTv.setText(mUserPrefs.getUsername());
        mUserScreenNameTv.setText(getString(R.string.screen_name, mUserPrefs.getUserScreenName()));
        if(!TextUtils.isEmpty(mUserPrefs.getUserAvatar()))
            Glide.with(this)
                    .load(mUserPrefs.getUserAvatar())
                    .into(mAvatarCiv);
        //init content
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_ft, new TimelineFragment(), TimelineFragment.TAG)
                .commitAllowingStateLoss();
    }

    public void setToolbar(@NonNull Toolbar toolbar) {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle abToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(abToggle);
        abToggle.syncState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_iv: {
                // TODO jump to my profile
                DebugHelper.toast(this, "click avatar");
            }break;
        }
    }
}
