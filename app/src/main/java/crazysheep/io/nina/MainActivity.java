package crazysheep.io.nina;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.utils.L;

public class MainActivity extends BaseActivity {

    @Bind(R.id.login_btn) TwitterLoginButton mLoginBtn;
    @Bind(R.id.username_tv) TextView mUsernameTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mLoginBtn.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                L.d("username: " + result.data.getUserName() + ", userid: " + result.data.getId()
                        + ", authtoken: " + result.data.getAuthToken());

                Toast.makeText(getActivity(), "username: " + result.data.getUserName(),
                        Toast.LENGTH_LONG).show();
                mUsernameTv.setText(result.data.getUserName());
            }

            @Override
            public void failure(TwitterException e) {
                L.d(e.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mLoginBtn.onActivityResult(requestCode, resultCode, data);
    }
}
