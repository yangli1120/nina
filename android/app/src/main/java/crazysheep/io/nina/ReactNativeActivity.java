package crazysheep.io.nina;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.util.Arrays;
import java.util.List;

/**
 * base react native activity
 *
 * Created by crazysheep on 16/3/16.
 */
public class ReactNativeActivity extends ReactActivity {

    @Override
    protected String getMainComponentName() {
        return "Nina";
    }

    @Override
    protected boolean getUseDeveloperSupport() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(new MainReactPackage());
    }
}
