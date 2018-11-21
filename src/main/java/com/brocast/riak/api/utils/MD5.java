package com.brocast.riak.api.utils;

import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by sardor on 1/8/14.
 */
public class MD5 {

    public static final String SALT = "Random$SaltValue#Badabum@$@4&#%^$*";

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MD5.class);


    public MD5() {
    }


    public static synchronized String hash(String input) {

        String md5 = null;

        if (null == input) return null;

        try {

            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");

            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());

            //Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);

        } catch (NoSuchAlgorithmException e) {

            log.error("ERROR IN DB API ", e);
        }
        return md5;
    }

    public static byte[] hashBin(String input) {

        byte[] md5 = null;

        if (null == input) return null;

        try {

            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");

            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());
            md5 = digest.digest();

        } catch (NoSuchAlgorithmException e) {

            log.error("ERROR IN DB API ", e);
        }
        return md5;
    }
}
