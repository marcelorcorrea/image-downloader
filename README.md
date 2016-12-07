# Image-Downloader
Image Downloader is small Java prototype lib that tries to download images from a given URL.

It can be used by adding image-downloader-core.jar to your classpath. Or using a small Swing implementation included in this lib.

You can try image-downloader from here: `https://104.131.96.246/image-downloader-webstart/` (don't forget to include this address to 'java configure exceptions')

####Build
`mvn package`

####Usage
To run the Swing application with webstart,you need tomcat installed. Set your tomcat credentials/url in image-downloader/pom.xml in plugin org.apache.tomcat.maven. Don't forget to add `script-manager` role to your user in tomcat-users.xml.

Then run:

`mvn tomcat:deploy`

Then go to: http://<tomcat_address:port>/image-downloader-webstart/
Download and open jnlp file.

To create executable jar, go to image-downloader-swing/pom.xml
uncomment maven-assembly-plugin and build it:

`mvn clean package`

To execute:

`java -jar image-downloader-swing/target/Image-Downloader.jar`


######Adding into your classpath
Here's an example of how to use image-downloader in your project:

First you need to implement ``ImageDownloaderListener`` interface and its methods:
    
    void onTaskStart(int numberOfImages);

    void onDownloadStart(String url, int contentLength);

    void onDownloadInProgress(String url, int progress);

    void onDownloadComplete(String url, DownloadedImage downloadedImage);

    void onDownloadFail(String imgSource);

    void onTaskFinished();

Then provide this implementation to the constructor: 
```
public static void main(String[] args) {
    ImageDownloader imageDownloader = new GenericImageDownloader();
    imageDownloader.setOnDownloadListener(imageDownloaderListenerImp);
    String url = "http://www.example.com;
    //Here it looks for images of JPEG type
    images = imageDownloader.download(href, ContentType.JPEG);
}
```
To write the images to disk, you just need to call: 
`imageDownloader.writeToDisk(images, new File("/tmp/"));`