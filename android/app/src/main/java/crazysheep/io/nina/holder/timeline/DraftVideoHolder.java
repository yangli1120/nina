package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.adapter.RecyclerViewBaseAdapter;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.widget.TextureVideoView;

/**
 * video type draft holder
 *
 * Created by crazysheep on 16/3/26.
 */
public class DraftVideoHolder extends DraftBaseHolder
        implements RecyclerViewBaseAdapter.OnViewHolderLifeCallback<DraftVideoHolder> {

    @Bind(R.id.video_tvv) TextureVideoView mVideoTvv;

    public DraftVideoHolder(@NonNull ViewGroup view) {
        super(view);
        ButterKnife.bind(this, view);

        mVideoTvv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoTvv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ViewGroup.LayoutParams params = mVideoTvv.getLayoutParams();
                params.height = mVideoTvv.getMeasuredWidth();
                mVideoTvv.setLayoutParams(params);
            }
        });
    }

    @Override
    public void bindData(int position, PostTweetBean postTweetBean) {
        super.bindData(position, postTweetBean);

        mVideoTvv.setVideo(new File(mPostTweetBean.getVideoPreviewFile()), true);
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.item_tweet_video;
    }

    @Override
    public void onViewAttached(DraftVideoHolder holder) {
        mVideoTvv.setVideo(new File(mPostTweetBean.getVideoPreviewFile()), true);
    }

    @Override
    public void onViewRecycled(DraftVideoHolder holder) {
        mVideoTvv.release();
    }

    @Override
    public void onViewDetached(DraftVideoHolder holder) {
        mVideoTvv.release();
    }
}
