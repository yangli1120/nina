package crazysheep.io.nina.widget.imagegroup;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * grid gallery widget for tweet
 *
 * Created by crazysheep on 16/1/28.
 */
public class GridGalleryLayout extends ViewGroup {

    ///////////////////////////// listener for child recycler ///////////////////////

    public interface OnChildLifeListener {
        /**
         * call when child attach to parent
         *
         * @param position The attach child's position in parent
         * @param view The attach child
         * */
        void onAttach(int position, ImageView view);
        /**
         * call when child detach from parent
         *
         * @param position The detach child's position in parent
         * @param view The detach child
         * */
        void onDetach(int position, ImageView view);
    }

    /////////////////////////////////////////////////////////////////////////////////

    private final int DIVIDE_SPACE = InnerUtils.dp2Px(getContext(), 1); // margin 1dp between items

    // contain a ImageView pool for recycler use
    private static List<ImageView> mAllGalleryIvs = new ArrayList<>();
    private static List<Boolean> mUsedMap = new ArrayList<>(); //map child is using or not

    private static final int MIN_COUNT = 1;
    // one tweet max images count is 4
    private static final int MAX_COUNT = 4;

    private OnChildLifeListener mListener;

    public GridGalleryLayout(Context context) {
        super(context);

        init();
    }

    public GridGalleryLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public GridGalleryLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GridGalleryLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        // add one child by default
        if(!attachReuseChild(0))
            attachNewChild(0);
    }

    public void setOnChildLifeListener(OnChildLifeListener listener) {
        mListener = listener;
    }

    /**
     * set current child count
     * */
    public void setItemCount(int count) {
        if(count < MIN_COUNT)
            count = MIN_COUNT;
        if(count > MAX_COUNT)
            count = MAX_COUNT;
        // new items count is same as before, so we can directly use without re-measure and re-layout
        if(getChildCount() == count) {
            for(int position = 0; position < getChildCount(); position++) {
                ImageView child = (ImageView) getChildAt(position);
                mUsedMap.set(mAllGalleryIvs.indexOf(child), true);
                notifyChildAttached(position, (ImageView) getChildAt(position));
            }
        } else if(getChildCount() > count) {
            int removeCount = getChildCount() - count;
            // remove excess child
            for(int position = getChildCount() - 1
                ; getChildCount() - position <= removeCount; position--) {
                detachChild(position);
            }
            requestLayout();
        } else if(getChildCount() < count) {
            // add more child
            for(int position = getChildCount() - 1; position < count; position++) {
                if(!attachReuseChild(position))
                    attachNewChild(position);
            }
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // measure child
        for(int i = 0; i < getChildCount(); i++)
            getChildAt(i).measure(
                    measureChildWidth(i, widthMeasureSpec),
                    measureChildHeight(i, heightMeasureSpec));

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // layout child
        for(int position = 0; position < getChildCount(); position++)
            layoutChild(position, l, t, r, b);
    }

    private void layoutChild(int position, int l, int t, int r, int b) {
        int right = r;
        int bottom = b;
        if(getChildCount() == 2) {
            right = position == 0 ? halfSize(r - l) : r;
        } else if(getChildCount() == 3) {
            right = position == 0 ? halfSize(r - l) : r;
            bottom = position == 1 ? halfSize(b - t) : b;
        } else if(getChildCount() == 4) {
            right = position % 2 == 0 ? halfSize(r - l) : r;
            bottom = position < 2 ? halfSize(b - t) : b;
        }
        getChildAt(position).layout(l, t, right, bottom);
    }

    private int measureChildWidth(int position, int widthMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec); // parent width size

        if(getChildCount() == 1) {
            // do nothing, child width match parent
        } else {
            widthSize = halfSize(widthSize);
        }

        return MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
    }

    private int measureChildHeight(int position, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if(getChildCount() == 1 || getChildCount() == 2
                || (getChildCount() == 3 && position == 0)) {
            // do nothing, child height match parent
        } else {
            heightSize = halfSize(heightSize);
        }

        return MeasureSpec.makeMeasureSpec(heightSize, heightMeasureSpec);
    }

    private int halfSize(int size) {
        return (size - DIVIDE_SPACE) / 2;
    }

    private void attachNewChild(int position) {
        ImageView child = new ImageView(getContext());
        child.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // add to pool and tag it's used
        mAllGalleryIvs.add(child);
        mUsedMap.add(true);
        // add to parent
        addView(child, position, newLayoutParams());
        // invoke listener
        notifyChildAttached(position, child);
    }

    private boolean attachReuseChild(int position) {
        for(int i = 0; i < mAllGalleryIvs.size(); i++)
            if(!mUsedMap.get(i)) {
                ImageView reuseChild = mAllGalleryIvs.get(i);
                mUsedMap.set(i, true);
                addView(reuseChild, position, newLayoutParams());
                notifyChildAttached(position, reuseChild);

                return true;
            }

        return false;
    }

    private void detachChild(int position) {
        ImageView detachChild = (ImageView)getChildAt(position);
        removeView(detachChild);
        int indexInPool = mAllGalleryIvs.indexOf(detachChild);
        mUsedMap.set(indexInPool, false);
        notifyChildDetached(position, detachChild);
    }

    private ViewGroup.LayoutParams newLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    private void notifyChildAttached(int position, ImageView child) {
        if(mListener != null)
            mListener.onAttach(position, child);
    }

    private void notifyChildDetached(int position, ImageView child) {
        if(mListener != null)
            mListener.onDetach(position, child);
    }

}
