package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import java.util.List;

/**
 * profile pager adapter, see{@link crazysheep.io.nina.ProfileActivity}
 *
 * Created by crazysheep on 16/2/2.
 */
public class ProfilePagerAdapter<T extends FragmentPagerBaseAdapter.IPagerFragment>
        extends FragmentPagerBaseAdapter {

    public ProfilePagerAdapter(@NonNull Context context, @NonNull FragmentManager ftMgr,
                               @NonNull List<T> fts) {
        super(context, ftMgr, fts);
    }

}
