package crazysheep.io.nina;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jakewharton.scalpel.ScalpelFrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.bean.UserDto;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.fragment.TimelineFragment;
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.service.BatmanService;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.DialogUtils;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.utils.RxWorker;
import crazysheep.io.nina.utils.ToastUtils;
import crazysheep.io.nina.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends BaseActivity
        implements BaseActivity.ITwitterServiceActivity, View.OnClickListener,
                   NavigationView.OnNavigationItemSelectedListener {

    private final int REQUEST_CODE_OVERLAY_PERMISSION = 1234;

    @Bind(R.id.drawer) DrawerLayout mDrawer;
    @Bind(R.id.nav_layout) NavigationView mNav;
    @Bind(R.id.scalpel_fl) ScalpelFrameLayout mScalpelFl;
    private SwitchCompat mThemeSwitchBtn;
    private SwitchCompat mScalpelSwitchBtn;
    private CircleImageView mAvatarCiv;
    private TextView mUserNameTv;
    private TextView mUserScreenNameTv;
    private ImageView mLogoutIv;

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

    private Call<UserDto> mUserCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initUI();

        startService(new Intent(this, BatmanService.class));
        bindService(new Intent(this, BatmanService.class), mConnection, Context.BIND_AUTO_CREATE);
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

    @Override
    @TargetApi(APICompat.M)
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_night_theme: {
                if(mSettingPrefs.isNightTheme())
                    mSettingPrefs.switchDayTheme();
                else
                    mSettingPrefs.switchNightTheme();

                mThemeSwitchBtn.setChecked(mSettingPrefs.isNightTheme());

                mDrawer.closeDrawer(GravityCompat.START);
                RxWorker.delayOnUI(this, 300, new Runnable() {
                    @Override
                    public void run() {
                        switchTheme();
                        recreate();
                    }
                });
            }break;

            case R.id.nav_react_native: {
                // android M need request ACTION_MANAGE_OVERLAY_PERMISSION for react native
                // see{@link https://github.com/facebook/react-native/issues/3150}
                if(APICompat.api23() && !Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    ActivityUtils.startResult(this, REQUEST_CODE_OVERLAY_PERMISSION, intent);
                } else {
                    ActivityUtils.start(this, ReactNativeActivity.class);
                }
            }break;

            case R.id.nav_scalpel_debug: {
                mScalpelSwitchBtn.setChecked(!mScalpelSwitchBtn.isChecked());
                mScalpelFl.setLayerInteractionEnabled(mScalpelSwitchBtn.isChecked());
                mScalpelFl.setDrawViews(mScalpelSwitchBtn.isChecked());
                mScalpelFl.setDrawIds(mScalpelSwitchBtn.isChecked());
            }break;
        }
        return true;
    }

    @Override
    @TargetApi(APICompat.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_OVERLAY_PERMISSION: {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    ToastUtils.t(this, "permission denied");
                } else {
                    ActivityUtils.start(this, ReactNativeActivity.class);
                }
            }break;
        }
    }

    private void initUI() {
        //init nav view, NavigationView can not use ButterKnife...
        //see{@link https://code.google.com/p/android/issues/detail?id=190226}
        mUserNameTv = ButterKnife.findById(mNav.getHeaderView(0), R.id.user_name_tv);
        mUserScreenNameTv = ButterKnife.findById(mNav.getHeaderView(0), R.id.user_screen_name_tv);
        mLogoutIv = ButterKnife.findById(mNav.getHeaderView(0), R.id.logout_iv);
        mLogoutIv.setOnClickListener(this);
        mAvatarCiv = ButterKnife.findById(mNav.getHeaderView(0), R.id.avatar_iv);
        mAvatarCiv.setOnClickListener(this);
        mThemeSwitchBtn = ButterKnife.findById(
                mNav.getMenu().findItem(R.id.nav_night_theme).getActionView(), R.id.theme_switch);
        mThemeSwitchBtn.setChecked(mSettingPrefs.isNightTheme());
        mScalpelSwitchBtn = ButterKnife.findById(
                mNav.getMenu().findItem(R.id.nav_scalpel_debug).getActionView(), R.id.theme_switch);
        mScalpelSwitchBtn.setChecked(false);

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

        mNav.setNavigationItemSelectedListener(this);
    }

    public void setToolbar(@NonNull Toolbar toolbar) {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle abToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(abToggle);
        mDrawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                // when user open drawer, request fresh user info
                if(!Utils.isNull(mUserCall))
                    mUserCall.cancel();

                mUserCall = mTwitter.getUserInfo(mUserPrefs.getUserScreenName());
                mUserCall.enqueue(new NiceCallback<UserDto>(getActivity()) {
                    @Override
                    public void onRespond(Response<UserDto> response) {
                        if (!Utils.isNull(response.body())) {
                            if (!response.body().name.equals(mUserPrefs.getUsername())) {
                                mUserPrefs.setUsername(response.body().name);
                                mUserNameTv.setText(response.body().name);
                            }
                            String profileImageUrl = response.body().originalProfileImageUrlHttps();
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
        });
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

            case R.id.logout_iv: {
                DialogUtils.showConfirmDialog(this, getString(R.string.dialog_logout_title), null,
                        new DialogUtils.ButtonAction() {
                            @Override
                            public String getTitle() {
                                return getString(R.string.ok_btn);
                            }

                            @Override
                            public void onClick(DialogInterface dialog) {
                                // logout
                                mUserPrefs.logout();

                                ActivityUtils.start(getActivity(), SplashActivity.class);
                                finish();
                            }
                        },
                        new DialogUtils.SimpleButtonAction() {
                            @Override
                            public String getTitle() {
                                return getString(R.string.cancel_btn);
                            }
                        });
            }break;
        }
    }

}
