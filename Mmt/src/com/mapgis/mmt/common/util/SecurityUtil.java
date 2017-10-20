package com.mapgis.mmt.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Comclay on 2017/3/16.
 * MD5加密
 */

public class SecurityUtil {
    /**
     * MD5加密字符串
     *
     * @param text 加密前的字符串
     * @return 加密后的字符串
     */
    public static String encrypt(String text) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("MD5");
        sha.update(text.getBytes());
        byte[] digest = sha.digest();
        return new String(digest);
    }
}
