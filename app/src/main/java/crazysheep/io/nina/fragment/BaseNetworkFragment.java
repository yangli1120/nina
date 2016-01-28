package crazysheep.io.nina.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import crazysheep.io.nina.R;
import crazysheep.io.nina.net.HttpClient;
import crazysheep.io.nina.net.TwitterService;
import crazysheep.io.nina.utils.Utils;

/**
 * if fragment need network request, use this
 *
 * Created by crazysheep on 16/1/28.
 */
public abstract class BaseNetworkFragment extends BaseFragment implements View.OnClickListener {

    private static final int STATE_IDLE = 0;
    private static final int STATE_ERROR = -1;
    private static final int STATE_LOADING = 1;
    private int mCurState = STATE_IDLE;

    private FrameLayout mContentFl;
    private FrameLayout mLoadingFl;
    private TextView mLoadingMsgTv;
    private ViewStub mErrorVs;
    private TextView mErrorMsgTv;

    protected TwitterService mHttp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHttp = HttpClient.getInstance().create(TwitterService.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_base_network, container, false);
        mContentFl = ButterKnife.findById(rootView, R.id._base_network_ft_content_fl);
        mLoadingFl = ButterKnife.findById(rootView, R.id._base_network_ft_loading_fl);
        mLoadingMsgTv = ButterKnife.findById(rootView, R.id._base_network_ft_loading_msg_tv);
        mErrorVs = ButterKnife.findById(rootView, R.id._base_network_ft_error_stub);
        mErrorMsgTv = ButterKnife.findById(rootView, R.id._base_network_ft_error_msg_tv);

        View contentView = onCreateView(inflater, container);
        if(!Utils.isNull(contentView)) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            mContentFl.addView(contentView, params);
        }

        return rootView;
    }

    @Nullable
    protected abstract View onCreateView(LayoutInflater inflater, ViewGroup container);

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id._base_network_ft_error_msg_tv: {
                onErrorClick();
            }
        }
    }

    protected final void showLoading() {
        showLoading(null);
    }

    protected final void showLoading(String loadingMsg) {
        mCurState = STATE_LOADING;

        mErrorVs.setVisibility(View.GONE);

        mLoadingFl.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(loadingMsg))
            mLoadingMsgTv.setText(loadingMsg);
    }

    protected final void hideLoading() {
        mCurState = STATE_IDLE;

        mLoadingFl.setVisibility(View.GONE);
    }

    protected final void showError() {
        showError(null);
    }

    protected final void showError(String err) {
        mCurState = STATE_ERROR;

        mLoadingFl.setVisibility(View.GONE);

        mErrorVs.setVisibility(View.VISIBLE);
        mErrorMsgTv.setText(!TextUtils.isEmpty(err) ? err : getString(R.string.err_load_failed));
    }

    protected final void hideError() {
        mCurState = STATE_IDLE;

        mErrorVs.setVisibility(View.GONE);
    }

    /* invoke when click error msg TextView */
    protected void onErrorClick() {}

}
