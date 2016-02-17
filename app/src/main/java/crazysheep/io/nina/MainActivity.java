package crazysheep.io.nina;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.bean.UserDto;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.fragment.TimelineFragment;
import crazysheep.io.nina.net_new.NiceCallback;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.RxWorker;
import crazysheep.io.nina.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity
        implements BaseActivity.ITwitterServiceActivity, View.OnClickListener {

    @Bind(R.id.drawer) DrawerLayout mDrawer;
    @Bind(R.id.nav_layout) NavigationView mNav;
    private CircleImageView mAvatarCiv;
    private TextView mUserNameTv;
    private TextView mUserScreenNameTv;

    private UserPrefs mUserPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUserPrefs = new UserPrefs(this);

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTwitter.getUserInfo(mUserPrefs.getUserScreenName(), new NiceCallback<UserDto>(TAG) {
            @Override
            public void onRespond(UserDto userDto, retrofit.client.Response response) {
                if (!Utils.isNull(userDto)) {
                    if (!userDto.name.equals(mUserPrefs.getUsername())) {
                        mUserPrefs.setUsername(userDto.name);
                        mUserNameTv.setText(userDto.name);
                    }
                    String profileImageUrl = userDto.profile_image_url_https;
                    if (!TextUtils.isEmpty(profileImageUrl)
                            && !profileImageUrl.equals(mUserPrefs.getUserAvatar())) {
                        mUserPrefs.setUserAvatar(profileImageUrl);
                        Glide.with(getActivity())
                                .load(profileImageUrl)
                                .into(mAvatarCiv);
                    }
                }
            }

            @Override
            public void onFailed(Throwable t) {

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
                mDrawer.closeDrawer(GravityCompat.START);
                RxWorker.delayOnUI(this, 300, new Runnable() {
                    @Override
                    public void run() {
                        ActivityUtils.start(getActivity(),
                                ActivityUtils.prepare(getActivity(), ProfileActivity.class)
                                        .putExtra(BundleConstants.EXTRA_USER_SCREEN_NAME,
                                                mUserPrefs.getUserScreenName())
                                        .putExtra(BundleConstants.EXTRA_USER_NAME,
                                                mUserPrefs.getUsername()));
                    }
                });
            }break;
        }
    }

}
