package crazysheep.io.nina.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * bitmap utils, such like compress image file
 *
 * Created by crazysheep on 16/2/25.
 */
public class BitmapUtils {

    /**
     * default width decode bitmap from a image file
     * */
    public static final int MAX_IMAGE_WIDTH = 2048;

    /**
     * decode bitmap from image file, max width is 2048, yeah, it is large enough for social network
     * */
    public static Bitmap decodeFile2048(@NonNull String filepath) throws IOException {
        return decodeFile(new File(filepath), MAX_IMAGE_WIDTH);
    }

    /**
     * Decodes image and scales it to reduce memory consumption
     * </p>
     * see{@link http://stackoverflow.com/questions/477572/strange-out-of-memory-issue-while-loading-an-image-to-a-bitmap-object/823966#823966}
     */
    private static Bitmap decodeFile(@NonNull File file, int targetWidth) throws IOException {
        Bitmap bitmap;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream(file);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        int scale = 1;
        if (o.outHeight > targetWidth || o.outWidth > targetWidth) {
            scale = (int)Math.pow(2, (int) Math.ceil(Math.log(targetWidth /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(file);
        bitmap = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();

        return bitmap;
    }

}
