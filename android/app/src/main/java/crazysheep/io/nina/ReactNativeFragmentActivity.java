package crazysheep.io.nina;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import crazysheep.io.nina.fragment.ReactNativeFragment;

/**
 * host activity for {@link crazysheep.io.nina.fragment.ReactNativeFragment}
 *
 * Created by crazysheep on 16/7/5.
 */
public class ReactNativeFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_react_native_fragment);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_ft, new ReactNativeFragment(),
                        ReactNativeFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }
}
