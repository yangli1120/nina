package crazysheep.io.nina.utils;

import android.annotation.TargetApi;
import android.util.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import crazysheep.io.nina.compat.APICompat;

/**
 * camera utils for new Camera2 api
 *
 * Created by crazysheep on 16/3/17.
 */
@TargetApi(APICompat.L)
public class Camera2Utils {

    public final static int DEFAULT_VIDEO_SIZE = 1280;

    /**
     * Compares two {@code Size}s based on their areas.
     */
    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * choose video size
     * */
    public static Size chooseVideoSize(Size[] choices) {
        StringBuilder sb = new StringBuilder();
        for(Size size : choices)
            sb.append(size + ", ");
        DebugHelper.log("Camera2Utils.chooseVideoSize(), all sizes: [" + sb.toString() + "]");
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3
                    && size.getWidth() <= DEFAULT_VIDEO_SIZE) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }

    /**
     * choose preview size base on video size
     * */
    public static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        StringBuilder sb = new StringBuilder();
        for(Size size : choices)
            sb.append(size + ", ");
        DebugHelper.log("Camera2Utils.chooseOptimalSize(), all sizes: [" + sb.toString()
                + "], aspectWidth: " + width + ", aspectHeight: " + height);
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

}
