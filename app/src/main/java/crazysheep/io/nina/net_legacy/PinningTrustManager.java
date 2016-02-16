package crazysheep.io.nina.net_legacy;

import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.network.PinningInfoProvider;

/**
 * copy from twitter's fabric sdk "io.fabric.sdk.android.services.network.PinningTrustManager"
 *
 * Created by crazysheep on 16/2/16.
 */
class PinningTrustManager implements X509TrustManager {
    private static final long PIN_FRESHNESS_DURATION_MILLIS = 15552000000L;
    private final TrustManager[] systemTrustManagers;
    private final SystemKeyStore systemKeyStore;
    private final long pinCreationTimeMillis;
    private final List<byte[]> pins = new LinkedList();
    private final Set<X509Certificate> cache = Collections.synchronizedSet(new HashSet());

    public PinningTrustManager(SystemKeyStore keyStore, PinningInfoProvider pinningInfoProvider) {
        this.systemTrustManagers = this.initializeSystemTrustManagers(keyStore);
        this.systemKeyStore = keyStore;
        this.pinCreationTimeMillis = pinningInfoProvider.getPinCreationTimeInMillis();
        String[] arr$ = pinningInfoProvider.getPins();
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            String pin = arr$[i$];
            this.pins.add(this.hexStringToByteArray(pin));
        }

    }

    private TrustManager[] initializeSystemTrustManagers(SystemKeyStore keyStore) {
        try {
            TrustManagerFactory e = TrustManagerFactory.getInstance("X509");
            e.init(keyStore.trustStore);
            return e.getTrustManagers();
        } catch (NoSuchAlgorithmException var3) {
            throw new AssertionError(var3);
        } catch (KeyStoreException var4) {
            throw new AssertionError(var4);
        }
    }

    private boolean isValidPin(X509Certificate certificate) throws CertificateException {
        try {
            MessageDigest nsae = MessageDigest.getInstance("SHA1");
            byte[] spki = certificate.getPublicKey().getEncoded();
            byte[] pin = nsae.digest(spki);
            Iterator i$ = this.pins.iterator();

            byte[] validPin;
            do {
                if (!i$.hasNext()) {
                    return false;
                }

                validPin = (byte[]) i$.next();
            } while (!Arrays.equals(validPin, pin));

            return true;
        } catch (NoSuchAlgorithmException var7) {
            throw new CertificateException(var7);
        }
    }

    private void checkSystemTrust(X509Certificate[] chain, String authType) throws CertificateException {
        TrustManager[] arr$ = this.systemTrustManagers;
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            TrustManager systemTrustManager = arr$[i$];
            ((X509TrustManager) systemTrustManager).checkServerTrusted(chain, authType);
        }

    }

    private void checkPinTrust(X509Certificate[] chain) throws CertificateException {
        if (this.pinCreationTimeMillis != -1L && System.currentTimeMillis() - this.pinCreationTimeMillis > 15552000000L) {
            Fabric.getLogger().w("Fabric", "Certificate pins are stale, (" + (System.currentTimeMillis() - this.pinCreationTimeMillis) + " millis vs " + 15552000000L + " millis) " + "falling back to system trust.");
        } else {
            X509Certificate[] cleanChain = CertificateChainCleaner.getCleanChain(chain, this.systemKeyStore);
            X509Certificate[] arr$ = cleanChain;
            int len$ = cleanChain.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                X509Certificate certificate = arr$[i$];
                if (this.isValidPin(certificate)) {
                    return;
                }
            }

            throw new CertificateException("No valid pins found in chain!");
        }
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new CertificateException("Client certificates not supported!");
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (!this.cache.contains(chain[0])) {
            this.checkSystemTrust(chain, authType);
            this.checkPinTrust(chain);
            this.cache.add(chain[0]);
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        // see{@link https://github.com/square/okhttp/issues/2329}
        return new X509Certificate[] {};
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }
}
