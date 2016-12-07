package com.marcelorcorrea.imagedownloader.swing;


import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 *
 * @author Marcelo
 */
@SuppressWarnings("serial")
class MakeImage extends JPanel {

    private Image image;

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public void paint(Graphics g) {
        if (image != null) {

            double horizontalScale = (double) this.getWidth() / image.getWidth(this);
            double verticalScale = (double) this.getHeight() / image.getHeight(this);
            int newWidth;
            int newHeight;
            if ((horizontalScale < 1) || (verticalScale < 1)) {
                if (horizontalScale < verticalScale) {
                    newWidth = this.getWidth();
                    newHeight = (int) Math.floor(image.getHeight(this) * horizontalScale);
                } else {
                    newHeight = this.getHeight();
                    newWidth = (int) Math.floor(image.getWidth(this) * verticalScale);
                }
                g.drawImage(image, 0, 0, newWidth, newHeight, this);
            } else {
                g.drawImage(image, 0, 0, image.getWidth(this), image.getHeight(this), this);
            }
        }
    }
}
