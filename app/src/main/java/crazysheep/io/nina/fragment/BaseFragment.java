package crazysheep.io.nina.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import crazysheep.io.nina.net.NetClient;
import retrofit.Retrofit;

/**
 * base fragment
 *
 * Created by crazysheep on 16/1/22.
 */
public class BaseFragment extends Fragment {

    public static String TAG = BaseFragment.class.getSimpleName();

    protected Retrofit mRetrofit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getClass().getSimpleName();

        mRetrofit = NetClient.getInstance(getActivity().getApplication());
    }

}
