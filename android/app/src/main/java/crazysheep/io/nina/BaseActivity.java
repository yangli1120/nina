package crazysheep.io.nina;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.android.debug.hv.ViewServer;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.dagger2.component.DaggerActivityComponent;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.RxTwitterService;
import crazysheep.io.nina.net.TwitterService;
import crazysheep.io.nina.prefs.SettingPrefs;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.DrawableUtils;
import crazysheep.io.nina.utils.Utils;
import dagger.Lazy;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * base activity
 *
 * Created by crazysheep on 16/1/20.
 */
public class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    //////////////////////////// api ////////////////////////////////

    /**
     * if current activity need request network service, implement this interface,
     * then BaseActivity will init TwitterService instance
     * */
    public interface ITwitterServiceActivity {}

    /**
     * give change to fragment handle onBackPressed()
     * */
    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    /////////////////////////////////////////////////////////////////

    public static String TAG = BaseActivity.class.getSimpleName();

    private FrameLayout mRootFl;

    @Inject protected UserPrefs mUserPrefs;
    @Inject protected SettingPrefs mSettingPrefs;
    @Inject protected Lazy<HttpClient> mHttpClient;
    protected TwitterService mTwitter;
    protected RxTwitterService mRxTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerActivityComponent.builder()
                .applicationComponent(BaseApplication.from(this).getComponent())
                .build()
                .inject(this);

        // init theme
        mRootFl = ButterKnife.findById(this, android.R.id.content);
        if(Utils.isNull(savedInstanceState))
            switchTheme();

        TAG = this.getClass().getSimpleName();

        if(this instanceof ITwitterServiceActivity) {
            mTwitter = mHttpClient.get().getTwitterService();
            mRxTwitter = mHttpClient.get().getRxTwitterService();
        }

        ViewServer.get(this).addWindow(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ViewServer.get(this).removeWindow(this);
    }

    protected final Activity getActivity() {
        return this;
    }

    protected final void switchTheme() {
        getDelegate().setLocalNightMode(mSettingPrefs.isNightTheme() ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // tint root layout background refer to current theme
        if(mSettingPrefs.isNightTheme()) {
            // if is night theme, tint root background with colorPrimary,
            // UI will have overdraw 1x
            int bgColor;
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            bgColor = typedValue.data;

            mRootFl.setBackground(DrawableUtils.tint(
                    ContextCompat.getDrawable(this, R.drawable.tint_bg), bgColor));
        } else {
            // if is day theme, set null drawable background, so that UI have no overdraw,
            // the best performance
            mRootFl.setBackground(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
    }

}
