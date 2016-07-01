package crazysheep.io.nina;

import android.os.Bundle;
import android.view.KeyEvent;

import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;

import crazysheep.io.nina.utils.Utils;

/**
 * base react native activity
 *
 * Created by crazysheep on 16/3/16.
 */
public class ReactNativeActivity extends BaseActivity implements DefaultHardwareBackBtnHandler {

    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReactRootView = new ReactRootView(this);
        mReactInstanceMgr = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();

        mReactRootView.startReactApplication(mReactInstanceMgr,
                getString(R.string.app_name), null);
        setContentView(mReactRootView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!Utils.isNull(mReactInstanceMgr))
            mReactInstanceMgr.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!Utils.isNull(mReactInstanceMgr))
            mReactInstanceMgr.onResume(this, this);
    }

    @Override
    public void onBackPressed() {
        if(!Utils.isNull(mReactInstanceMgr))
            mReactInstanceMgr.onBackPressed();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU && !Utils.isNull(mReactInstanceMgr)) {
            mReactInstanceMgr.showDevOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }
}
