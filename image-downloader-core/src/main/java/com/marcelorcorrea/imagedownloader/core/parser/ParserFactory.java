package com.marcelorcorrea.imagedownloader.core.parser;

public abstract class ParserFactory {

    public static HTMLParser getParser(String url) {
        if (url != null) {
            if (url.contains(ImgurParser.IMGUR_DOMAIN)) {
                return new ImgurParser();
            }
        }
        return new JsoupParser();
    }
}
