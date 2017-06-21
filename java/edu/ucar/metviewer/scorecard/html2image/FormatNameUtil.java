/**
 * FormatNameUtil.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.html2image;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 17/01/17 13:22 $
 */
class FormatNameUtil {

  private static final Map<String, String> types = new HashMap<>();
  private static final String DEFAULT_FORMAT = "png";

  private FormatNameUtil() {
  }

  static {
    types.put("gif", "gif");
    types.put("jpg", "jpg");
    types.put("jpeg", "jpg");
    types.put("png", "png");
  }

  private static String formatForExtension(String extension) {
    final String type = types.get(extension);
    if (type == null) {
      return DEFAULT_FORMAT;
    }
    return type;
  }

  public static String formatForFilename(final String fileName) {
    final int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex < 0) {
      return DEFAULT_FORMAT;
    }
    final String ext = fileName.substring(dotIndex + 1);
    return formatForExtension(ext);
  }
}
