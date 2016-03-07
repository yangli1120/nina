package crazysheep.io.nina.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TrendDto;

/**
 * adapter for twitter trends, see{@link crazysheep.io.nina.SearchActivity}
 *
 * Created by crazysheep on 16/3/6.
 */
public class TrendAdapter extends RecyclerViewBaseAdapter<TrendAdapter.TrendHolder, TrendDto> {

    public TrendAdapter(@NonNull Context context, List<TrendDto> items) {
        super(context, items);
    }

    @Override
    public void onBindViewHolder(TrendHolder holder, int position) {
        TrendDto trendDto = getItem(position);
        holder.trendNameTv.setText(trendDto.name.trim());
        holder.trendCountTv.setText(
                mContext.getString(R.string.trend_count, trendDto.tweet_volume));
    }

    @Override
    protected TrendHolder onCreateHolder(ViewGroup parent, int viewType) {
        return new TrendHolder(mInflater.inflate(R.layout.item_trend, parent, false));
    }

    /////////////////////////// holder ////////////////////////////

    public static class TrendHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @Bind(R.id.trend_name_tv) TextView trendNameTv;
        @Bind(R.id.trend_count_tv) TextView trendCountTv;

        public TrendHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // TODO click event
        }
    }
}
