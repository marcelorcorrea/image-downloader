package com.marcelorcorrea.imagedownloader.core.parser;

public abstract class ParserFactory {

    private static ImgurParser IMGUR_PARSER_INSTANCE = new ImgurParser();
    private static JsoupParser JSOUP_PARSER_INSTANCE = new JsoupParser();

    public static HTMLParser getParser(String url) {
        if (url != null && !url.isEmpty()) {
            if (url.contains(ImgurParser.IMGUR_DOMAIN)) {
                return IMGUR_PARSER_INSTANCE;
            }
        }
        return JSOUP_PARSER_INSTANCE;
    }
}
