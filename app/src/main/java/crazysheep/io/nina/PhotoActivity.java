package crazysheep.io.nina;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.SimpleTransitionListener;
import uk.co.senab.photoview.PhotoView;

/**
 * show big photo
 *
 * Created by crazysheep on 16/3/2.
 */
public class PhotoActivity extends BaseActivity {

    public static final String SHARED_ELEMENT_PHOTO = "shared:element:photo";

    @Bind(R.id.image_pv) PhotoView mPhotoPv;
    @Bind(R.id.transition_image_iv) ImageView mTransitionIv;

    private String photoUrl;
    private int[] thumbnailSizes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);

        photoUrl = getIntent().getStringExtra(BundleConstants.EXTRA_PHOTO_URL);
        if(TextUtils.isEmpty(photoUrl))
            finish();
        thumbnailSizes = getIntent().getIntArrayExtra(
                BundleConstants.EXTRA_PHOTO_THUMBNAIL_SIZE);

        if(APICompat.api21() && addTransitionListener()) {
            mTransitionIv.setVisibility(View.VISIBLE);
            mTransitionIv.getViewTreeObserver().addOnGlobalLayoutListener(
                    new OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mTransitionIv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            ViewGroup.LayoutParams params = mTransitionIv.getLayoutParams();
                            params.height = Math.round(mTransitionIv.getMeasuredWidth()
                                    * thumbnailSizes[1] * 1f / thumbnailSizes[0]);
                            mTransitionIv.setLayoutParams(params);
                        }
                    });

            ViewCompat.setTransitionName(mTransitionIv, SHARED_ELEMENT_PHOTO);
            loadThumbnailImage();
        } else {
            loadFullSizeImage();
        }
    }

    @TargetApi(APICompat.L)
    private boolean addTransitionListener() {
        Transition transition = getWindow().getSharedElementEnterTransition();
        if(!Utils.isNull(transition)) {
            transition.addListener(new SimpleTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    loadFullSizeImage();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    transition.removeListener(this);
                }
            });
            return true;
        }

        return false;
    }

    private void loadFullSizeImage() {
        Glide.with(this)
                .load(photoUrl)
                .dontAnimate()
                .fitCenter()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(
                            Exception e, String model, Target<GlideDrawable> target,
                            boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            GlideDrawable resource, String model, Target<GlideDrawable> target,
                            boolean isFromMemoryCache, boolean isFirstResource) {
                        Glide.clear(mTransitionIv);
                        mTransitionIv.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mPhotoPv);
    }

    private void loadThumbnailImage() {
        Glide.with(this)
                .load(photoUrl)
                .override(thumbnailSizes[0], thumbnailSizes[1])
                .into(mTransitionIv);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.image_pv)
    public void clickPhoto() {
        finishWithTransitionIfNeed();
    }

    @Override
    public void onBackPressed() {
        finishWithTransitionIfNeed();
    }

    private void finishWithTransitionIfNeed() {
        if(APICompat.api21()) {
            // because mTransitionIv have the shared element id, let mTransitionIv visible to start
            // return transition animation
            mPhotoPv.setVisibility(View.GONE);
            mTransitionIv.setVisibility(View.VISIBLE);

            finishAfterTransition();
        } else {
            finish();
        }
    }
}
