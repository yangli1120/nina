package crazysheep.io.nina;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.adapter.SelectableGalleryAdapter;
import crazysheep.io.nina.adapter.RecyclerViewBaseAdapter;
import crazysheep.io.nina.bean.MediaStoreImageBean;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.db.RxDB;
import crazysheep.io.nina.utils.SystemUIHelper;
import crazysheep.io.nina.utils.ToastUtils;
import crazysheep.io.nina.utils.Utils;

/**
 * show images in system media store and show a button let user open camera take photo
 * </p>
 * Created by crazysheep on 16/2/23.
 */
public class GalleryActivity extends BaseSwipeBackActivity
        implements RecyclerViewBaseAdapter.OnItemClickListener {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.data_rv) RecyclerView mGalleryRv;
    private SelectableGalleryAdapter mAdapter;

    private ArrayList<MediaStoreImageBean> mSelectedImages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        mSelectedImages = getIntent().getParcelableArrayListExtra(
                BundleConstants.EXTRA_SELECTED_IMAGES);

        setSupportActionBar(mToolbar);
        if(!Utils.isNull(getSupportActionBar())) {
            // about change actionbar title color programmatically,
            // see{@link http://stackoverflow.com/questions/9920277/how-to-change-action-bar-title-color-in-code}
            Spannable title = new SpannableString(getString(R.string.gallery));
            title.setSpan(new ForegroundColorSpan(Color.WHITE), 0, title.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(title);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_36dp);
        }
        GridLayoutManager layoutMgr = new GridLayoutManager(this, 4);
        mGalleryRv.setLayoutManager(layoutMgr);
        mAdapter = new SelectableGalleryAdapter(this, null);
        mAdapter.setOnItemClickListener(this);
        mGalleryRv.setAdapter(mAdapter);
        // how to set recyclerview's padding,
        // see{@link http://stackoverflow.com/questions/24914191/recyclerview-cliptopadding-false}
        mGalleryRv.setPadding(
                mGalleryRv.getPaddingLeft(),
                mGalleryRv.getPaddingTop() + calculateSystemUIHeight(),
                mGalleryRv.getPaddingRight(), mGalleryRv.getPaddingBottom());

        RxDB.getAllImages(getContentResolver(), new RxDB.Callback<List<MediaStoreImageBean>>() {
            @Override
            public void onResult(List<MediaStoreImageBean> mediaStoreImageBeans) {
                mAdapter.setData(mediaStoreImageBeans);

                if(!Utils.isNull(mSelectedImages)) {
                    for (MediaStoreImageBean imageBean : mSelectedImages)
                        mAdapter.toggleSelection(imageBean);
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onFailed(String err) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        MenuItem doneItem = menu.findItem(R.id.select_done);
        doneItem.setTitle(getString(R.string.select_done, mAdapter.getSelectedPositions().size()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_done: {
                if(mAdapter.getSelectedPositions().size() == 0) {
                    ToastUtils.t(this, getString(R.string.toast_select_no_image));
                } else {
                    // return select images
                    Intent data = new Intent();
                    data.putParcelableArrayListExtra(BundleConstants.EXTRA_SELECTED_IMAGES,
                            (ArrayList<MediaStoreImageBean>)mAdapter.getSelectedItems());
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }

                return true;
            }

            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        mAdapter.toggleSelection(position);
        invalidateOptionsMenu();
    }

    private int calculateSystemUIHeight() {
        return Math.round(SystemUIHelper.getStatusBarSize(this)
                + SystemUIHelper.getToolbarSize(this));
    }

}
