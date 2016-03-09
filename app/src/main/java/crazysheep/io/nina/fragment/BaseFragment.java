package crazysheep.io.nina.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import crazysheep.io.nina.application.BaseApplication;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.TwitterService;
import crazysheep.io.nina.prefs.SettingPrefs;
import crazysheep.io.nina.prefs.UserPrefs;
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

    @Inject protected Lazy<UserPrefs> mUserPrefs;
    @Inject protected Lazy<SettingPrefs> mSettingPrefs;

    protected TwitterService mTwitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // dagger2 inject
        BaseApplication.from(getContext()).getComponent().inject(this);

        TAG = getClass().getSimpleName();

        if(this instanceof INetworkFragment)
            mTwitter = HttpClient.getInstance().getTwitterService();
    }

}
