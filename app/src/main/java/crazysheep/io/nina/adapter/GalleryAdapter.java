package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.MediaStoreImageBean;

/**
 * gallery adapter
 *
 * Created by crazysheep on 16/2/23.
 */
public class GalleryAdapter extends RecyclerViewBaseAdapter<GalleryAdapter.ImageHolder,
        MediaStoreImageBean> {

    public GalleryAdapter(@NonNull Context context, List<MediaStoreImageBean> items) {
        super(context, items);
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        MediaStoreImageBean imageBean = getItem(position);

        Glide.clear(holder.imgIv);
        Glide.with(mContext)
                .load(new File(imageBean.filepath))
                .placeholder(R.color.place_holder_bg)
                .fitCenter()
                .into(holder.imgIv);

        holder.imgTv.setText(imageBean.title);
    }

    @Override
    protected ImageHolder onCreateHolder(ViewGroup parent, int viewType) {
        return new ImageHolder(mInflater.inflate(R.layout.item_gallery_image, parent, false));
    }

    ///////////////////////// view holder /////////////////////////

    static class ImageHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.gallery_img_iv) ImageView imgIv;
        @Bind(R.id.gallery_img_tv) TextView imgTv;

        public ImageHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);

            itemView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)itemView
                                    .getLayoutParams();
                            params.height = itemView.getMeasuredWidth();
                            itemView.setLayoutParams(params);
                        }
                    });
        }
    }
}
