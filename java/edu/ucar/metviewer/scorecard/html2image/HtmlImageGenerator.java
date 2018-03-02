/**
 * HtmlImageGenerator.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.html2image;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : tatiana $
 * @version : 1.0 : 17/01/17 13:20 $
 */
public class HtmlImageGenerator {

  private static final Logger logger = LogManager.getLogger("HtmlImageGenerator");
  private static final String TEXT_HTML = "text/html";

  private final JEditorPane editorPane;
  private static final Dimension DEFAULT_SIZE = new Dimension(800, 800);

  public HtmlImageGenerator() {
    editorPane = new JEditorPane();

    editorPane.setEditable(false);
    editorPane.setEditorKitForContentType(TEXT_HTML, new SynchronousHTMLEditorKit());
    editorPane.setContentType(TEXT_HTML);
    editorPane.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, Boolean.FALSE);
    editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editorPane.setOpaque(true);
    //editorPane.setSize(DEFAULT_SIZE);
    Font font;
    try(InputStream is = getClass().getResourceAsStream("l_10646.ttf")) {
      font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, 14);
    } catch (FontFormatException | IOException e) {
      font = new Font("SansSerif", Font.PLAIN, 14);
      logger.error(e.getMessage());
    }
    editorPane.setFont(font);
  }


  public void loadHtml(final String html) {
    editorPane.setText(html.trim());
  }


  public void saveAsImage(String file) {
    saveAsImage(new File(file));
  }

  private void saveAsImage(File file) {
    final String formatName = FormatNameUtil.formatForFilename(file.getName());

    BufferedImage image = getInBufferedImage();
    BufferedImage bufferedImageToWrite = getOutBufferedImage(image);

    try {
      boolean success = ImageIO.write(bufferedImageToWrite, formatName, file);
      if (!success)
        throw new IOException("Error during creating an image  " + file);
    } catch (IOException e) {
      logger.error(e);
      throw new RuntimeException("Error during creating an image  " + file);
    }
  }

  private BufferedImage getOutBufferedImage(BufferedImage image) {
    BufferedImage bufferedImageToWrite = null;
    try {
      bufferedImageToWrite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics graphics2D = bufferedImageToWrite.createGraphics();
      graphics2D.drawImage(image, 0, 0, Color.WHITE, null);
      image.flush();
      bufferedImageToWrite.flush();
      graphics2D.dispose();
    } catch (Exception e) {
      logger.error(e);
    }
    return bufferedImageToWrite;
  }


  private BufferedImage getInBufferedImage() {
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

