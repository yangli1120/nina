package crazysheep.io.nina;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.adapter.FragmentPagerBaseAdapter;
import crazysheep.io.nina.adapter.ProfilePagerAdapter;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.fragment.ProfileLikeFragment;
import crazysheep.io.nina.fragment.ProfileMediaFragment;
import crazysheep.io.nina.fragment.ProfileTimelineFragment;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.StringUtils;
import crazysheep.io.nina.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import twitter4j.User;

/**
 * user profile activity
 *
 * Created by crazysheep on 16/2/2.
 */
public class ProfileActivity extends BaseSwipeBackActivity
        implements AppBarLayout.OnOffsetChangedListener {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.appbar) AppBarLayout mAppbar;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.action_fab) FloatingActionButton mFab;
    @Bind(R.id.tabs) TabLayout mTabLayout;
    @Bind(R.id.content_vp) ViewPager mContentVp;
    // header
    @Bind(R.id.parallax_header_iv) ImageView mHeaderIv;
    @Bind(R.id.author_avatar_iv) CircleImageView mUserAvatar;
    @Bind(R.id.user_name_tv) TextView mUserNameTv;
    @Bind(R.id.user_screen_name_tv) TextView mUserScreenNameTv;
    @Bind(R.id.user_introduction_tv) TextView mUserIntroductionTv;
    @Bind(R.id.user_location_tv) TextView mUserLocationTv;
    @Bind(R.id.following_tv) TextView mFollowingTv;
    @Bind(R.id.follower_tv) TextView mFollowerTv;

    private String mUserName;
    private String mScreenName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mUserName = getIntent().getStringExtra(BundleConstants.EXTRA_USER_NAME);
        mScreenName = getIntent().getStringExtra(BundleConstants.EXTRA_USER_SCREEN_NAME);
        initUI();
        requestUser(mScreenName);
    }

    @SuppressWarnings("unchecked")
    private void initUI() {
        mAppbar.addOnOffsetChangedListener(this);
        setSupportActionBar(mToolbar);
        if(!Utils.isNull(getSupportActionBar())) {
            getSupportActionBar().setTitle(
                    !TextUtils.isEmpty(mUserName) ? mUserName : getString(R.string.profile_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        }

        List<FragmentPagerBaseAdapter.IPagerFragment> fragments = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putString(BundleConstants.EXTRA_USER_SCREEN_NAME, mScreenName);
        fragments.add(
                ActivityUtils.newFragment(getActivity(), ProfileTimelineFragment.class, bundle));
        fragments.add(ActivityUtils.newFragment(getActivity(), ProfileMediaFragment.class, bundle));
        fragments.add(ActivityUtils.newFragment(getActivity(), ProfileLikeFragment.class, bundle));
        mContentVp.setAdapter(
                new ProfilePagerAdapter(this, getSupportFragmentManager(), fragments));
        mContentVp.setOffscreenPageLimit(2);
        mContentVp.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setupWithViewPager(mContentVp);
    }

    /*
    * for handling appbar collapsing or expanding, see{@link https://github.com/saulmm/CoordinatorBehaviorExample}
    * */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

        mCollapsingToolbar.setTitle(percentage >= 1f ? mUserName : null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestUser(String screenName) {
        // TODO use NinaTwitterApiClient request "users/show"
    }

    private void updateUserUI(User user) {
        if(!Utils.isNull(user)) {
            Glide.clear(mUserAvatar);
            Glide.with(this)
                    .load(user.getOriginalProfileImageURLHttps())
                    .into(mUserAvatar);

            Glide.clear(mHeaderIv);
            Glide.with(this)
                    .load(user.getProfileBackgroundImageUrlHttps())
                    .error(R.color.colorPrimary)
                    .into(mHeaderIv);

            mUserNameTv.setText(user.getName());
            mUserScreenNameTv.setText(getString(R.string.screen_name, user.getScreenName()));
            mUserIntroductionTv.setText(user.getDescription());
            if(!TextUtils.isEmpty(user.getLocation())) {
                mUserLocationTv.setText(user.getLocation());
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mUserLocationTv,
                        R.drawable.ic_location_grey, 0, 0, 0);
            } else {
                mUserLocationTv.setVisibility(View.GONE);
            }
            mFollowerTv.setText(
                    getString(user.getFollowersCount() <= 1
                                    ? R.string.profile_follower : R.string.profile_follower_s,
                            StringUtils.formatCount(user.getFollowersCount())));
            mFollowingTv.setText(
                    getString(R.string.profile_following,
                            StringUtils.formatCount(user.getFriendsCount())));
        }
    }

}
