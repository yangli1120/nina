package crazysheep.io.nina;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.adapter.RecyclerViewBaseAdapter;
import crazysheep.io.nina.adapter.SelectableGalleryAdapter;
import crazysheep.io.nina.bean.MediaStoreImageBean;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.db.RxDB;
import crazysheep.io.nina.utils.ActivityUtils;
import crazysheep.io.nina.utils.CameraUtils;
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

    ///////////////////////// api //////////////////////////////

    @ParcelablePlease
    public static class Options implements Parcelable {

        public static final Options DEFAULT = new Options.Builder().chooseImage().build();

        private static final int FLAG_CHOOSE_IMAGE = 0x0001;
        private static final int FLAG_TAKE_PHOTO = 0x0010;
        private static final int FLAG_CAPTURE_VIDEO = 0x0100;

        protected int flag = FLAG_CHOOSE_IMAGE;

        public Options() {}

        private Options(int flag) {
            this.flag = flag;
        }

        public boolean chooseImage() {
            return (flag & FLAG_CHOOSE_IMAGE) != 0;
        }

        public boolean takePhoto() {
            return (flag & FLAG_TAKE_PHOTO) != 0;
        }

        public boolean captureVideo() {
            return (flag & FLAG_CAPTURE_VIDEO) != 0;
        }

        public static class Builder {

            private int flag = FLAG_CHOOSE_IMAGE;

            public Builder() {}

            public Builder chooseImage() {
                flag |= FLAG_CHOOSE_IMAGE;
                return this;
            }

            public Builder takePhoto() {
                flag |= FLAG_TAKE_PHOTO;
                return this;
            }

            public Builder captureVideo() {
                flag |= FLAG_CAPTURE_VIDEO;
                return this;
            }

            public Options build() {
                return new Options(flag);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            crazysheep.io.nina.OptionsParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Options> CREATOR = new Creator<Options>() {
            public Options createFromParcel(Parcel source) {
                Options target = new Options();
                crazysheep.io.nina.OptionsParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public Options[] newArray(int size) {
                return new Options[size];
            }
        };
    }
    ////////////////////////////////////////////////////////////

    private static final int REQUEST_TAKE_PHOTO = 111;
    private static final int REQUEST_CAPTURE_VIDEO = 112;

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.data_rv) RecyclerView mGalleryRv;
    private SelectableGalleryAdapter mAdapter;

    private ArrayList<MediaStoreImageBean> mSelectedImages;

    private Options mOptions;

    private File mPhotoFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        mSelectedImages = getIntent().getParcelableArrayListExtra(
                BundleConstants.EXTRA_SELECTED_IMAGES);
        mOptions = getIntent().getParcelableExtra(BundleConstants.EXTRA_GALLERY_OPTIONS);
        if(Utils.isNull(mOptions))
            mOptions = Options.DEFAULT;

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
            getSupportActionBar().setHomeAsUpIndicator(
                    android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        }
        GridLayoutManager layoutMgr = new GridLayoutManager(this, 3);
        mGalleryRv.setLayoutManager(layoutMgr);
        mAdapter = new SelectableGalleryAdapter(this, null, mOptions.takePhoto(),
                mOptions.captureVideo());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if(!Utils.isNull(mPhotoFile)) {
                        // return to request activity
                        Intent result = new Intent();
                        MediaStoreImageBean imageBean = new MediaStoreImageBean();
                        imageBean.filepath = mPhotoFile.getAbsolutePath();
                        imageBean.title = mPhotoFile.getName();
                        ArrayList<MediaStoreImageBean> images = new ArrayList<>(1);
                        images.add(imageBean);
                        result.putExtra(BundleConstants.EXTRA_SELECTED_IMAGES, images);
                        setResult(Activity.RESULT_OK, result);
                        finish();
                    }
                }break;

                case REQUEST_CAPTURE_VIDEO: {
                    // handle capture video
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }break;
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if(mOptions.takePhoto() && position == 0) {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(!Utils.isNull(takePhotoIntent.resolveActivity(getPackageManager()))) {
                mPhotoFile = null;
                try {
                    mPhotoFile = CameraUtils.createImageFile();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                if(!Utils.isNull(mPhotoFile)) {
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                    ActivityUtils.startResult(this, REQUEST_TAKE_PHOTO, takePhotoIntent);
                }
            }
        } else if(mOptions.captureVideo() && position == 1) {
            ActivityUtils.startResult(this, REQUEST_CAPTURE_VIDEO,
                    ActivityUtils.prepare(this, CaptureVideoActivity.class));
        } else {
            mAdapter.toggleSelection(position);
            invalidateOptionsMenu();
        }
    }

    private int calculateSystemUIHeight() {
        return Math.round(SystemUIHelper.getStatusBarSize(this)
                + SystemUIHelper.getToolbarSize(this));
    }

}
