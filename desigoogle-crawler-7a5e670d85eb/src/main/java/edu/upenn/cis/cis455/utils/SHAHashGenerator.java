package edu.upenn.cis.cis455.utils;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAHashGenerator {

    public static String getHash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(content.getBytes());
            byte[] digest = md.digest();
            String contentHash = DatatypeConverter
                    .printHexBinary(digest).toUpperCase();
            return contentHash;
        } catch (NoSuchAlgorithmException e) {
            return content;
        }
    }
}
