package crazysheep.io.nina.reactnative;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import crazysheep.io.nina.BuildConfig;
import crazysheep.io.nina.utils.Utils;

/**
 * container for React Native, contain singleton instance for global used
 *
 * Created by crazysheep on 16/7/4.
 */
@Singleton
public class ReactNativeContainer {

    private static ReactNativeContainer sInstance;

    public static ReactNativeContainer getInstance() {
        return sInstance;
    }

    private Context mContext;
    private ReactInstanceManager mReactInstanceMgr;
    private ReactRootView mReactRootView;
    private LifecycleState mLifecycleState = LifecycleState.BEFORE_RESUME;

    /*package used, for dagger inject*/
    @Inject
    protected ReactNativeContainer(@NonNull Context context) {
        mContext = context.getApplicationContext(); // avoid memory leak, use application context
        sInstance = this;

        ReactInstanceManager.Builder builder = ReactInstanceManager.builder()
                .setApplication((Application) context.getApplicationContext())
                .setUseDeveloperSupport(getUseDeveloperSupport())
                .setJSMainModuleName(getJSMainModuleName())
                .setInitialLifecycleState(mLifecycleState);
        for(ReactPackage reactPackage : getReactPackages()) {
            builder.addPackage(reactPackage);
        }
        String jsBundleFile = getJSBundleFile();
        if(!TextUtils.isEmpty(jsBundleFile)) {
            builder.setJSBundleFile(jsBundleFile);
        } else {
            builder.setBundleAssetName(getJSBundleAssetName());
        }

        mReactInstanceMgr = builder.build();
    }

    private String getJSMainModuleName() {
        return "index.android";
    }

    private @Nullable String getJSBundleFile() {
        return null;
    }

    private @Nullable String getJSBundleAssetName() {
        return "index.android.bundle";
    }

    /*
     * if want add custom ReactPackage, edit here
     * */
    private List<ReactPackage> getReactPackages() {
        return Arrays.asList(new MainReactPackage());
    }

    private boolean getUseDeveloperSupport() {
        return BuildConfig.DEBUG;
    }

    private String getMainComponentName() {
        return "Nina";
    }

    private ReactRootView createReactRootView() {
        return new ReactRootView(mContext);
    }

    private void releaseReactRootView(ReactRootView reactRootView) {
        reactRootView.unmountReactApplication();
        reactRootView = null;
    }

    //////////////////////// activity ////////////////////////////

    /**
     * call at Activity.onCreate()
     * */
    public void onCreate(@NonNull Activity activity, Bundle launchOptions) {
        // TODO handle permission to show redbox in dev builds.

        if(!Utils.isNull(mReactRootView)) {
            releaseReactRootView(mReactRootView);
        }
        mReactRootView = createReactRootView();
        mReactRootView.startReactApplication(mReactInstanceMgr, getMainComponentName(),
                launchOptions);
        activity.setContentView(mReactRootView);
    }

    public void onPause() {
        mLifecycleState = LifecycleState.BEFORE_RESUME;

        if(!Utils.isNull(mReactInstanceMgr)) {
            mReactInstanceMgr.onHostPause();
        }
    }

    public void onResume(@NonNull Activity activity, @NonNull DefaultHardwareBackBtnHandler handler) {
        mLifecycleState = LifecycleState.RESUMED;

        if(!Utils.isNull(mReactInstanceMgr)) {
            mReactInstanceMgr.onHostResume(activity, handler);
        }
    }

    public void onDestory() {
        mLifecycleState = LifecycleState.BEFORE_CREATE;

        if(!Utils.isNull(mReactInstanceMgr)) {
            mReactInstanceMgr.onHostDestroy();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!Utils.isNull(mReactInstanceMgr)) {
            mReactInstanceMgr.onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean onBackPressed() {
        if(!Utils.isNull(mReactInstanceMgr)) {
            mReactInstanceMgr.onBackPressed();
            return true;
        }

        return false;
    }

    public boolean onNewIntent(Intent intent) {
        if(!Utils.isNull(mReactInstanceMgr)) {
            mReactInstanceMgr.onNewIntent(intent);

            return true;
        }

        return false;
    }

    private boolean doRefresh = false;
    public boolean onKeyUp(@NonNull Activity activity, int keyCode) {
        if(!Utils.isNull(mReactInstanceMgr)
                && mReactInstanceMgr.getDevSupportManager().getDevSupportEnabled()) {
            if(keyCode == KeyEvent.KEYCODE_MENU) {
                mReactInstanceMgr.showDevOptionsDialog();
                return true;
            }
            if(keyCode == KeyEvent.KEYCODE_R && !(activity.getCurrentFocus() instanceof EditText)) {
                // enable double-tap-R-to-reload
                if(doRefresh) {
                    mReactInstanceMgr.getDevSupportManager().handleReloadJS();
                    doRefresh = false;
                } else {
                    doRefresh = true;
                    new Handler().postDelayed(() -> doRefresh = false, 200);
                }
                return true;
            }
        }

        return false;
    }

    ////////////////////////// fragment ////////////////////////////////

    public @Nullable View onCreateView(@Nullable Bundle launchOptions) {
        if(!Utils.isNull(mReactInstanceMgr)) {
            if(!Utils.isNull(mReactRootView)) {
                releaseReactRootView(mReactRootView);
            }
            mReactRootView = createReactRootView();
            mReactRootView.startReactApplication(mReactInstanceMgr, getMainComponentName(),
                    launchOptions);
            return mReactRootView;
        }
        return null;
    }
}
