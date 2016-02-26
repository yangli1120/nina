package crazysheep.io.nina;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.service.BatmanService;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.RxWorker;
import crazysheep.io.nina.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends BaseActivity
        implements BaseActivity.ITwitterServiceActivity, View.OnClickListener {

    @Bind(R.id.drawer) DrawerLayout mDrawer;
    @Bind(R.id.nav_layout) NavigationView mNav;
    private CircleImageView mAvatarCiv;
    private TextView mUserNameTv;
    private TextView mUserScreenNameTv;

    private BatmanService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((BatmanService.BatmanBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private UserPrefs mUserPrefs;

    private Call<UserDto> mUserCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUserPrefs = new UserPrefs(this);

        initUI();

        startService(new Intent(this, BatmanService.class));
        bindService(new Intent(this, BatmanService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!Utils.isNull(mUserCall))
            mUserCall.cancel();

        mUserCall = mTwitter.getUserInfo(mUserPrefs.getUserScreenName());
        mUserCall.enqueue(new NiceCallback<UserDto>() {
            @Override
            public void onRespond(Response<UserDto> response) {
                if (!Utils.isNull(response.body())) {
                    if (!response.body().name.equals(mUserPrefs.getUsername())) {
                        mUserPrefs.setUsername(response.body().name);
                        mUserNameTv.setText(response.body().name);
                    }
                    String profileImageUrl = response.body().profile_image_url_https;
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
                L.d(t.toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // if BatmanService is still posting tweet, do not stop it, it will stop itself
        // after post done
        if(!mService.isPosting())
            stopService(new Intent(this, BatmanService.class));
        unbindService(mConnection);
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
