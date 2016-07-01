package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * base FragmentPagerAdapter for easy use
 *
 * Created by crazysheep on 16/2/2.
 */
public class FragmentPagerBaseAdapter<T extends FragmentPagerBaseAdapter.IPagerFragment>
        extends FragmentPagerAdapter {

    ////////////////////////////// api ///////////////////////////////
    public interface IPagerFragment {
        String getTitle(Context context);
    }
    //////////////////////////////////////////////////////////////////

    protected List<T> mFragments;
    private Context mContext;

    public FragmentPagerBaseAdapter(@NonNull Context context, @NonNull FragmentManager ftMgr,
                                    @NonNull List<T> fts) {
        super(ftMgr);
        mFragments = fts;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return (Fragment)mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getTitle(mContext);
    }

}
