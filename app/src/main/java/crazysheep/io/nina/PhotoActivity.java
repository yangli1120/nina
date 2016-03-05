package crazysheep.io.nina;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.transition.Transition;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.commit451.elasticdragdismisslayout.ElasticDragDismissFrameLayout;
import com.commit451.elasticdragdismisslayout.ElasticDragDismissListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import crazysheep.io.nina.compat.APICompat;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.utils.Utils;
import crazysheep.io.nina.widget.SimpleTransitionListener;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * show big photo
 *
 * Created by crazysheep on 16/3/2.
 */
public class PhotoActivity extends BaseActivity {

    public static final String SHARED_ELEMENT_PHOTO = "shared:element:photo";

    @Bind(R.id.drag_dismiss_layout) ElasticDragDismissFrameLayout mDragDismissFl;
    @Bind(R.id.image_iv) ImageView mPhotoIv;

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
            loadThumbnailImage();
            ViewCompat.setTransitionName(mPhotoIv, SHARED_ELEMENT_PHOTO);
        } else {
            loadFullSizeImage();
        }
        mDragDismissFl.addListener(new ElasticDragDismissListener() {
            @Override
            public void onDrag(float elasticOffset, float elasticOffsetPixels,
                               float rawOffset, float rawOffsetPixels) {
            }

            @Override
            public void onDragDismissed() {
                finishWithTransitionIfNeed();
            }
        });
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
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(
                            GlideDrawable resource,
                            GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mPhotoIv.setImageDrawable(resource);
                        PhotoViewAttacher attacher = new PhotoViewAttacher(mPhotoIv);
                        attacher.update();
                    }
                });
    }

    private void loadThumbnailImage() {
        // be careful, to reuse thumbnail bitmap in memory cache as soon as possible,
        // all request options must same as {@link crazysheep.io.nina.holder.timeline.ImageHolder}
        // dose, in ImageHolder.onAttach(), I used override(w, h) and centerCrop(),
        // so here must use same options to let Glide know I want the cache in memory as soon as
        // possible for nice transition animation use, if I use fitCenter() or other options here
        // not same as before, Glide will load another bitmap when play transition animation,
        // that's weird
        Glide.with(this)
                .load(photoUrl)
                .override(thumbnailSizes[0], thumbnailSizes[1])
                .priority(Priority.IMMEDIATE)
                .centerCrop()
                .dontAnimate()
                .into(mPhotoIv);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.image_iv)
    public void clickPhoto() {
        finishWithTransitionIfNeed();
    }

    @Override
    public void onBackPressed() {
        finishWithTransitionIfNeed();
    }

    private void finishWithTransitionIfNeed() {
        if(APICompat.api21()) {
            ActivityCompat.finishAfterTransition(this);
        } else {
            finish();
        }
    }
}
