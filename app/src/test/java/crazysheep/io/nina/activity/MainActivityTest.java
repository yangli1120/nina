package crazysheep.io.nina.activity;

import android.support.design.widget.FloatingActionButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.BuildConfig;
import crazysheep.io.nina.MainActivity;
import crazysheep.io.nina.R;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;

/**
 * test {@link crazysheep.io.nina.MainActivity}
 *
 * Created by crazysheep on 16/3/27.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    MainActivity mActivity;
    @Bind(R.id.action_fab) FloatingActionButton mFab;

    @Before
    public void setup() {
        mActivity = Robolectric.setupActivity(MainActivity.class);
        ButterKnife.bind(mActivity);
    }

    @Test
    public void clickFabStartPostTweetActivity() {
        mFab.performClick();

        ShadowApplication app = shadowOf(RuntimeEnvironment.application);
        assertThat("PostTweetActivity has started", app.getNextStartedActivity(),
                is(notNullValue()));
    }

}
