package crazysheep.io.nina.net_legacy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * copy from twitter's fabric sdk "io.fabric.sdk.android.services.network.SystemKeyStore"
 *
 * Created by crazysheep on 16/2/16.
 */
class SystemKeyStore {
    final KeyStore trustStore;
    private final HashMap<Principal, X509Certificate> trustRoots;

    public SystemKeyStore(InputStream keystoreStream, String passwd) {
        KeyStore trustStore = this.getTrustStore(keystoreStream, passwd);
        this.trustRoots = this.initializeTrustedRoots(trustStore);
        this.trustStore = trustStore;
    }

    public boolean isTrustRoot(X509Certificate certificate) {
        X509Certificate trustRoot = (X509Certificate)this.trustRoots.get(certificate.getSubjectX500Principal());
        return trustRoot != null && trustRoot.getPublicKey().equals(certificate.getPublicKey());
    }

    public X509Certificate getTrustRootFor(X509Certificate certificate) {
        X509Certificate trustRoot = (X509Certificate)this.trustRoots.get(certificate.getIssuerX500Principal());
        if(trustRoot == null) {
            return null;
        } else if(trustRoot.getSubjectX500Principal().equals(certificate.getSubjectX500Principal())) {
            return null;
        } else {
            try {
                certificate.verify(trustRoot.getPublicKey());
                return trustRoot;
            } catch (GeneralSecurityException var4) {
                return null;
            }
        }
    }

    private HashMap<Principal, X509Certificate> initializeTrustedRoots(KeyStore trustStore) {
        try {
            HashMap e = new HashMap();
            Enumeration aliases = trustStore.aliases();

            while(aliases.hasMoreElements()) {
                String alias = (String)aliases.nextElement();
                X509Certificate cert = (X509Certificate)trustStore.getCertificate(alias);
                if(cert != null) {
                    e.put(cert.getSubjectX500Principal(), cert);
                }
            }

            return e;
        } catch (KeyStoreException var6) {
            throw new AssertionError(var6);
        }
    }

    private KeyStore getTrustStore(InputStream keystoreStream, String passwd) {
        try {
            KeyStore e = KeyStore.getInstance("BKS");
            BufferedInputStream bin = new BufferedInputStream(keystoreStream);

            try {
                e.load(bin, passwd.toCharArray());
            } finally {
                bin.close();
            }

            return e;
        } catch (KeyStoreException var12) {
            throw new AssertionError(var12);
        } catch (NoSuchAlgorithmException var13) {
            throw new AssertionError(var13);
        } catch (CertificateException var14) {
            throw new AssertionError(var14);
        } catch (IOException var15) {
            throw new AssertionError(var15);
        }
    }
}
