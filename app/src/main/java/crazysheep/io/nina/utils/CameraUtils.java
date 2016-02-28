package crazysheep.io.nina.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * camera utils
 *
 * Created by crazysheep on 16/2/28.
 */
public class CameraUtils {

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

}
