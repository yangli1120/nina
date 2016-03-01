package crazysheep.io.nina.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import crazysheep.io.nina.bean.ITweet;
import crazysheep.io.nina.bean.PostTweetBean;
import crazysheep.io.nina.bean.TweetDto;
import crazysheep.io.nina.holder.timeline.BaseHolder;
import crazysheep.io.nina.holder.timeline.NormalBaseHolder;
import crazysheep.io.nina.holder.timeline.TimelineHolderFactory;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.NiceCallback;
import crazysheep.io.nina.utils.L;
import crazysheep.io.nina.widget.recyclerviewhelper.ItemTouchHelperAdapter;
import retrofit2.Response;

/**
 * adapter for twitter timeline, see{@link crazysheep.io.nina.fragment.TimelineFragment}
 *
 * Created by crazysheep on 16/1/23.
 */
public class TimelineAdapter<T extends BaseHolder> extends RecyclerViewBaseAdapter<T, ITweet>
        implements ItemTouchHelperAdapter {

    public TimelineAdapter(@NonNull Context context, List<ITweet> data) {
        super(context, data);
    }

    @Override
    protected T onCreateHolder(ViewGroup parent, int viewType) {
        return TimelineHolderFactory.createHolder(mInflater, parent, viewType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(T holder, int position) {
        holder.bindData(position, getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineHolderFactory.getViewType(getItem(position));
    }

    public List<PostTweetBean> getDraftItems() {
        List<PostTweetBean> drafts = new ArrayList<>();
        for(ITweet iTweet : getData())
            if(iTweet instanceof PostTweetBean)
                drafts.add((PostTweetBean)iTweet);

        return drafts;
    }

    public List<TweetDto> getTweets() {
        List<TweetDto> tweets = new ArrayList<>();
        for(ITweet iTweet : getData())
            if(iTweet instanceof TweetDto)
                tweets.add((TweetDto)iTweet);

        return tweets;
    }

    @Override
    public void onItemDismiss(int position) {
        // swipe to delete failed draft or my own tweet
        ITweet iTweet = getItem(position);
        if(iTweet instanceof PostTweetBean) {
            PostTweetBean postTweetBean = (PostTweetBean) iTweet;
            postTweetBean.delete();

            removeItem(position);
            notifyItemRemoved(position);
        } else if(iTweet instanceof TweetDto) {
            TweetDto tweetDto = (TweetDto) iTweet;
            // request to delete this tweet
            HttpClient.getInstance()
                    .getTwitterService()
                    .detroyTweet(tweetDto.id)
                    .enqueue(new NiceCallback<TweetDto>() {
                        @Override
                        public void onRespond(Response<TweetDto> response) {
                            Snackbar.make(((Activity)mContext).getWindow().getDecorView(),
                                    "delete success", Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailed(Throwable t) {
                            L.d(t.toString());
                            Snackbar.make(((Activity)mContext).getWindow().getDecorView(),
                                    "delete failed", Snackbar.LENGTH_LONG).show();
                        }
                    });

            removeItem(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @SuppressWarnings("unused, unchecked")
    @Subscribe
    public void onEvent(NormalBaseHolder.EventLikeStatus event) {
        for(TweetDto tweetDto : getTweets())
            if(event.getTweetDto().id == tweetDto.id) {
                tweetDto.favorite_count++;
                tweetDto.favorited = true;
                notifyItemChanged(findItemPosition(tweetDto));
            }
    }
}
