package crazysheep.io.nina.utils;

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
}
