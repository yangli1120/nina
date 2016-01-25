package com.soupkodjou.twitterapp.appuser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Authorization {
    private String consumerKey;
    private String consumerSecret;

    public Authorization(String consumerKey, String consumerSecret){
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    /**
     * This method takes the URL, the method key/value pairs associated with a request, and a request token,
     * then returns the authorization string header needed to issue the request to Twitter.
     * key/value pairs only include actual parameter required by the request, without the ones starting with oauth_
     * used for the authorization. The method will add these parameters.*
     * value for each key is expected to be the raw value, not the encoded one.
     *
     * If the request token is not available, then a null value will be passed*/

    public String getAuthorizationHeader(String url, String method, Map<String, String> actualKeyValuePairs,
                                         String requestToken, String requestTokenSecret, boolean percentEncodeSortedValues){

        //Build the request signature from the Signature class
        Map<String, String> signatureKeyValuePairs = new HashMap<>(); //for holding key/value pairs required for the signature

        Map<String, String> headerKeyValuePairs = new TreeMap<>();//for holding key/value pairs required for the header authorization string

        headerKeyValuePairs.put("oauth_consumer_key", this.consumerKey);
        headerKeyValuePairs.put("oauth_nonce", generateNonce());
        headerKeyValuePairs.put("oauth_signature_method", "HMAC-SHA1");
        headerKeyValuePairs.put("oauth_timestamp", "" + Calendar.getInstance().getTimeInMillis()/1000l);
        if (requestToken != null){
            headerKeyValuePairs.put("oauth_token", requestToken);
        }
        headerKeyValuePairs.put("oauth_version", "1.0");

        //Adding header key/value pairs used for the signature to signatureKeyValuePairs
        signatureKeyValuePairs.putAll(headerKeyValuePairs);

        //Adding actual key/value pairs attached to the request
        signatureKeyValuePairs.putAll(actualKeyValuePairs);

        String signature = new Signature(this.consumerSecret, requestTokenSecret)
                .getSignature(method, url, signatureKeyValuePairs, percentEncodeSortedValues);


        /****Now is the time to create the authorization String**/
        //Adding the request signature to the headerKeyValuePairs Map
        headerKeyValuePairs.put("oauth_signature", signature);

        return generateAuthorizationHeader(headerKeyValuePairs);
    }

    private String generateAuthorizationHeader(Map<String, String> headerKeyValuePairs){
        String authorizationHeader = "OAuth ";

        for (String key: headerKeyValuePairs.keySet()){
            String encodedKey = OAuth.percentEncode(key);

            String encodedValue = OAuth.percentEncode(headerKeyValuePairs.get(key));

            authorizationHeader += encodedKey;
            authorizationHeader += "=";
            authorizationHeader += "\"";
            authorizationHeader += encodedValue;
            authorizationHeader += "\"";
            authorizationHeader += ", ";

        }

        return authorizationHeader.substring(0, authorizationHeader.length() - 2);
    }

    private String generateNonce(){
        String nonce = "";

        //Nonce will contain from 25 to 40 characters
        Random rd = new Random();
        int charCount = rd.nextInt(15) + 25;

        for (int i = 0; i < charCount; i++){
            //Captures any of the alphabet uppercase letter
            Character c = (char)(rd.nextInt(25) + 65);
            nonce += c;
        }

        return nonce;
    }
}