package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;

/**
 * txt type tweet
 *
 * Created by crazysheep on 16/1/23.
 */
public class TxtHolder extends BaseHolder {

    public TxtHolder(@NonNull ViewGroup parent) {
        super(parent);
        ButterKnife.bind(this, parent);
    }

    @Override
    public int getContentViewRes() {
        return R.layout.item_tweet_txt;
    }

    @Override
    public void bindData(int position, @NonNull TweetDto tweetDto) {
        super.bindData(position, tweetDto);
    }

}
