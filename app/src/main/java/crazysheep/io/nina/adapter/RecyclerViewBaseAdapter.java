package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import crazysheep.io.nina.utils.Utils;

/**
 * base adapter for RecyclerView
 *
 * Created by crazysheep on 15/12/17.
 */
public abstract class RecyclerViewBaseAdapter<VH extends RecyclerView.ViewHolder, DT>
        extends RecyclerView.Adapter<VH> {

    //////////////////////// api /////////////////////////

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    public interface OnViewHolderLifeCallback<VH extends RecyclerView.ViewHolder> {
        void onViewRecycled(VH holder);
        void onViewAttached(VH holder);
        void onViewDetached(VH holder);
    }

    //////////////////////////////////////////////////////

    protected Context mContext;
    protected List<DT> mItems;
    protected LayoutInflater mInflater;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public RecyclerViewBaseAdapter(@NonNull Context context, List<DT> items) {
        mContext = context;
        mItems = items;
        if(mItems == null)
            mItems = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);
    }

    public void setData(List<DT> items) {
        mItems = items;
        if(mItems == null)
            mItems = new ArrayList<>();

        notifyDataSetChanged();
    }

    public void addData(List<DT> moreItems) {
        if(!Utils.isNull(moreItems)) {
            mItems.addAll(moreItems);
            notifyDataSetChanged();
        }
    }

    public List<DT> getData() {
        return mItems;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    protected boolean isFooter(int position) {
        return getItemCount() - 1 == position;
    }

    protected final void removeItem(int position) {
        if(position >= 0 && position < getItemCount()) {
            mItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    protected final void removeItem(@NonNull DT item) {
        int index = mItems.indexOf(item);
        if(index >= 0 && index < getItemCount()) {
            mItems.remove(index);
            notifyItemRemoved(index);
        }
    }

    protected final int findItemPosition(@NonNull DT item) {
        return mItems.indexOf(item);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        VH holder = onCreateHolder(parent, viewType);
        updateClickListener(holder);

        return holder;
    }

    protected abstract VH onCreateHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(VH holder, int position);

    @Override
    @SuppressWarnings("unchecked")
    public void onViewRecycled(VH holder) {
        updateClickListener(holder);

        if(holder instanceof OnViewHolderLifeCallback)
            ((OnViewHolderLifeCallback)holder).onViewRecycled(holder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewDetachedFromWindow(VH holder) {
        if(holder instanceof OnViewHolderLifeCallback)
            ((OnViewHolderLifeCallback)holder).onViewDetached(holder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewAttachedToWindow(VH holder) {
        if(holder instanceof OnViewHolderLifeCallback)
            ((OnViewHolderLifeCallback)holder).onViewAttached(holder);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public DT getItem(int position) {
        return mItems.get(position);
    }

    private void updateClickListener(final VH holder) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLongClickListener != null)
                    mOnItemLongClickListener.onItemLongClick(holder.itemView,
                            holder.getAdapterPosition());

                return true;
            }
        });
    }

}
