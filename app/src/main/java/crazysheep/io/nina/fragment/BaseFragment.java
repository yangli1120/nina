package crazysheep.io.nina.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.squareup.leakcanary.RefWatcher;

import javax.inject.Inject;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.dagger2.component.DaggerFragmentComponent;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.RxTwitterService;
import crazysheep.io.nina.net.TwitterService;
import dagger.Lazy;

/**
 * base fragment
 *
 * Created by crazysheep on 16/1/22.
 */
public class BaseFragment extends Fragment {

    ////////////////////////////// interface //////////////////////////////////////

    /**
     * if fragment need request twitter service, implement this fragment
     * */
    public interface INetworkFragment {}

    //////////////////////////////////////////////////////////////////////////////

    public static String TAG = BaseFragment.class.getSimpleName();

    @Inject protected Lazy<HttpClient> mHttpClient;
    @Inject protected RefWatcher mRefWatcher;
    protected TwitterService mTwitter;
    protected RxTwitterService mRxTwitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getClass().getSimpleName();

        DaggerFragmentComponent.builder()
                .applicationComponent(BaseApplication.from(getActivity()).getComponent())
                .build()
                .inject(this);

        if(this instanceof INetworkFragment) {
            mTwitter = mHttpClient.get().getTwitterService();
            mRxTwitter = mHttpClient.get().getRxTwitterService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRefWatcher.watch(this);
    }
}
