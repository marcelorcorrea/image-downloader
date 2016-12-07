package com.marcelorcorrea.imagedownloader.swing.cache;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by marcelo on 08/09/15.
 */
public class ImageCache {

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    public static void cache(String key, BufferedImage bufferedImage) {
        cache.put(key, bufferedImage);
    }

    public static BufferedImage retrieve(String key) {
        return cache.get(key);
    }

    public static void clear() {
        cache.clear();
    }

}
