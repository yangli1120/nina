package crazysheep.io.nina;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.utils.ActivityUtils;
import rx.Observable;
import rx.functions.Action1;

/**
 * splash activity
 *
 * Created by crazysheep on 16/1/20.
 */
public class SplashActivity extends BaseActivity {

    public static final String[] PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Bind(R.id.logo_tv) TextView mLogoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        mLogoTv.setAlpha(0.5f);
        mLogoTv.animate()
                .alpha(1f)
                .setDuration(1000)
                .start();
        goMain();
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
