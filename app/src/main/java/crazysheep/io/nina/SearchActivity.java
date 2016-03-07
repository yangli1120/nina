package crazysheep.io.nina;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
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
import crazysheep.io.nina.adapter.TrendAdapter;
import crazysheep.io.nina.bean.LocationDto;
import crazysheep.io.nina.bean.PlaceTrendResultDto;
import crazysheep.io.nina.bean.TrendDto;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.constants.PermissionConstants;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.DialogUtils;
import crazysheep.io.nina.utils.ImeUtils;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.swiperefresh.LoadMoreRecyclerView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

    private static final int REQUEST_LOCATION_SETTINGS = 100;

    private final String[] mPers = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private LocationManager mLocationMgr;

    private boolean isRequestingEnableLocationSettings = false;

    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.data_rv) LoadMoreRecyclerView mDataRv;
    @Bind(R.id.search_view) SearchView mSearchView;
    @Bind(R.id.trend_nearby_iv) View mNearbyIv;
    @Bind(R.id.trend_global_iv) View mGlobalIv;
    @Bind(R.id.trend_ll) View mTrendLl;

    private TrendAdapter mTrendAdapter;
    private Observable<List<PlaceTrendResultDto>> mTrendObser;

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

        mNearbyIv.setOnClickListener(this);
        mGlobalIv.setOnClickListener(this);

        mDataRv.setLayoutManager(new LinearLayoutManager(this));
        mTrendAdapter = new TrendAdapter(this, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isRequestingEnableLocationSettings) {
            isRequestingEnableLocationSettings = false;
            queryLocation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseLocationListener();
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
                ImeUtils.hide(this);

                requestLocationPermission();
            }break;

            case R.id.trend_global_iv: {
                ImeUtils.hide(this);

                if(!Utils.isNull(mTrendObser))
                    mTrendObser.unsubscribeOn(AndroidSchedulers.mainThread());
                mTrendObser = mRxTwitter.trend(WOEID_GLOBAL)
                        .subscribeOn(Schedulers.io());
                mTrendObser.observeOn(AndroidSchedulers.mainThread())
                        .map(new Func1<List<PlaceTrendResultDto>, List<TrendDto>>() {
                            @Override
                            public List<TrendDto> call(
                                    List<PlaceTrendResultDto> placeTrendResultDtos) {
                                return Utils.size(placeTrendResultDtos) > 0
                                        ? placeTrendResultDtos.get(0).getTrends() : null;
                            }
                        })
                        .subscribe(new Action1<List<TrendDto>>() {
                            @Override
                            public void call(List<TrendDto> trendDtos) {
                                // show global trends
                                mTrendLl.setVisibility(View.GONE);

                                mTrendAdapter.setData(trendDtos);
                                mDataRv.setAdapter(mTrendAdapter);
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
            criteria.setAltitudeRequired(false);
            criteria.setCostAllowed(false);
            criteria.setBearingRequired(false);
            List<String> providers = mLocationMgr.getProviders(criteria, false);
            for(String provider : providers) {
                Location location = mLocationMgr.getLastKnownLocation(provider);
                if(!Utils.isNull(location)) {
                    requestNearbyTrend(location);
                    return;
                }
                mLocationMgr.requestLocationUpdates(provider, 5 * 1000, 5, this);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        releaseLocationListener();

        requestNearbyTrend(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        releaseLocationListener();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        // user has disable location settings, show dialog if user want enable
        DialogUtils.showConfirmDialog(this,
                "location setting is disable", "want enable location settings?",
                new DialogUtils.ButtonAction() {
                    @Override
                    public String getTitle() {
                        return getString(R.string.ok_btn);
                    }

                    @Override
                    public void onClick(DialogInterface dialog) {
                        isRequestingEnableLocationSettings = true;
                        ActivityUtils.startResult(getActivity(), REQUEST_LOCATION_SETTINGS,
                                new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                },
                new DialogUtils.ButtonAction() {
                    @Override
                    public String getTitle() {
                        return getString(R.string.cancel_btn);
                    }

                    @Override
                    public void onClick(DialogInterface dialog) {
                    }
                });
    }

    private void requestNearbyTrend(@NonNull Location location) {
        if(!Utils.isNull(mTrendObser))
            mTrendObser.unsubscribeOn(AndroidSchedulers.mainThread());
        mTrendObser = mRxTwitter.closest(location.getLatitude(), location.getLongitude())
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<LocationDto>, LocationDto>() {
                    @Override
                    public LocationDto call(List<LocationDto> locationDtos) {
                        if(Utils.size(locationDtos) > 0)
                            return locationDtos.get(0);
                        else
                            throw Exceptions.propagate(
                                    new Throwable("api \"closest()\" request failed"));
                    }
                })
                .flatMap(new Func1<LocationDto, Observable<List<PlaceTrendResultDto>>>() {
                    @Override
                    public Observable<List<PlaceTrendResultDto>> call(LocationDto locationDto) {
                        return mRxTwitter.trend(locationDto.woeid);
                    }
                });
        mTrendObser.observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<PlaceTrendResultDto>, List<TrendDto>>() {
                    @Override
                    public List<TrendDto> call(List<PlaceTrendResultDto> placeTrendResultDtos) {
                        return Utils.size(placeTrendResultDtos) > 0
                                ? placeTrendResultDtos.get(0).getTrends() : null;
                    }
                })
                .subscribe(new Action1<List<TrendDto>>() {
                    @Override
                    public void call(List<TrendDto> trendDtos) {
                        // show nearby trend
                        mTrendLl.setVisibility(View.GONE);

                        mTrendAdapter.setData(trendDtos);
                        mDataRv.setAdapter(mTrendAdapter);
                    }
                });
    }

    private void releaseLocationListener() {
        if(!Utils.isNull(mLocationMgr))
            try {
                mLocationMgr.removeUpdates(this);
                mLocationMgr = null;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
    }

}
