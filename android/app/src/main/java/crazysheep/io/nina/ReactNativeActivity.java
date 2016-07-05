package crazysheep.io.nina;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.dagger2.component.DaggerReactNativeComponent;
import crazysheep.io.nina.reactnative.ReactNativeContainer;

/**
 * base react native activity
 *
 * Created by crazysheep on 16/3/16.
 */
public class ReactNativeActivity extends Activity implements DefaultHardwareBackBtnHandler {

    protected ReactNativeContainer mRNContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRNContainer = DaggerReactNativeComponent.builder()
                .applicationComponent(BaseApplication.from(this).getComponent())
                .build()
                .getReactNativeContainer();

        mRNContainer.onCreate(this, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(!mRNContainer.onNewIntent(intent)) {
            super.onNewIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRNContainer.onResume(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mRNContainer.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRNContainer.onDestory();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mRNContainer.onKeyUp(this, keyCode) || super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mRNContainer.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }
}
