package crazysheep.io.nina;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.RxTwitterService;
import crazysheep.io.nina.net.TwitterService;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * base activity
 *
 * Created by crazysheep on 16/1/20.
 */
public class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    //////////////////////////// api ////////////////////////////////

    /**
     * if current activity need request network service, implement this interface,
     * then BaseActivity will init TwitterService instance
     * */
    public interface ITwitterServiceActivity {}

    /////////////////////////////////////////////////////////////////

    public static String TAG = BaseActivity.class.getSimpleName();

    protected TwitterService mTwitter;
    protected RxTwitterService mRxTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = this.getClass().getSimpleName();

        if(this instanceof ITwitterServiceActivity) {
            mTwitter = HttpClient.getInstance().getTwitterService();
            mRxTwitter = HttpClient.getInstance().getRxTwitterService();
        }
    }

    protected final Activity getActivity() {
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
    }

}
