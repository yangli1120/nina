package crazysheep.io.nina.fragment;

import android.content.Context;
import android.support.annotation.NonNull;

import crazysheep.io.nina.R;
import crazysheep.io.nina.adapter.FragmentPagerBaseAdapter;

/**
 * profiel media fragment
 *
 * Created by crazysheep on 16/2/2.
 */
public class ProfileMediaFragment extends BaseFragment
        implements FragmentPagerBaseAdapter.IPagerFragment {

    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.profile_tab_media);
    }
}
