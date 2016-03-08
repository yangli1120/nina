package crazysheep.io.nina.prefs;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * settings prefs
 *
 * Created by crazysheep on 16/3/8.
 */
public class SettingPrefs extends BasePrefs {

    public static final String PREFS_NAME = "nina.setting";

    private static final int THEME_DAY = 0;
    private static final int THEME_NIGHT = 1;

    public static final String KEY_THEME = "key_theme";

    public SettingPrefs(@NonNull Context context) {
        super(context);
    }

    public void switchDayTheme() {
        setInt(KEY_THEME, THEME_DAY);
    }

    public void switchNightTheme() {
        setInt(KEY_THEME, THEME_NIGHT);
    }

    public boolean isDayTheme() {
        return getInt(KEY_THEME, THEME_DAY) == THEME_DAY;
    }

    public boolean isNightTheme() {
        return getInt(KEY_THEME, THEME_DAY) == THEME_NIGHT;
    }

}
