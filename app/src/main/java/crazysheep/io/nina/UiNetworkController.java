package crazysheep.io.nina;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

/**
 * control ui network state, "Yes I am commander"
 *
 * Created by crazysheep on 16/1/30.
 */
public class UiNetworkController {

    //////////////////////////// builder //////////////////////////////
    public static class Builder {

        private View mErrorView;
        private TextView mErrorMsgTv;
        private View mLoadingView;
        private TextView mLoadingMsgTv;

        private View mContentView;

        public Builder() {}

        public Builder setErrorView(@NonNull View errorView) {
            mErrorView = errorView;
            return this;
        }

        public Builder setLoadingView(@NonNull View loadingView) {
            mLoadingView = loadingView;
            return this;
        }

        public Builder setErrorMessageView(@NonNull TextView errorTv) {
            mErrorMsgTv = errorTv;
            return this;
        }

        public Builder setLoadingMessageView(@NonNull TextView loadingTv) {
            mLoadingMsgTv = loadingTv;
            return this;
        }

        public Builder setContentView(@NonNull View contentView) {
            mContentView = contentView;
            return this;
        }

        public UiNetworkController build() {
            return new UiNetworkController(mErrorView, mErrorMsgTv, mLoadingView, mLoadingMsgTv,
                    mContentView);
        }

    }
    /////////////////////////////////////////////////////////////////////

    private static final int STATE_ERROR = -1;
    private static final int STATE_NORMAL = 0;
    private static final int STATE_LOADING = 1;

    private View mErrorView;
    private TextView mErrorTv;
    private View mLoadingView;
    private TextView mLoadingTv;
    private View mContentView;

    private int mState = STATE_NORMAL;

    private UiNetworkController(@NonNull View error, @NonNull TextView errorTv,
                                @NonNull View loading, @NonNull TextView loadingTv,
                                @NonNull View content) {
        mErrorView = error;
        mErrorTv = errorTv;
        mLoadingView = loading;
        mLoadingTv = loadingTv;
        mContentView = content;
    }

    private void setState(int state) {
        mState = state;
    }

    public void showError(String errorMsg) {
        setState(STATE_ERROR);

        mContentView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);

        mErrorTv.setText(errorMsg);
        mErrorView.setVisibility(View.VISIBLE);
    }

    public void showLoading(String loadingMsg) {
        setState(STATE_LOADING);

        mContentView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);

        mLoadingTv.setText(loadingMsg);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    public void showContent() {
        setState(STATE_NORMAL);

        mContentView.setVisibility(View.VISIBLE);

        mErrorView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
    }

}
