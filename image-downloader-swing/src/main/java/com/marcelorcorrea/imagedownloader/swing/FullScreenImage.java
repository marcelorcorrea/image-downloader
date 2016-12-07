package com.marcelorcorrea.imagedownloader.swing;

import com.marcelorcorrea.imagedownloader.core.model.DownloadedImage;
import com.marcelorcorrea.imagedownloader.swing.cache.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.List;

class FullScreenImage extends JFrame {

    private static final int MAX_WIDTH_SIZE = 1024;
    private static final int MAX_HEIGHT_SIZE = 768;

    private final JLabel imageLabel;
    private final List<DownloadedImage> images;
    private int index;

    public FullScreenImage(List<DownloadedImage> images, int index) {
        this.index = index;
        this.images = images;
        this.imageLabel = new JLabel();
        appendListener();
        generateImage(this.index);
    }

    private void appendListener() {
        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (index < images.size() - 1) {
                        generateImage(++index);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (index > 0) {
                        generateImage(--index);
                    }
                }
            }
        });
    }

    private void generateImage(int index) {
        DownloadedImage downloadedImage = images.get(index);
        BufferedImage image = ImageCache.retrieve(downloadedImage.getName());
        Dimension dimension = resizeImage(image);
        BufferedImage bufferedImage = createNewBufferedImage(image, dimension);

        imageLabel.setIcon(new ImageIcon(bufferedImage));

        setSize(dimension);
        setLocationRelativeTo(null);
        this.setTitle(downloadedImage.getName());
        add(imageLabel);
    }

    private Dimension resizeImage(BufferedImage image) {
        double horizontalScale = (double) MAX_WIDTH_SIZE / image.getWidth();
        double verticalScale = (double) MAX_HEIGHT_SIZE / image.getHeight();
        int newWidth = MAX_WIDTH_SIZE;
        int newHeight = MAX_HEIGHT_SIZE;

        Dimension dimension;

        if ((horizontalScale < 1) || (verticalScale < 1)) {
            if (horizontalScale < verticalScale) {
                newHeight = (int) Math.floor(image.getHeight() * horizontalScale);
            } else {
                newWidth = (int) Math.floor(image.getWidth() * verticalScale);
            }

            dimension = new Dimension(newWidth, newHeight);
        } else {
            dimension = new Dimension(image.getWidth(), image.getHeight());
        }
        return dimension;
    }

    private BufferedImage createNewBufferedImage(BufferedImage image, Dimension dimension) {
        BufferedImage newImage = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, dimension.width, dimension.height, null);
        g.dispose();
        return newImage;
    }

}