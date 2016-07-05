package crazysheep.io.nina.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.dagger2.component.DaggerReactNativeComponent;
import crazysheep.io.nina.reactnative.ReactNativeContainer;

/**
 * react native fragment
 *
 * Created by crazysheep on 16/7/5.
 */
public class ReactNativeFragment extends Fragment implements DefaultHardwareBackBtnHandler {

    private ReactNativeContainer mRNContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRNContainer = DaggerReactNativeComponent.builder()
                .applicationComponent(BaseApplication.from(getContext()).getComponent())
                .build()
                .getReactNativeContainer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return mRNContainer.onCreateView(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRNContainer.onResume(getActivity(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRNContainer.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRNContainer.onDestory();
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        mRNContainer.onBackPressed();
    }

}
