package crazysheep.io.nina.utils;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

/**
 * FloatingActionButton utils
 *
 * Created by crazysheep on 16/3/17.
 */
public class FabUtils {

    /**
     * set FloatingActionButton gone
     * */
    public static void gone(@NonNull FloatingActionButton fab) {
        if(fab.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab
                    .getLayoutParams();
            p.setAnchorId(View.NO_ID);
            fab.setLayoutParams(p);
        }

        fab.setVisibility(View.GONE);
    }

}
