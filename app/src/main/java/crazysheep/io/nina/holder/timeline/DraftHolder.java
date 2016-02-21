package crazysheep.io.nina.holder.timeline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.prefs.UserPrefs;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * draft holder
 *
 * Created by crazysheep on 16/2/19.
 */
public class DraftHolder extends BaseHolder<PostTweetBean> {

    private Context mContext;
    private UserPrefs mUserPrefs;

    @Bind(R.id.author_avatar_iv) CircleImageView avatarIv;
    @Bind(R.id.author_name_tv) TextView authorTv;
    @Bind(R.id.author_screen_name_tv) TextView authorScreenNameTv;
    @Bind(R.id.tweet_content_tv) TextView contentTv;

    public DraftHolder(@NonNull ViewGroup view) {
        super(view);
        mContext = view.getContext();
        mUserPrefs = new UserPrefs(mContext);
        ButterKnife.bind(this, view);
    }

    @Override
    public void bindData(int position, PostTweetBean postTweetBean) {
        // TODO bind draft UI with data
        Glide.clear(avatarIv);
        Glide.with(mContext)
                .load(mUserPrefs.getUserAvatar())
                .into(avatarIv);

        authorTv.setText(mUserPrefs.getUsername());
        authorScreenNameTv.setText(
                mContext.getString(R.string.screen_name , mUserPrefs.getUserScreenName()));

        contentTv.setText(postTweetBean.getStatus());
    }
}
