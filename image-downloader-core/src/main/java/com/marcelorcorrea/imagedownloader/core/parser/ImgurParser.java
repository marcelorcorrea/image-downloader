package com.marcelorcorrea.imagedownloader.core.parser;

import com.marcelorcorrea.imagedownloader.core.exception.ImageDownloaderException;
import com.marcelorcorrea.imagedownloader.core.model.ContentType;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImgurParser implements HTMLParser {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.81 Safari/537.36";
    public static final String IMGUR_DOMAIN = "imgur.com";
    private static final String LAYOUT_BLOG_URL = "http://" + IMGUR_DOMAIN + "/a/%s/layout/blog";
    private static final String IMAGE_URL = "http://i.imgur.com/%s.jpg";

    @Override
    public Set<String> parse(String url, ContentType contentType) throws ImageDownloaderException {
        String albumKey = extractAlbumKey(url);
        if (albumKey != null && !albumKey.isEmpty()) {
            url = createURL(albumKey);
        }
        return generateImageURLs(url, contentType);
    }

    private String extractAlbumKey(String url) throws ImageDownloaderException {
        String albumKey = "";
        String regex = "(https?)\\:\\/\\/(www\\.)?(?:m\\.)?imgur\\.com/(a|gallery)/([a-zA-Z0-9]+)(#[0-9]+)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            albumKey = matcher.group(4);
        }
        return albumKey;
    }

    private String createURL(String albumKey) {
        return String.format(LAYOUT_BLOG_URL, albumKey);
    }

    private Set<String> generateImageURLs(String url, ContentType contentType) throws ImageDownloaderException {
        Set<String> imageURLs = new HashSet<>();
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent(USER_AGENT)
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();
            Document document = response.parse();
            Elements elementsByClass = document.getElementsByClass("post-image-container");
            for (Element element : elementsByClass) {
                imageURLs.add(String.format(IMAGE_URL, element.attr("id")));
            }
        } catch (IOException e) {
            throw new ImageDownloaderException(e.getMessage());
        }
        return imageURLs;
    }
}
