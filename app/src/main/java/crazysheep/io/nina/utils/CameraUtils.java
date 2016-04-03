package crazysheep.io.nina.utils;

import android.hardware.Camera;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * camera utils
 *
 * Created by crazysheep on 16/2/28.
 */
public class CameraUtils {

    public final static int DEFAULT_VIDEO_SIZE = 1280;

    /**
     * create image file to save camera's new photo
     * */
    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "nina_" + timeStamp + "_";
        File imageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", imageDir);
    }

    /**
     * camera api1, choose video size
     * */
    public static Camera.Size chooseVideoSize(List<Camera.Size> sizes) {
        if(Utils.size(sizes) <= 0)
            return null;

        StringBuilder sb = new StringBuilder();
        for(Camera.Size size : sizes)
            sb.append(size.width + "*" + size.height + ", ");
        DebugHelper.log("CameraUtils.chooseVideoSize(), all sizes: [" + sb.toString() + "]");
        for (Camera.Size size : sizes) {
            if (size.width == size.height * 4 / 3
                    && size.width <= DEFAULT_VIDEO_SIZE) {
                return size;
            }
        }

        return sizes.get(sizes.size() - 1);
    }

}
