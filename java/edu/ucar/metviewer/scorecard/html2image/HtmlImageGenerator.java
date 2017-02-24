/**
 * HtmlImageGenerator.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.html2image;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author : tatiana $
 * @version : 1.0 : 17/01/17 13:20 $
 */
public class HtmlImageGenerator {

  private static final Logger logger = Logger.getLogger(HtmlImageGenerator.class);
  public static final String TEXT_HTML = "text/html";

  private JEditorPane editorPane;
  private static final Dimension DEFAULT_SIZE = new Dimension(800, 800);
  private static final Insets DEFAULT_INSETS = new Insets(0, 0, 20, 55);

  public HtmlImageGenerator() {
    editorPane = new JEditorPane();
    editorPane.setSize(DEFAULT_SIZE);
    editorPane.setEditable(false);
    editorPane.setEditorKitForContentType(TEXT_HTML, new SynchronousHTMLEditorKit());
    editorPane.setContentType(TEXT_HTML);
    //editorPane.setMargin(DEFAULT_INSETS);
    editorPane.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, Boolean.FALSE);
    editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editorPane.setOpaque(true);
  }


  public void loadHtml(final String html) {
    editorPane.setText(html);
  }


  public void saveAsImage(String file) {
    saveAsImage(new File(file));
  }

  public void saveAsImage(File file) {
    BufferedImage image = getBufferedImage();
    BufferedImage bufferedImageToWrite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics graphics2D = bufferedImageToWrite.createGraphics();
    graphics2D.drawImage(image, 0, 0, Color.WHITE, null);
    image.flush();
    bufferedImageToWrite.flush();
    final String formatName = FormatNameUtil.formatForFilename(file.getName());
    graphics2D.dispose();
    try {
      boolean success = ImageIO.write(bufferedImageToWrite, formatName, file);
      if (!success)
        throw new IOException("Error during creating an image  " + file);
    } catch (IOException e) {
      logger.error(e);
      throw new RuntimeException("Error during creating an image  " + file);
    }
  }


  public BufferedImage getBufferedImage() {
    Dimension prefSize = editorPane.getPreferredSize();
    editorPane.setSize(prefSize);
    BufferedImage img = new BufferedImage(prefSize.width, prefSize.height, BufferedImage.TYPE_INT_ARGB);
    Graphics graphics = img.createGraphics();
    editorPane.printAll(graphics);
    editorPane.repaint();
    graphics.dispose();
    return img;
  }

}

