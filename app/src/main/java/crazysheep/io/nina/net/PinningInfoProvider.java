package crazysheep.io.nina.net;

import java.io.InputStream;

/**
 * copy from twitter's fabric sdk "io.fabric.sdk.android.services.network.PinningInfoProvider"
 *
 * Created by crazysheep on 16/2/16.
 */
interface PinningInfoProvider {
    long PIN_CREATION_TIME_UNDEFINED = -1L;

    InputStream getKeyStoreStream();

    String getKeyStorePassword();

    String[] getPins();

    long getPinCreationTimeInMillis();
}
