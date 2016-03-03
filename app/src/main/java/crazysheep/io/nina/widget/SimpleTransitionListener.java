package crazysheep.io.nina.widget;

import android.annotation.TargetApi;
import android.transition.Transition;

import crazysheep.io.nina.compat.APICompat;

/**
 * simple transition listener implements {@link android.transition.Transition.TransitionListener}
 *
 * Created by crazysheep on 16/3/3.
 */
@TargetApi(APICompat.J16)
public class SimpleTransitionListener implements Transition.TransitionListener {

    @Override
    public void onTransitionCancel(Transition transition) {
    }

    @Override
    public void onTransitionStart(Transition transition) {
    }

    @Override
    public void onTransitionEnd(Transition transition) {
    }

    @Override
    public void onTransitionPause(Transition transition) {
    }

    @Override
    public void onTransitionResume(Transition transition) {
    }
}
