package crazysheep.io.nina;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

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
import crazysheep.io.nina.utils.Utils;

/**
 * user profile activity
 *
 * Created by crazysheep on 16/2/2.
 */
public class ProfileActivity extends BaseSwipeBackActivity {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.action_fab) FloatingActionButton mFab;
    @Bind(R.id.tabs) TabLayout mTabLayout;
    @Bind(R.id.content_vp) ViewPager mContentVp;

    private String mScreenName;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        String title = getIntent().getParcelableExtra(BundleConstants.EXTRA_PROFILE_TITLE);
        mScreenName = getIntent().getStringExtra(BundleConstants.EXTRA_USER_SCREEN_NAME);
        setSupportActionBar(mToolbar);
        if(!Utils.isNull(getSupportActionBar())) {
            getSupportActionBar().setTitle(
                    !TextUtils.isEmpty(title) ? title : getString(R.string.profile_title));
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
}
