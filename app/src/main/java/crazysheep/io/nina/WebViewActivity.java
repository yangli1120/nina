package crazysheep.io.nina;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.nina.chrome.customtabs.CustomTabsHelper;
import crazysheep.io.nina.chrome.customtabs.ServiceConnection;
import crazysheep.io.nina.chrome.customtabs.ServiceConnectionCallback;
import crazysheep.io.nina.chrome.customtabs.SessionHelper;
import crazysheep.io.nina.constants.BundleConstants;
import crazysheep.io.nina.utils.Utils;

/**
 * open web url activity, use chrome custom tabs if could, otherwise use local WebView
 *
 * Created by crazysheep on 16/4/12.
 */
public class WebViewActivity extends BaseActivity implements ServiceConnectionCallback {

    @Bind(R.id.web_stub) ViewStub mWebStub;
    private Toolbar mToolbar;
    private ViewGroup mContainer;
    private ProgressBar mLoadPb;
    private WebView mWebView;

    private boolean isAllowedToUseCustomTabs = false;

    private CustomTabsClient mClient;
    private CustomTabsServiceConnection mServiceConnection;
    private CustomTabsSession mCustomSession;

    private String openUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);

        openUrl = getIntent().getStringExtra(BundleConstants.EXTRA_OPEN_WEB_URL);
        if(TextUtils.isEmpty(openUrl))
            finish();

        isAllowedToUseCustomTabs = !TextUtils.isEmpty(CustomTabsHelper.getPackageNameToUse(this));
        if(isAllowedToUseCustomTabs) {
            // bind chrome service at onStart()
        } else if(!Utils.isNull(mWebStub)) {
            // can not bind to chrome service, use local shit WebView
            mWebStub.inflate();
            mContainer = ButterKnife.findById(this, R.id.root);
            mToolbar = ButterKnife.findById(this, R.id.toolbar);
            setSupportActionBar(mToolbar);
            // init actionbar
            if(!Utils.isNull(getSupportActionBar())) {
                getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
            }
            mLoadPb = ButterKnife.findById(this, R.id.load_pb);
            mLoadPb.setBackgroundColor(Color.WHITE);
            mLoadPb.setPadding(0, 0, 0, 0);
            mLoadPb.setMax(100);
            // init webview
            mWebView = ButterKnife.findById(this, R.id.open_url_wb);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if(!Utils.isNull(getSupportActionBar())) {
                        getSupportActionBar().setTitle(view.getTitle());
                    }
                }
            });
            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    mLoadPb.setProgress(newProgress);
                    mLoadPb.setSecondaryProgress(newProgress);
                }
            });
            mWebView.loadUrl(openUrl);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(isAllowedToUseCustomTabs) {
            // bind to chrome service
            mServiceConnection = new ServiceConnection(this);
            boolean connectSuccess = CustomTabsClient.bindCustomTabsService(this,
                    CustomTabsHelper.getPackageNameToUse(this), mServiceConnection);
            if(!connectSuccess)
                mServiceConnection = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!Utils.isNull(mServiceConnection)) {
            unbindService(mServiceConnection);
            mClient = null;
            mCustomSession = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // about destroy WebView,
        // see{@link http://stackoverflow.com/questions/17418503/destroy-webview-in-android}
        if(!Utils.isNull(mContainer)) {
            mContainer.removeAllViews();

            if(!Utils.isNull(mWebView)) {
                mWebView.clearHistory();
                mWebView.clearCache(true);
                mWebView.loadUrl("about:blank");
                mWebView.pauseTimers();
                mWebView = null;
            }
        }
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        mClient = client;
        mClient.warmup(0); // speed up load url

        // use chrome custom tab load url
        mCustomSession = mClient.newSession(new CustomTabsCallback());
        mCustomSession.mayLaunchUrl(Uri.parse(openUrl), null, null); // speed up load url
        SessionHelper.setCurrentSession(mCustomSession);

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(mCustomSession);
        builder.setShowTitle(true);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        builder.setToolbarColor(typedValue.data);
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        builder.setSecondaryToolbarColor(typedValue.data);
        builder.setStartAnimations(this, R.anim.slide_left_in, R.anim.slide_right_out);

        CustomTabsIntent customTabsIntent = builder.build();
        CustomTabsHelper.addKeepAliveExtra(this, customTabsIntent.intent);
        customTabsIntent.launchUrl(this, Uri.parse(openUrl));

        finish();
    }

    @Override
    public void onServiceDisconnected() {
        mClient = null;
    }
}
