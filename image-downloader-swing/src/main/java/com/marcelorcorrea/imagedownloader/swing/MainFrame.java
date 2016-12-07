package com.marcelorcorrea.imagedownloader.swing;

import com.marcelorcorrea.imagedownloader.core.ImageDownloader;
import com.marcelorcorrea.imagedownloader.core.exception.ImageDownloaderException;
import com.marcelorcorrea.imagedownloader.core.exception.UnknownContentTypeException;
import com.marcelorcorrea.imagedownloader.core.imp.GenericImageDownloader;
import com.marcelorcorrea.imagedownloader.core.listener.ImageDownloaderListener;
import com.marcelorcorrea.imagedownloader.core.model.ContentType;
import com.marcelorcorrea.imagedownloader.core.model.DownloadedImage;
import com.marcelorcorrea.imagedownloader.swing.cache.ImageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcelo Correa
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ImageDownloaderListener {

    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private final ImageDownloader downloader;
    private JFileChooser fileChooser;
    private File currentDirectory;
    private JButton okButton;
    private JButton saveButton;
    private JTextField address;
    private JTextField txtWidth;
    private JTextField txtHeight;
    private JPopupMenu popupMenu;
    private JComboBox<ContentType> extension;
    private JProgressBar progressBar;
    private JList<DownloadedImage> list;
    private DownloadedImageModel listModel;
    private MakeImage image;
    private int numberOfDownloadedImages;
    private AtomicInteger currentNumberOfDownloadedImages;

    private MainFrame() {
        super("***** Image Downloader ***** - by Marcelo");

        downloader = new GenericImageDownloader();
        downloader.setOnDownloadListener(this);
        initComponents();
    }

    private void initComponents() {
        KeyListener enterListener = new EnterListener();
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        currentDirectory = new File(System.getProperty("user.home"));

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(100, 25));
        progressBar.setStringPainted(true);
        progressBar.setString("0%");

        listModel = new DownloadedImageModel();
        list = new JList<>(listModel);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DownloadedImage selectedImage = list.getSelectedValue();
                if (selectedImage != null) {
                    image.setImage(ImageCache.retrieve(selectedImage.getName()));
                }
                repaint();
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openFullScreenImage(((DownloadedImageModel) list.getModel()).getElements(), list.getSelectedIndex());
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openFullScreenImage(((DownloadedImageModel) list.getModel()).getElements(), list.getSelectedIndex());
                }
            }
        });


        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(200, 250));

        image = new MakeImage();
        image.setPreferredSize(new Dimension(300, 320));
        image.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openFullScreenImage(((DownloadedImageModel) list.getModel()).getElements(), list.getSelectedIndex());
                }
            }
        });

        JLabel lblAddress = new JLabel("URL: ");
        JLabel lblExtension = new JLabel("Extension: ");
        okButton = new JButton("OK");

        popupMenu = new JPopupMenu();
        JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        paste.setText("Paste");
        paste.setMnemonic(KeyEvent.VK_P);

        JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        copy.setText("Copy");
        copy.setMnemonic(KeyEvent.VK_C);

        JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
        cut.setText("Cut");
        cut.setMnemonic(KeyEvent.VK_T);

        popupMenu.add(cut);
        popupMenu.add(copy);
        popupMenu.add(paste);

        address = new JTextField();
        address.setPreferredSize(new Dimension(445, 28));
        address.addKeyListener(enterListener);
        address.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                checkForTriggerEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkForTriggerEvent(e);
            }

            private void checkForTriggerEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        address.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        address.selectAll();
                    }
                });
            }
        });
        ContentType[] types = new ContentType[]{ContentType.JPG, ContentType.PNG, ContentType.GIF};
        extension = new JComboBox<>(types);
        extension.setSelectedIndex(0);
        extension.setPreferredSize(new Dimension(70, 25));

        saveButton = new JButton("Save");

        JLabel lblWidth = new JLabel("Width:");
        JLabel lblHeight = new JLabel("Height:");
        txtWidth = new JTextField();
        txtWidth.addFocusListener(new TextFieldHandler());
        txtWidth.addKeyListener(enterListener);
        txtWidth.setPreferredSize(new Dimension(70, 25));
        txtHeight = new JTextField();
        txtHeight.addFocusListener(new TextFieldHandler());
        txtHeight.addKeyListener(enterListener);
        txtHeight.setPreferredSize(new Dimension(70, 25));
        JPanel filterPanel = new JPanel();
        filterPanel.setPreferredSize(new Dimension(430, 80));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter:"));
        filterPanel.add(lblWidth);
        filterPanel.add(txtWidth);
        filterPanel.add(lblHeight);
        filterPanel.add(txtHeight);
        filterPanel.add(lblExtension);
        filterPanel.add(extension);

        JPanel statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(135, 80));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status:"));
        statusPanel.add(progressBar);

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(570, 80));
        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Navigation:"));
        panel.add(lblAddress);
        panel.add(address);
        panel.add(okButton);

        JPanel listPanel = new JPanel();
        listPanel.setPreferredSize(new Dimension(210, 300));
        listPanel.add(scroll);
        listPanel.add(saveButton);

        JPanel imageProgress = new JPanel();
        imageProgress.add(image);

        JPanel listAndImage = new JPanel();
        listAndImage.setPreferredSize(new Dimension(570, 340));
        listAndImage.setBorder(BorderFactory.createTitledBorder("Available Images"));
        listAndImage.setLayout(new BorderLayout());
        listAndImage.add(listPanel, BorderLayout.WEST);
        listAndImage.add(imageProgress, BorderLayout.EAST);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveImages();
            }
        });
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                executeOKAction();
            }
        });

        try {
            this.setIconImage(ImageIO.read(getClass().getResourceAsStream("/download_icon.png")));
        } catch (IOException e1) {
            logger.debug("image icon not found, proceding with swing default icon...");
        }
        this.setLayout(new FlowLayout());
        this.add(panel);
        this.add(filterPanel);
        this.add(statusPanel);
        this.add(listAndImage);
        this.setSize(640, 550);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private File chooseFile() {
        fileChooser.setCurrentDirectory(currentDirectory);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            return null;
        } else {
            currentDirectory = fileChooser.getSelectedFile();
            return currentDirectory;
        }
    }

    private void clearAvailableImages() {
        list.clearSelection();
        listModel.clear();
        image.setImage(null);
        progressBar.setValue(0);
        progressBar.setString("0%");
        repaint();
    }

    private void enableComponents() {
        okButton.setEnabled(true);
        saveButton.setEnabled(true);
    }

    private void disableComponents() {
        okButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    private void downloadImages() {
        ImageCache.clear();
        clearAvailableImages();
        disableComponents();
        try {
            downloader.download(address.getText().trim(), (ContentType) extension.getSelectedItem());
        } catch (ImageDownloaderException | UnknownContentTypeException ex) {
            enableComponents();
            logger.error("Exception: " + ex + " -- message: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveImages() {
        List<DownloadedImage> images = list.getSelectedValuesList();

        File file = chooseFile();
        if (file != null && file.isDirectory() && !images.isEmpty()) {
            boolean isSuccess = downloader.writeToDisk(images, file);
            if (isSuccess) {
                String message;
                if (images.size() > 1) {
                    message = "Files successfully saved!";
                } else {
                    message = "File successfully saved!";
                }
                JOptionPane.showMessageDialog(null, message, "SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Could not save files!", "ERROR!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onTaskStart(int numberOfImages) {
//        logger.warn("onTaskStart ");
        logger.warn("Number of Total Files: " + numberOfImages);
        numberOfDownloadedImages = numberOfImages;
        if (numberOfDownloadedImages == 0) {
            enableComponents();
            return;
        }
        currentNumberOfDownloadedImages = new AtomicInteger();
        progressBar.setMaximum(numberOfDownloadedImages);
    }

    @Override
    public void onDownloadStart(String url, int contentLength) {
    }

    @Override
    public void onDownloadInProgress(String url, int progress) {
    }

    @Override
    public void onDownloadComplete(String url, DownloadedImage downloadedImage) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(downloadedImage.getBytes()));
            if (hasValidDimension(bufferedImage)) {
                ImageCache.cache(downloadedImage.getName(), bufferedImage);
                listModel.addElement(downloadedImage);
                listModel.update();
            }
            incrementProgressBar();
        } catch (IOException ex) {
            enableComponents();
            logger.error("Exception: " + ex + " -- message: " + ex.getMessage());
        }
    }

    @Override
    public void onDownloadFail(String imgSource) {
        incrementProgressBar();
    }

    @Override
    public void onTaskFinished() {
        enableComponents();
    }

    private void incrementProgressBar() {
        currentNumberOfDownloadedImages.getAndIncrement();
        progressBar.setValue(currentNumberOfDownloadedImages.get());
        progressBar.setString((currentNumberOfDownloadedImages.get() * 100) / numberOfDownloadedImages + "%");
    }

    private boolean hasValidDimension(BufferedImage bufferedImage) {
        int width = !txtWidth.getText().isEmpty() ? Integer.parseInt(txtWidth.getText()) : 0;
        int height = !txtHeight.getText().isEmpty() ? Integer.parseInt(txtHeight.getText()) : 0;
        return bufferedImage != null && bufferedImage.getWidth() >= width && bufferedImage.getHeight() >= height;
    }

    private void openFullScreenImage(List<DownloadedImage> images, int index) {
        FullScreenImage fsi = new FullScreenImage(images, index);
        fsi.setVisible(true);
    }

    private void executeOKAction() {
        new Thread() {
            @Override
            public void run() {
                if (address.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "URL field is required!", "WARNING!",
                            JOptionPane.WARNING_MESSAGE);
                    address.requestFocus();
                } else {
                    downloadImages();
                }
            }
        }.start();
    }

    public static void main(String[] args) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        new MainFrame();
    }

    private class EnterListener extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                executeOKAction();
            }
        }
    }

    private class TextFieldHandler extends FocusAdapter {

        @Override
        public void focusLost(FocusEvent e) {
            if (e.getSource() == txtHeight || e.getSource() == txtWidth) {
                JTextField field = (JTextField) e.getSource();
                if (!field.getText().isEmpty() && !field.getText().matches("[0-9]+")) {
                    JOptionPane.showMessageDialog(null, "Field accepts only number!", "WARNING!",
                            JOptionPane.WARNING_MESSAGE);
                    field.setText("");
                    field.requestFocus();
                }
            }
        }
    }
}
