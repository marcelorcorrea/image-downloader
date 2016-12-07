package com.marcelorcorrea.imagedownloader.core.parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JsoupParser.class, Jsoup.class})
public class JsoupTest {


    private HTMLParser parser = new JsoupParser();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        URL url = PowerMockito.mock(URL.class);
        HttpURLConnection connection = PowerMockito.mock(HttpURLConnection.class);
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] bytes = new byte[]{};

        PowerMockito.whenNew(URL.class).withArguments(Mockito.anyString()).thenReturn(url);
        Mockito.when(url.openConnection()).thenReturn(connection);
        when(connection.getContentType()).thenReturn("text/html", "image/jpg");
        when(connection.getInputStream()).thenReturn(inputStream);
        when(inputStream.read(Mockito.any(byte[].class))).thenReturn(-1);

        Connection conn = Mockito.mock(Connection.class);
        Connection.Response response = Mockito.mock(Connection.Response.class);
        Document document = Mockito.mock(Document.class);


        Element element = Mockito.mock(Element.class);
        Elements elements = new Elements();
        elements.add(element);

        PowerMockito.mockStatic(Jsoup.class);
        Mockito.when(Jsoup.connect(Mockito.anyString())).thenReturn(conn);
        Mockito.when(conn.ignoreContentType(Mockito.anyBoolean())).thenReturn(conn);
        Mockito.when(conn.userAgent((Mockito.anyString()))).thenReturn(conn);
        Mockito.when(conn.timeout((Mockito.anyInt()))).thenReturn(conn);
        Mockito.when(conn.followRedirects(Mockito.anyBoolean())).thenReturn(conn);
        Mockito.when(conn.execute()).thenReturn(response);
        Mockito.when(response.parse()).thenReturn(document);
        Mockito.when(document.select(Mockito.anyString())).thenReturn(elements);
        Mockito.when(element.attr("href")).thenReturn("http://www.marcelorcorrea.com/image.jpg");

        ByteArrayOutputStream byteArrayOutputStream = Mockito.mock(ByteArrayOutputStream.class);
        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
        when(byteArrayOutputStream.toByteArray()).thenReturn(bytes);
    }

    @Test
    public void test(){

    }

}
