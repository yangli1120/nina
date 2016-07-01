package crazysheep.io.nina.db;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import crazysheep.io.nina.bean.MediaStoreImageBean;
import crazysheep.io.nina.utils.Utils;

/**
 * helper for system media store
 *
 * Created by crazysheep on 16/2/23.
 */
public class MediaStoreHelper {

    public static final String[] IMAGES_PROJECTION = new String[] {
            Images.ImageColumns._ID,
            Images.ImageColumns.TITLE,
            Images.ImageColumns.DATA,
    };

    /**
     * query system media store, get all images, sort by modified time desc
     * */
    public static List<MediaStoreImageBean> getAllImages(@NonNull ContentResolver resolver) {
        List<MediaStoreImageBean> images = new ArrayList<>();

        Cursor cursor = resolver.query(Images.Media.EXTERNAL_CONTENT_URI,
                IMAGES_PROJECTION, null, null, Images.ImageColumns.DATE_MODIFIED + " DESC");
        if(!Utils.isNull(cursor) && cursor.getCount() > 0) {
            cursor.moveToPosition(-1);
            while(cursor.moveToNext())
                images.add(MediaStoreImageBean.parseCursor(cursor));

            cursor.close();
        }

        return images;
    }
}
