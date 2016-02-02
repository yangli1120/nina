package crazysheep.io.nina;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.constants.PermissionConstants;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.L;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Observable;
import rx.functions.Action1;

/**
 * splash activity
 *
 * Created by crazysheep on 16/1/20.
 */
public class SplashActivity extends BaseActivity {

    @Bind(R.id.logo_tv) TextView mLogoTv;
    @Bind(R.id.login_btn) TwitterLoginButton mLoginBtn;

    private UserPrefs mUserPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        mUserPrefs = new UserPrefs(this);
        initUI();

        requestStorage();
    }

    private void initUI() {
        // make animation smoothly
        mLogoTv.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mLogoTv.getViewTreeObserver().removeOnPreDrawListener(this);

                mLogoTv.setAlpha(0.3f);
                mLogoTv.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .start();

                return true;
            }
        });

        mLoginBtn.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                L.d("username: " + result.data.getUserName()
                        + ", user_id: " + result.data.getUserId()
                        + ", token: " + result.data.getAuthToken().token
                        + ", secret: " + result.data.getAuthToken().secret);

                mUserPrefs.setUserScreenName(result.data.getUserName());
                mUserPrefs.setId(result.data.getId());
                mUserPrefs.setAuthToken(result.data.getAuthToken().token);
                mUserPrefs.setSecret(result.data.getAuthToken().secret);

                goMain();
            }

            @Override
            public void failure(TwitterException e) {
                L.d(e.toString());
            }

            @Override
            public void onResponse(Response<TwitterSession> response, Retrofit retrofit) {
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLoginBtn.setVisibility(mUserPrefs.isLogin() ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mLoginBtn.onActivityResult(requestCode, resultCode, data);
    }

    private void goMain() {
        Observable.just(true)
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        ActivityUtils.start(getActivity(), MainActivity.class);
                        finish();
                    }
                });
    }

    @AfterPermissionGranted(PermissionConstants.RC_EXTERNAL_STORAGE)
    private void requestStorage() {
        String[] pers = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if(EasyPermissions.hasPermissions(this, pers)) {
            if(mUserPrefs.isLogin())
                goMain();
        } else {
            EasyPermissions.requestPermissions(this, null, PermissionConstants.RC_EXTERNAL_STORAGE,
                    pers);
        }
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {
        super.onPermissionsGranted(perms);

        if(mUserPrefs.isLogin())
            goMain();
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
        super.onPermissionsDenied(perms);

        finish();
    }
}
