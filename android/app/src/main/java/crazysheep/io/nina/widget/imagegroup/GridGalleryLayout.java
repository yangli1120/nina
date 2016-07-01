package crazysheep.io.nina.widget.imagegroup;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * grid gallery widget for tweet timeline
 *
 * Created by crazysheep on 16/1/28.
 */
public class GridGalleryLayout extends ViewGroup implements View.OnClickListener {

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

    public interface OnChildClickListener {
        void onClick(int position, ImageView view);
    }

    /////////////////////////////////////////////////////////////////////////////////

    private final int DIVIDE_SPACE = InnerUtils.dp2Px(getContext(), 2); // margin 2dp between items

    // contain a ImageView pool for recycler use
    private static List<ImageView> mAllGalleryIvs = new ArrayList<>();
    private static List<Boolean> mUsedMap = new ArrayList<>(); //map child is using or not

    private static final int MIN_COUNT = 1;
    // one tweet max images count is 4
    private static final int MAX_COUNT = 4;

    private OnChildLifeListener mOnLifeListener;
    private OnChildClickListener mOnClickListener;

    private Paint mPaint = new Paint();

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
    @SuppressWarnings("unused")
    public GridGalleryLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        mPaint.setTextSize(InnerUtils.dp2Px(getContext(), 14));
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(InnerUtils.dp2Px(getContext(), 2));
        // add one child by default
        if(!attachReuseChild(0))
            attachNewChild(0);
    }

    public void setOnChildLifeListener(OnChildLifeListener listener) {
        mOnLifeListener = listener;
    }

    public void setOnChildClickListener(OnChildClickListener listener) {
        mOnClickListener = listener;
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
            // notify exist child attach
            for(int pos = 0; pos < count; pos++)
                notifyChildAttached(pos, (ImageView)getChildAt(pos));
            // remove excess child
            for(int position = getChildCount() - 1; position > count - 1; position--) {
                detachChild(position);
            }
            requestLayout();
            invalidate();
        } else if(getChildCount() < count) {
            // notify exist child attach
            for(int pos = 0; pos < getChildCount(); pos++)
                notifyChildAttached(pos, (ImageView)getChildAt(pos));
            // add more child
            for(int position = getChildCount(); position < count; position++) {
                if(!attachReuseChild(position))
                    attachNewChild(position);
            }
            requestLayout();
            invalidate();
        }

        // update child imageview's click listener
        for(int pos = 0; pos < getChildCount(); pos++)
            getChildAt(pos).setOnClickListener(this);
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
            layoutChild(position, 0, 0, r - l, b - t);
    }

    private void layoutChild(int position, int l, int t, int r, int b) {
        int top = t;
        int left = l;
        int right = r;
        int bottom = b;
        if(getChildCount() == 2) {
            right = position == 0 ? l + halfSize(r - l) : r;
            left = position == 0 ? l : r - halfSize(r - l);
        } else if(getChildCount() == 3) {
            right = position == 0 ? l + halfSize(r - l) : r;
            left = position == 0 ? l : r - halfSize(r - l);
            top = position == 2 ? b - halfSize(b - t) : t;
            bottom = position == 1 ? t + halfSize(b - t) : b;
        } else if(getChildCount() == 4) {
            right = position % 2 == 0 ? l + halfSize(r - l) : r;
            left = position % 2 == 0 ? l : r - halfSize(r - l);
            bottom = position < 2 ? t + halfSize(b - t) : b;
            top = position < 2 ? t : b - halfSize(b - t);
        }
        getChildAt(position).layout(left, top, right, bottom);
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
        ImageView child = new ImageView(getContext().getApplicationContext());
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
        if(mOnLifeListener != null)
            mOnLifeListener.onAttach(position, child);
    }

    private void notifyChildDetached(int position, ImageView child) {
        if(mOnLifeListener != null)
            mOnLifeListener.onDetach(position, child);
    }

    @Override
    public void onClick(View v) {
        if(mOnClickListener != null)
            mOnClickListener.onClick(indexOfChild(v), (ImageView) v);
    }

}
