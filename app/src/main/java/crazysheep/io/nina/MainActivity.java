package crazysheep.io.nina;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.fragment.TimelineFragment;

public class MainActivity extends BaseActivity {

    @Bind(R.id.drawer) DrawerLayout mDrawer;
    @Bind(R.id.nav_layout) NavigationView mNav;

    private TimelineFragment mTimelineFt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTimelineFt = new TimelineFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_ft, mTimelineFt, TimelineFragment.TAG)
                .commitAllowingStateLoss();
    }

    public void setToolbar(@NonNull Toolbar toolbar) {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle abToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(abToggle);
        abToggle.syncState();
    }

}
