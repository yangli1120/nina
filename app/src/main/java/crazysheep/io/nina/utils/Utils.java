package crazysheep.io.nina.utils;

import java.util.UUID;

/**
 * common utils
 *
 * Created by crazysheep on 16/1/22.
 */
public class Utils {

    public static boolean isNull(Object obj) {
        return null == obj;
    }

    public static String randomId() {
        return UUID.randomUUID().toString();
    }
}
