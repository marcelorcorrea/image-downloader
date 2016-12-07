package com.marcelorcorrea.imagedownloader.core.parser;

import com.marcelorcorrea.imagedownloader.core.exception.ImageDownloaderException;
import com.marcelorcorrea.imagedownloader.core.model.ContentType;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcelo Correa
 */
public class JsoupParser implements HTMLParser {

    private static final Logger logger = LoggerFactory.getLogger(JsoupParser.class);
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.81 Safari/537.36";

    @Override
    public Set<String> parse(String url, ContentType contentType) throws ImageDownloaderException {

        try {
            String query = "img[src*=." + contentType.getRawValue() + "],"
                    + "img[data-src*=." + contentType.getRawValue() + "],"
                    + "a[href*=." + contentType.getRawValue() + "],"
                    + "link[rel=image_src][href*=." + contentType.getRawValue() + "]";
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent(USER_AGENT)
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();
            Document document = response.parse();
            logger.info("Searching for useful images...");
            Elements elements = document.select(query);

            if (elements.size() > 0) {
                logger.info("Images found! Looking for its sources...");
            } else {
                logger.info("No Images found, checking if there is an iframe tag...");
                Element iframe = document.select("iframe").first();
                if (iframe != null && !iframe.absUrl("src").isEmpty()) {
                    logger.info("iframe found, connecting and searching for useful images...");
                    return parse(iframe.absUrl("src"), contentType);
                }
            }
            return extractImageSourceFromElements(elements);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            throw new ImageDownloaderException(ex);
        }
    }

    /**
     * Extracts from an element the source of the image, since the elements list can have more than one type.
     *
     * @param elements List of Elements to be iterated
     * @return List of image source from the elements.
     */
    private Set<String> extractImageSourceFromElements(Elements elements) {
        Set<String> imgs = new HashSet<>();
        String attributes[] = {"src", "href", "data-src"};

        for (Element element : elements) {
            for (String attribute : attributes) {
                if (!element.absUrl(attribute).isEmpty()) {
                    imgs.add(element.absUrl(attribute));
                    break;
                }
            }
        }
        return imgs;
    }
}
