package crazysheep.io.nina.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * system media store image bean, for gallery, see{@link crazysheep.io.nina.GalleryActivity}
 *
 * Created by crazysheep on 16/2/23.
 */
@ParcelablePlease
public class MediaStoreImageBean implements Parcelable {

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

    public MediaStoreImageBean() {}

    public MediaStoreImageBean(long id, String filepath, String title) {
        this.id = id;
        this.filepath = filepath;
        this.title = title;
    }

    @Override
    public String toString() {
        return id + " - " + filepath;
    }

    ///////////////// parcelable //////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MediaStoreImageBeanParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MediaStoreImageBean> CREATOR = new Creator<MediaStoreImageBean>() {
        public MediaStoreImageBean createFromParcel(Parcel source) {
            MediaStoreImageBean target = new MediaStoreImageBean();
            MediaStoreImageBeanParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public MediaStoreImageBean[] newArray(int size) {
            return new MediaStoreImageBean[size];
        }
    };
}
