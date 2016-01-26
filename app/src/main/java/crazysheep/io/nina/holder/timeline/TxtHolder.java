package crazysheep.io.nina.holder.timeline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.TweetDto;

/**
 * txt tweet
 *
 * Created by crazysheep on 16/1/23.
 */
public class TxtHolder extends BaseHolder {

    @Bind(R.id.tweet_content_etv) ExpandableTextView txtEtv;

    public TxtHolder(@NonNull ViewGroup parent, @NonNull Context context) {
        super(parent, context);
        ButterKnife.bind(this, parent);
    }

    @Override
    public int getContentViewRes() {
        return R.layout.item_tweet_txt;
    }

    @Override
    public void bindData(int position, @NonNull TweetDto tweetDto) {
        super.bindData(position, tweetDto);

        txtEtv.setText(tweetDto.text);
    }
}
