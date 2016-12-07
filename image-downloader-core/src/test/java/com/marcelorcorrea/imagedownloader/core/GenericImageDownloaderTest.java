package com.marcelorcorrea.imagedownloader.core;

import com.google.common.io.Files;
import com.marcelorcorrea.imagedownloader.core.exception.ImageDownloaderException;
import com.marcelorcorrea.imagedownloader.core.exception.UnknownContentTypeException;
import com.marcelorcorrea.imagedownloader.core.fetcher.ImageFetcher;
import com.marcelorcorrea.imagedownloader.core.http.DownloadManager;
import com.marcelorcorrea.imagedownloader.core.imp.GenericImageDownloader;
import com.marcelorcorrea.imagedownloader.core.listener.ImageDownloaderListener;
import com.marcelorcorrea.imagedownloader.core.model.ContentType;
import com.marcelorcorrea.imagedownloader.core.model.DownloadedImage;
import com.marcelorcorrea.imagedownloader.core.parser.HTMLParser;
import org.junit.Assert;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest({GenericImageDownloader.class, Files.class, DownloadManager.class})
public class GenericImageDownloaderTest {

    private HTMLParser parser;
    private HttpURLConnection connection;
    private URL url;
    private DummyListener dummyListener;
    private ImageDownloader imageDownloader;
    private ImageFetcher imageFetcher;
    ImageFetcher.ImageFetcherCallback imageFetcherCallback;
    private byte[] bytes;

    @Rule
    public final ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        //Image downloader class
        imageDownloader = new GenericImageDownloader();

        //It's listeners and parsers
        dummyListener = new DummyListener();
        imageFetcher = Mockito.mock(ImageFetcher.class);
        imageFetcherCallback = Mockito.mock(ImageFetcher.ImageFetcherCallback.class);
        parser = Mockito.mock(HTMLParser.class);

        // Setting listeners and parsers
        imageDownloader.setOnDownloadListener(dummyListener);
        imageDownloader.setImageFetcher(imageFetcher);
        imageDownloader.setHTMLParser(parser);

        //mocks
        url = PowerMockito.mock(URL.class);
        bytes = new byte[]{15};
        when(imageFetcher.downloadImage(Mockito.anyString())).thenReturn(bytes);

//        connection = PowerMockito.mock(HttpURLConnection.class);
//        InputStream inputStream = Mockito.mock(InputStream.class);
//        when(inputStream.read(Mockito.any(byte[].class))).thenReturn(-1);
//
//        PowerMockito.whenNew(URL.class).withArguments(Mockito.anyString()).thenReturn(url);
//        Mockito.when(url.openConnection()).thenReturn(connection);
//        when(connection.getContentType()).thenReturn("text/html", "image/jpg");
//        when(connection.getInputStream()).thenReturn(inputStream);
//        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

//        ByteArrayOutputStream byteArrayOutputStream = Mockito.mock(ByteArrayOutputStream.class);
//        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
//        byte[] bytes = new byte[]{};
//        when(byteArrayOutputStream.toByteArray()).thenReturn(bytes);
    }

    @Test
    public void testSingleJPGImage() throws Exception {
        final String path = "http://www.marcelorcorrea.com/image.jpg";
        ImageDownloaderListener imageDownloaderListener = new ImageDownloaderListener() {
            int counter = 5;

            @Override
            public void onTaskStart(int numberOfImages) {
                Assert.assertEquals(1, numberOfImages);
            }

            @Override
            public void onDownloadStart(String url, int contentLength) {
                Assert.assertEquals(path, url);
                Assert.assertEquals(contentLength, 15);
            }

            @Override
            public void onDownloadInProgress(String url, int progress) {
                Assert.assertEquals(path, url);
                Assert.assertEquals(counter, progress);
                counter += 5;
            }

            @Override
            public void onDownloadComplete(String url, DownloadedImage downloadedImage) {
                Assert.assertEquals(path, url);
                Assert.assertEquals(bytes, downloadedImage.getBytes());
                Assert.assertEquals("image.jpg", downloadedImage.getName());
                Assert.assertEquals("jpg", downloadedImage.getExtension());
            }

            @Override
            public void onDownloadFail(String imgSource) {
            }

            @Override
            public void onTaskFinished() {
            }
        };
        DownloadManager manager = Mockito.spy(DownloadManager.class);
        Mockito.doReturn(ContentType.JPG).when(manager).getContentType(Mockito.anyString());

        connection = PowerMockito.mock(HttpURLConnection.class);
        InputStream inputStream = Mockito.mock(InputStream.class);
        when(inputStream.read(Mockito.any(byte[].class))).thenReturn(5).thenReturn(5).thenReturn(5).thenReturn(-1);

        PowerMockito.whenNew(URL.class).withArguments(Mockito.anyString()).thenReturn(url);
        Mockito.when(url.openConnection()).thenReturn(connection);
        when(connection.getContentLength()).thenReturn(15);
        when(connection.getInputStream()).thenReturn(inputStream);
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        ByteArrayOutputStream byteArrayOutputStream = Mockito.mock(ByteArrayOutputStream.class);
        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
        when(byteArrayOutputStream.toByteArray()).thenReturn(bytes);

        imageDownloader.setImageFetcher(manager);
        manager.setListener((ImageFetcher.ImageFetcherCallback) imageDownloader);
        imageDownloader.setOnDownloadListener(imageDownloaderListener);
        imageDownloader.download(path, ContentType.JPG);
    }

    @Test
    public void testMultiImagesFromHTMLWithSelectedContentTypeJPG() throws Exception {
        final String path = "http://www.marcelorcorrea.com/";
        DummyDownload dummyDownload = new DummyDownload("image.jpg", "http://www.marcelorcorrea.com/image.jpg", 89);
        DummyDownload dummyDownload2 = new DummyDownload("image2.jpg", "http://www.marcelorcorrea.com/image2.jpg", 50);
        DummyDownload dummyDownload3 = new DummyDownload("image3.jpg", "http://www.marcelorcorrea.com/image3.jpg", 17);

        final Map<String, DummyDownload> urls = new HashMap<>();
        urls.put(dummyDownload.url, dummyDownload);
        urls.put(dummyDownload2.url, dummyDownload2);
        urls.put(dummyDownload3.url, dummyDownload3);
        ImageDownloaderListener imageDownloaderListener = new ImageDownloaderListener() {

            @Override
            public void onTaskStart(int numberOfImages) {
                Assert.assertEquals(urls.size(), numberOfImages);
            }

            @Override
            public void onDownloadStart(String url, int contentLength) {
                Assert.assertTrue(urls.containsKey(url));
                Assert.assertEquals(urls.get(url).contentLength, contentLength);
            }

            @Override
            public void onDownloadInProgress(String url, int progress) {
                Assert.assertTrue(urls.containsKey(url));
                DummyDownload dd = urls.get(url);
                dd.progress = progress;
            }

            @Override
            public void onDownloadComplete(String url, DownloadedImage downloadedImage) {
                Assert.assertTrue(urls.containsKey(url));
                Assert.assertEquals(bytes, downloadedImage.getBytes());
                DummyDownload dd = urls.get(url);
                Assert.assertEquals(dd.name, downloadedImage.getName());
                Assert.assertEquals("jpg", downloadedImage.getExtension());
                Assert.assertEquals(dd.contentLength, dd.progress);
            }

            @Override
            public void onDownloadFail(String imgSource) {
            }

            @Override
            public void onTaskFinished() {
            }
        };

        DownloadManager manager = Mockito.spy(DownloadManager.class);
        Mockito.doReturn(ContentType.HTML).doReturn(ContentType.JPG).when(manager).getContentType(Mockito.anyString());

        URL urlImage = PowerMockito.mock(URL.class);
        URL urlImage2 = PowerMockito.mock(URL.class);
        URL urlImage3 = PowerMockito.mock(URL.class);

        HttpURLConnection connectionImage = Mockito.mock(HttpURLConnection.class);
        HttpURLConnection connectionImage2 = Mockito.mock(HttpURLConnection.class);
        HttpURLConnection connectionImage3 = Mockito.mock(HttpURLConnection.class);

        InputStream inputStream = Mockito.mock(InputStream.class);
        InputStream inputStream2 = Mockito.mock(InputStream.class);
        InputStream inputStream3 = Mockito.mock(InputStream.class);

        when(inputStream.read(Mockito.any(byte[].class))).thenReturn(15).thenReturn(14).thenReturn(15).thenReturn(15).thenReturn(15).thenReturn(15).thenReturn(-1);
        when(inputStream2.read(Mockito.any(byte[].class))).thenReturn(15).thenReturn(15).thenReturn(15).thenReturn(5).thenReturn(-1);
        when(inputStream3.read(Mockito.any(byte[].class))).thenReturn(15).thenReturn(2).thenReturn(-1);

        PowerMockito.whenNew(URL.class).withArguments("http://www.marcelorcorrea.com/image.jpg").thenReturn(urlImage);
        PowerMockito.whenNew(URL.class).withArguments("http://www.marcelorcorrea.com/image2.jpg").thenReturn(urlImage2);
        PowerMockito.whenNew(URL.class).withArguments("http://www.marcelorcorrea.com/image3.jpg").thenReturn(urlImage3);

        Mockito.when(urlImage.openConnection()).thenReturn(connectionImage);
        Mockito.when(urlImage2.openConnection()).thenReturn(connectionImage2);
        Mockito.when(urlImage3.openConnection()).thenReturn(connectionImage3);

        when(connectionImage.getContentLength()).thenReturn(89);
        when(connectionImage.getInputStream()).thenReturn(inputStream);
        when(connectionImage.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        when(connectionImage2.getContentLength()).thenReturn(50);
        when(connectionImage2.getInputStream()).thenReturn(inputStream2);
        when(connectionImage2.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        when(connectionImage3.getContentLength()).thenReturn(17);
        when(connectionImage3.getInputStream()).thenReturn(inputStream3);
        when(connectionImage3.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        ByteArrayOutputStream byteArrayOutputStream = Mockito.mock(ByteArrayOutputStream.class);
        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
        when(byteArrayOutputStream.toByteArray()).thenReturn(bytes);

        Set<String> result = new HashSet<>();
        result.add("http://www.marcelorcorrea.com/image.jpg");
        result.add("http://www.marcelorcorrea.com/image2.jpg");
        result.add("http://www.marcelorcorrea.com/image3.jpg");
        when(parser.parse(Mockito.anyString(), any(ContentType.class))).thenReturn(result);

        imageDownloader.setImageFetcher(manager);
        manager.setListener((ImageFetcher.ImageFetcherCallback) imageDownloader);

        imageDownloader.setOnDownloadListener(imageDownloaderListener);
        imageDownloader.download(path, ContentType.JPG);
    }

    //    @Test
    public void testContentTypeMatchesJPG() throws ImageDownloaderException, InterruptedException, UnknownContentTypeException {
        String path = "http://www.marcelorcorrea.com/";
        Set<String> result = new HashSet<>();
        result.add("http://www.marcelorcorrea.com/image.jpg");
        result.add("http://www.marcelorcorrea.com/image2.jpg");
        result.add("http://www.marcelorcorrea.com/image3.jpg");
        when(parser.parse(Mockito.anyString(), any(ContentType.class))).thenReturn(result);
        when(connection.getContentType()).thenReturn("text/html", "image/pjpeg", "image/jpeg", "image/jpg");
        imageDownloader.download(path, ContentType.JPG);
        Thread.sleep(100);
        verify(parser).parse(path, ContentType.JPG);
//        Assert.assertEquals(3, dummyListener.images.size());
    }

    //    @Test
    public void testHappyPathWhenContentTypeIsHTML() throws ImageDownloaderException, InterruptedException, UnknownContentTypeException {
        String path = "http://www.marcelorcorrea.com/";
        Set<String> result = new HashSet<>();
        result.add("http://www.marcelorcorrea.com/image.jpg");
        result.add("http://www.marcelorcorrea.com/image2.jpg");
        when(parser.parse(Mockito.anyString(), any(ContentType.class))).thenReturn(result);
        imageDownloader.download(path, ContentType.JPG);
        //wait threads to process images...
        Thread.sleep(1000);
//        Assert.assertEquals(2, dummyListener.size);
//        Assert.assertEquals(2, dummyListener.images.size());
//        Assert.assertEquals("image2.jpg", dummyListener.images.get(0).getName());
//        Assert.assertEquals("jpg", dummyListener.images.get(0).getExtension());
//        Assert.assertEquals("image.jpg", dummyListener.images.get(1).getName());
//        Assert.assertEquals("jpg", dummyListener.images.get(1).getExtension());
    }

    //    @Test
    public void testHappyPathWhenContentTypeIsPNG() throws ImageDownloaderException, InterruptedException, UnknownContentTypeException {
        String path = "http://www.marcelorcorrea.com/image.png";
        when(connection.getContentType()).thenReturn("image/png");
        imageDownloader.download(path, ContentType.PNG);
        //wait threads to process images...
        Thread.sleep(500);
//        Assert.assertEquals(1, dummyListener.size);
//        Assert.assertEquals(1, dummyListener.images.size());
//        Assert.assertEquals("image.png", dummyListener.images.get(0).getName());
//        Assert.assertEquals("png", dummyListener.images.get(0).getExtension());
    }

    //    @Test
    public void testWriteToDisk() throws Exception {
        PowerMockito.mockStatic(Files.class);
        DownloadedImage downloadedImage = Mockito.mock(DownloadedImage.class);
        when(downloadedImage.getName()).thenReturn("image.jpg");
        File file = Mockito.mock(File.class);
        PowerMockito.whenNew(File.class).withArguments(file, "image.jpg").thenReturn(file);
        boolean result = imageDownloader.writeToDisk(Collections.singletonList(downloadedImage), file);
//        Assert.assertTrue(result);
    }

    //    @Test
    public void testExceptionWriteToDisk() throws Exception {
        PowerMockito.mockStatic(Files.class);
        DownloadedImage downloadedImage = Mockito.mock(DownloadedImage.class);
        when(downloadedImage.getName()).thenReturn("image.jpg");
        File file = Mockito.mock(File.class);
        PowerMockito.whenNew(File.class).withArguments(file, "image.jpg").thenThrow(new IOException("Error"));
        boolean result = imageDownloader.writeToDisk(Collections.singletonList(downloadedImage), file);
//        Assert.assertFalse(result);
    }


    //    @Test
    public void testEmptyURLField() throws ImageDownloaderException, UnknownContentTypeException {
//        expectedEx.expect(ImageDownloaderException.class);
//        expectedEx.expectMessage("URL is a required field.");
        imageDownloader.download("", ContentType.JPEG);
    }

    //    @Test
    public void testNullURLField() throws ImageDownloaderException, UnknownContentTypeException {
//        expectedEx.expect(ImageDownloaderException.class);
//        expectedEx.expectMessage("URL is a required field.");
        imageDownloader.download(null, ContentType.JPEG);
    }

    //    @Test
    public void testURLWithoutHTTP() throws ImageDownloaderException, UnknownContentTypeException {
        String path = "www.marcelorcorrea.com/image.jpg";
        when(connection.getContentType()).thenReturn("image/jpg");
        imageDownloader.download(path, ContentType.JPG);
//        Assert.assertEquals(1, dummyListener.images.size());
    }

    //    @Test
    public void testWithUnsupportedProtocol() throws ImageDownloaderException, UnknownContentTypeException {
        String path = "ftp://www.marcelorcorrea.com/image.jpg";
//        expectedEx.expect(ImageDownloaderException.class);
//        expectedEx.expectMessage("Only protocols HTTP and HTTPS are supported.");
        imageDownloader.download(path, ContentType.JPEG);
    }

    class DummyListener implements ImageDownloaderListener {

        @Override
        public void onTaskStart(int numberOfImages) {

        }

        @Override
        public void onDownloadStart(String url, int contentLength) {

        }

        @Override
        public void onDownloadInProgress(String url, int progress) {

        }

        @Override
        public void onDownloadComplete(String url, DownloadedImage downloadedImage) {

        }

        @Override
        public void onDownloadFail(String imgSource) {

        }

        @Override
        public void onTaskFinished() {

        }
    }

    class DummyDownload {
        String name;
        String url;
        int contentLength;
        int progress;
        int counter;

        public DummyDownload(String name, String url, int contentLength) {
            this.name = name;
            this.url = url;
            this.contentLength = contentLength;
        }
    }
}