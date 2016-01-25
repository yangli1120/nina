package com.soupkodjou.twitterapp.appuser;

import android.util.Base64;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Signature {

    private String consumerSecret;
    private String oauthTokenSecret;

    public Signature(String consumerSecret, String oauthTokenSecret){
        this.consumerSecret = consumerSecret;
        this.oauthTokenSecret = oauthTokenSecret;
    }

    public String getSignature(String method, String baseURL, Map<String, String> keyPairs, boolean percentEncodeSortedValues){

        String keyPairString = this.generateKeyValuePairString(keyPairs, percentEncodeSortedValues);

        String baseString = this.getBaseString(method, baseURL, keyPairString);

        String signingKey = this.getSignatureKey(this.consumerSecret, this.oauthTokenSecret);

        String signature = this.hmacSha1(baseString, signingKey);

        return signature;
    }

    private String getBaseString(String method, String baseURL, String keyPairString){
        return method.toUpperCase() + "&" + OAuth.percentEncode(baseURL) + "&" + OAuth.percentEncode(keyPairString);
    }

    private String getSignatureKey(String consumerSecret, String oauthTokenSecret){
        if (oauthTokenSecret != null){
            return OAuth.percentEncode(consumerSecret) + "&" + OAuth.percentEncode(oauthTokenSecret);
        }else{
            return OAuth.percentEncode(consumerSecret) + "&";
        }
    }

    private String generateKeyValuePairString(Map<String, String> keyValuePairs, boolean percentEncodeSortedValues){
        String keyValuePairString = "";

        SortedMap<String, String> sortedEncodedKeyPairs = new TreeMap<>();
        for (String key : keyValuePairs.keySet()) {
            String encodedKey = OAuth.percentEncode(key);
            String encodedValue = OAuth.percentEncode(keyValuePairs.get(key));
            sortedEncodedKeyPairs.put(encodedKey, encodedValue);
        }

        int i = 0;
        for (String encodedKey : sortedEncodedKeyPairs.keySet()) {
            String value = null;
            if (percentEncodeSortedValues){
                value = OAuth.percentEncode(sortedEncodedKeyPairs.get(encodedKey));
            }else{
                value = sortedEncodedKeyPairs.get(encodedKey);
            }
            keyValuePairString += encodedKey + "=" + value;
            if (++i < sortedEncodedKeyPairs.keySet().size()){
                keyValuePairString += "&";
            }
        }

        return keyValuePairString;
    }

    private String hmacSha1(String baseString, String key){
        String result = null;
        final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(baseString.getBytes());
            byte[] encodedBytes = Base64.encode(rawHmac, Base64.URL_SAFE);
            result = new String(encodedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}