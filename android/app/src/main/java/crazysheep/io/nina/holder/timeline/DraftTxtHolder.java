package crazysheep.io.nina.holder.timeline;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import crazysheep.io.nina.R;

/**
 * txt type draft holder
 *
 * Created by crazysheep on 16/2/25.
 */
public class DraftTxtHolder extends DraftBaseHolder {

    public DraftTxtHolder(@NonNull ViewGroup view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.item_tweet_txt;
    }
}
