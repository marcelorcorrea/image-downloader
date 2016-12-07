package com.marcelorcorrea.imagedownloader.core.util;

import java.util.Random;

/**
 * Created by marcelo on 10/3/16.
 */
public class Util {

    public static String getFilename(String path, String extension) {
        //remove extra slash(es) that might have at the end of the path
        path = path.replaceAll("/+$", "");
        int index = (path.lastIndexOf("/")) + 1;
        int extensionIndex = (path.lastIndexOf(".") + 4);
        if (index > extensionIndex) {
            return path.substring(index) + "." + extension;
        }
        return path.substring(index, extensionIndex);
    }

    public static String randomString(String extension) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rand = new Random();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            buf.append(chars.charAt(rand.nextInt(chars.length())));
        }
        buf.append(".").append(extension);
        return buf.toString();
    }
}
