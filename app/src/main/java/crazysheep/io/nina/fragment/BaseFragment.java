package crazysheep.io.nina.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.TwitterService;

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

    protected TwitterService mTwitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getClass().getSimpleName();

        if(this instanceof INetworkFragment)
            mTwitter = HttpClient.getInstance().getTwitterService();
    }

}
