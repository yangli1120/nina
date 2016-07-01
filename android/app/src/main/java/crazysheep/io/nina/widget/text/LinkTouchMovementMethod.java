package crazysheep.io.nina.widget.text;

import android.support.v4.view.MotionEventCompat;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * handle link touch in TextView
 * see{@link http://stackoverflow.com/questions/20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o}
 *
 * Created by crazysheep on 16/4/12.
 */
public class LinkTouchMovementMethod extends LinkMovementMethod {

    private static LinkTouchMovementMethod sInstance;
    public static LinkTouchMovementMethod get() {
        if(sInstance == null)
            sInstance = new LinkTouchMovementMethod();

        return sInstance;
    }

    private TouchableSpan mPressedSpan;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                mPressedSpan = getPressedSpan(widget, buffer, event);
                if(mPressedSpan != null) {
                    mPressedSpan.setPressed(true);
                    Selection.setSelection(buffer, buffer.getSpanStart(mPressedSpan),
                            buffer.getSpanEnd(mPressedSpan));
                }
            }break;

            case MotionEvent.ACTION_MOVE: {
                TouchableSpan touchableSpan = getPressedSpan(widget, buffer, event);
                if(mPressedSpan != null && mPressedSpan != touchableSpan) {
                    mPressedSpan.setPressed(false);
                    mPressedSpan = null;
                    Selection.removeSelection(buffer);
                }
            }break;

            case MotionEvent.ACTION_UP: {
                if(mPressedSpan != null) {
                    mPressedSpan.setPressed(false);
                    super.onTouchEvent(widget, buffer, event);
                }
                mPressedSpan = null;
                Selection.removeSelection(buffer);
            }break;
        }

        return true;
    }

    private TouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        TouchableSpan[] link = spannable.getSpans(off, off, TouchableSpan.class);
        TouchableSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }

        return touchedSpan;
    }

}
