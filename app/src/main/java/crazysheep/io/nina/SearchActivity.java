package crazysheep.io.nina;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.bean.PlaceTrendResultDto;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.constants.PermissionConstants;
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.utils.DebugHelper;
import crazysheep.io.nina.utils.Utils;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Response;

/**
 * search twitter activity
 *
 * Created by crazysheep on 16/3/5.
 */
public class SearchActivity extends BaseSwipeBackActivity
        implements SearchView.OnQueryTextListener, BaseActivity.ITwitterServiceActivity,
                   View.OnClickListener, LocationListener {

    // see{@link https://dev.twitter.com/rest/reference/get/trends/place}
    private static final long WOEID_GLOBAL = 1;

    private final String[] mPers = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private LocationManager mLocationMgr;

    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.data_rv) RecyclerView mResultRv;
    @Bind(R.id.search_view) SearchView mSearchView;
    @Bind(R.id.trend_nearby_iv) View mNearbyIv;
    @Bind(R.id.trend_global_iv) View mGlobalIv;
    @Bind(R.id.trend_ll) View mTrendLl;

    private Call<List<PlaceTrendResultDto>> mTrendCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        overridePendingTransition(0, android.R.anim.slide_out_right);

        mCoordinatorLayout.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mCoordinatorLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                if (APICompat.api21()) {
                    Animator animator = android.view.ViewAnimationUtils.createCircularReveal(
                            mCoordinatorLayout, mCoordinatorLayout.getWidth(), 0, 0,
                            (float) Math.hypot(mCoordinatorLayout.getWidth(),
                                    mCoordinatorLayout.getHeight()));
                    animator.setDuration(300).setInterpolator(new AccelerateInterpolator());
                    animator.start();
                } else {
                    SupportAnimator revealAnimator = ViewAnimationUtils.createCircularReveal(
                            mCoordinatorLayout, mCoordinatorLayout.getWidth(), 0, 0,
                            (float) Math.hypot(mCoordinatorLayout.getWidth(),
                                    mCoordinatorLayout.getHeight()));
                    revealAnimator.setDuration(300).setInterpolator(new AccelerateInterpolator());
                    revealAnimator.start();
                }

                return true;
            }
        });
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);
        mSearchView.setQueryHint(getString(R.string.search_twitter));
        ImageView iconIv = ButterKnife.findById(mSearchView,
                android.support.v7.appcompat.R.id.search_mag_icon);
        iconIv.setImageResource(
                android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        iconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // set cursor color
        // see{@link http://stackoverflow.com/questions/18705185/changing-the-cursor-color-in-searchview-without-actionbarsherlock}
        AutoCompleteTextView autoEt = ButterKnife.findById(mSearchView,
                android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field cursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            cursorDrawableRes.setAccessible(true);
            cursorDrawableRes.set(autoEt,
                    android.support.v7.appcompat.R.drawable.abc_text_cursor_material);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @AfterPermissionGranted(PermissionConstants.RC_ACCESS_LOCATION)
    private void requestLocationPermission() {
        if(EasyPermissions.hasPermissions(this, mPers)) {
            // permission allowed, do request
            queryLocation();
        } else {
            EasyPermissions.requestPermissions(this, null,
                    PermissionConstants.RC_ACCESS_LOCATION, mPers);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trend_nearby_iv: {
                requestLocationPermission();
            }break;

            case R.id.trend_global_iv: {
                if(!Utils.isNull(mTrendCall))
                    mTrendCall.cancel();
                mTrendCall = mTwitter.trend(WOEID_GLOBAL);
                mTrendCall.enqueue(new NiceCallback<List<PlaceTrendResultDto>>() {
                    @Override
                    public void onRespond(Response<List<PlaceTrendResultDto>> response) {
                        // TODO show global trends
                    }

                    @Override
                    public void onFailed(Throwable t) {
                    }
                });
            }break;
        }
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
        queryLocation();
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {
    }

    private void queryLocation() {
        if(Utils.isNull(mLocationMgr))
            mLocationMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            List<String> providers = mLocationMgr.getProviders(criteria, false);
            for(String provider : providers)
                mLocationMgr.requestLocationUpdates(provider, 5 * 1000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO search place id, then search popular nearby
        DebugHelper.log("onLocationChanged(), geo: " + String.format("[%1s, %2s]",
                location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        DebugHelper.log("onStatusChanged(), status: " + status + ", provider: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        DebugHelper.log("onProviderEnabled, provider: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        DebugHelper.log("onProviderDisabled, provider: " + provider);
    }

}
