package crazysheep.io.nina.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * string utils
 *
 * Created by crazysheep on 16/2/3.
 */
public class StringUtils {

    /**
     * format count, such like format 1100 as 1.1k, but if count less than 1000, return itself
     * */
    public static String formatCount(int count) {
        if(count >= 1000)
            return String.format("%.1fK", count * 1f / 1000);
        else
            return String.valueOf(count);
    }

    /**
     * url encode
     * */
    public static String urlEncode(String s) {
        if(!TextUtils.isEmpty(s))
            try {
                return URLEncoder.encode(s, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        return s;
    }

    /**
     * set string spannable
     * */
    public static String span(String s, int color) {
        if(!TextUtils.isEmpty(s)) {
            SpannableString ss = new SpannableString(s);
            ss.setSpan(new ForegroundColorSpan(color), 0, s.length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            return ss.toString();
        }

        return s;
    }

}
