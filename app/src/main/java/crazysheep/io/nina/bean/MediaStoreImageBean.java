package crazysheep.io.nina.bean;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

/**
 * system media store image bean, for gallery, see{@link crazysheep.io.nina.GalleryActivity}
 *
 * Created by crazysheep on 16/2/23.
 */
public class MediaStoreImageBean {

    public long id;
    public String filepath;
    public String title;

    public static MediaStoreImageBean parseCursor(@NonNull Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
        String filepath = cursor.getString(
                cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
        String title = cursor.getString(
                cursor.getColumnIndex(MediaStore.Images.ImageColumns.TITLE));

        return new MediaStoreImageBean(id, filepath, title);
    }

    public MediaStoreImageBean(long id, String filepath, String title) {
        this.id = id;
        this.filepath = filepath;
        this.title = title;
    }

}
