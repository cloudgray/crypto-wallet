package com.example.wallet.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.jasypt.util.text.BasicTextEncryptor;

public class CryptoUtils {

    private static final String SHA_256 = "SHA-256";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static String encrypt(String value, String key) throws Exception {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPasswordCharArray(key.toCharArray());
        return textEncryptor.encrypt(value);
    }

    public static String decrypt(String encryptedValue, String key) throws Exception {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPasswordCharArray(key.toCharArray());
        return textEncryptor.decrypt(encryptedValue);
    }

    public static String hash(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(SHA_256);
        byte[] hashedBytes = digest.digest(value.getBytes(UTF_8));

        return bytesToHex(hashedBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}

