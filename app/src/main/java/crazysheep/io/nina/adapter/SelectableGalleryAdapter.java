package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.MediaStoreImageBean;
import crazysheep.io.nina.utils.Utils;

/**
 * gallery adapter
 *
 * Created by crazysheep on 16/2/23.
 */
public class SelectableGalleryAdapter extends RecyclerViewBaseAdapter<
        SelectableGalleryAdapter.ImageHolder, MediaStoreImageBean> {

    private SparseBooleanArray mSelectMap = new SparseBooleanArray();
    private static int MAX_SELECTION = 4;
    private boolean showCameraButton = false;

    public SelectableGalleryAdapter(@NonNull Context context, List<MediaStoreImageBean> items,
                                    boolean showCameraButton) {
        super(context, items);
        this.showCameraButton = showCameraButton;

        if(showCameraButton) {
            List<MediaStoreImageBean> tempItems = new ArrayList<>(Utils.size(items) + 1);
            // a invalid data, just place for take photo button
            tempItems.add(new MediaStoreImageBean());
            if(!Utils.isNull(items))
                tempItems.addAll(items);

            mItems = tempItems;
        }
    }

    @Override
    public void setData(List<MediaStoreImageBean> items) {
        if(showCameraButton) {
            List<MediaStoreImageBean> tempItems = new ArrayList<>(Utils.size(items) + 1);
            // a invalid data, just place for take photo button
            tempItems.add(new MediaStoreImageBean());
            if(!Utils.isNull(items))
                tempItems.addAll(items);
            super.setData(tempItems);
        } else {
            super.setData(items);
        }
    }

    public void setMaxSelection(int max) {
        MAX_SELECTION = max;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(ImageHolder holder, int position) {
        if(showCameraButton && isHeader(position)) {
            holder.imgTv.setVisibility(View.GONE);

            Glide.clear(holder.imgIv);
            holder.imgIv.setImageResource(R.drawable.ic_photo_camera_48dp);
        } else {
            MediaStoreImageBean imageBean = getItem(position);

            Glide.clear(holder.imgIv);
            Glide.with(mContext)
                    .load(new File(imageBean.filepath))
                    .asBitmap()
                    .placeholder(R.color.place_holder_bg)
                    .listener(GlidePalette.with(imageBean.filepath)
                            .use(GlidePalette.Profile.VIBRANT)
                            .intoBackground(holder.imgTv))
                    .fitCenter()
                    .into(holder.imgIv);

            holder.imgTv.setVisibility(View.VISIBLE);
            holder.imgTv.setText(imageBean.title);

            holder.selectedV.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected ImageHolder onCreateHolder(ViewGroup parent, int viewType) {
        return new ImageHolder(mInflater.inflate(R.layout.item_gallery_image, parent, false));
    }

    public void toggleSelection(int position) {
        if(position >= 0 && position < getItemCount()) {
            if(mSelectMap.get(position, false)) {
                mSelectMap.delete(position);
            } else if(getSelectedPositions().size() > MAX_SELECTION - 1) {
                // beyond max selection
            } else {
                mSelectMap.put(position, true);
            }

            notifyItemChanged(position);
        }
    }

    public void toggleSelection(@NonNull MediaStoreImageBean imageBean) {
        for(MediaStoreImageBean item : mItems) {
            if(item.id == imageBean.id) {
                toggleSelection(findItemPosition(item));
            }
        }
    }

    public boolean isSelected(int position) {
        return mSelectMap.get(position, false);
    }

    public void clearAllSelection() {
        List<Integer> selectedItems = getSelectedPositions();
        mSelectMap.clear();
        for(Integer integer : selectedItems)
            notifyItemChanged(integer);
    }

    public List<Integer> getSelectedPositions() {
        List<Integer> selectedItems = new ArrayList<>(mSelectMap.size());
        for(int i = 0; i < mSelectMap.size(); i++)
            selectedItems.add(mSelectMap.keyAt(i));

        return selectedItems;
    }

    public List<MediaStoreImageBean> getSelectedItems() {
        List<MediaStoreImageBean> selected = new ArrayList<>();
        for(Integer position : getSelectedPositions())
            selected.add(getItem(position));

        return selected;
    }

    ///////////////////////// view holder /////////////////////////

    static class ImageHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.gallery_img_iv) ImageView imgIv;
        @Bind(R.id.gallery_img_tv) TextView imgTv;
        @Bind(R.id.selected_state_v) View selectedV;

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
