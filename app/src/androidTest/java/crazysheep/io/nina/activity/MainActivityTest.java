package crazysheep.io.nina.activity;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import crazysheep.io.nina.MainActivity;
import crazysheep.io.nina.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * UI test for {@link crazysheep.io.nina.MainActivity}
 *
 * Created by crazysheep on 16/6/7.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testDrawerToggle() {
        /*
         * about how to open DrawerLayout,
         * see{@link http://stackoverflow.com/questions/32654554/opendrawer-from-espresso-contrib-is-deprectaded}
         */
        onView(withId(R.id.drawer)).perform(DrawerActions.open());

        /*
         *  how to test NavigationView,
         *  see{@link http://qiita.com/operandoOS/items/05f650e7b7fe7e5189c2}
         */
        onView(withId(R.id.nav_layout))
                .perform(NavigationViewActions.navigateTo(R.id.nav_night_theme));

        onView(withId(R.id.nav_layout))
                .perform(NavigationViewActions.navigateTo(R.id.nav_scalpel_debug));

        onView(withId(R.id.drawer)).perform(DrawerActions.close());
    }

}
