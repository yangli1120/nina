package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.MediaStoreImageBean;
import crazysheep.io.nina.utils.Utils;

/**
 * preview gallery adapter, see{@link crazysheep.io.nina.PostTweetActivity}
 *
 * Created by crazysheep on 16/2/24.
 */
public class PreviewGalleryAdapter extends RecyclerViewBaseAdapter<
        PreviewGalleryAdapter.ImageHolder, MediaStoreImageBean> {

    /////////////////////// listener /////////////////////////////

    public interface OnItemRemoveListener {
        void onRemoved(int position);
    }
    //////////////////////////////////////////////////////////////

    private OnItemRemoveListener mOnItemRemoveListener;

    public PreviewGalleryAdapter(@NonNull Context context, List<MediaStoreImageBean> items) {
        super(context, items);
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        mOnItemRemoveListener = listener;
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, final int position) {
        Glide.clear(holder.previewIv);
        Glide.with(mContext)
                .load(new File(getItem(position).filepath))
                .placeholder(R.color.place_holder_bg)
                .into(holder.previewIv);
    }

    @Override
    protected ImageHolder onCreateHolder(ViewGroup parent, int viewType) {
        return new ImageHolder(mInflater.inflate(R.layout.item_image_preview, parent, false), this);
    }

    //////////////////////// view holder //////////////////////////

    static class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.image_preview_iv) ImageView previewIv;
        @Bind(R.id.remove_image_iv) ImageView removeIv;

        private PreviewGalleryAdapter mAdapter;

        public ImageHolder(@NonNull View view, @NonNull PreviewGalleryAdapter adapter) {
            super(view);
            mAdapter = adapter;
            ButterKnife.bind(this, view);

            itemView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            ViewGroup.LayoutParams params = itemView.getLayoutParams();
                            params.height = itemView.getMeasuredWidth();
                            itemView.setLayoutParams(params);
                        }
                    });

            removeIv.setOnClickListener(this);
            previewIv.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.remove_image_iv: {
                    int removePos = getAdapterPosition();
                    if(!Utils.isNull(mAdapter.mOnItemRemoveListener))
                        mAdapter.mOnItemRemoveListener.onRemoved(removePos);
                    mAdapter.removeItem(removePos);
                }break;
            }
        }
    }
}
