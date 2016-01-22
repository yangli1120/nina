package crazysheep.io.nina;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.TextView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.prefs.UserPrefs;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.L;
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
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        mUserPrefs = new UserPrefs(this);
        initUI();

        if(mUserPrefs.isLogin())
            goMain();
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

                mUserPrefs.setUsername(result.data.getUserName());
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

}
