package edu.upenn.cis.cis455.utils;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5HashGenerator {


    public static String getHash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
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
