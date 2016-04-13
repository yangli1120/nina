package crazysheep.io.nina.widget.text;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

/**
 * handle span clickable in TextView
 * see{@link http://stackoverflow.com/questions/20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o}
 *
 * Created by crazysheep on 16/4/12.
 */
public abstract class TouchableSpan extends ClickableSpan {

    private boolean isPressed = false;

    private int unpressedTextColor;
    private int pressedTextColor;
    private int pressedBackgroundColor;

    public TouchableSpan(int unpressedTextColor, int pressedTextColor, int pressedBackgroundColor) {
        this.unpressedTextColor = unpressedTextColor;
        this.pressedTextColor = pressedTextColor;
        this.pressedBackgroundColor = pressedBackgroundColor;
    }

    public void setPressed(boolean pressed) {
        isPressed = pressed;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);

        ds.setColor(isPressed ? pressedTextColor : unpressedTextColor);
        ds.bgColor = isPressed ? pressedBackgroundColor : Color.TRANSPARENT;
        ds.setUnderlineText(false);
    }
}
