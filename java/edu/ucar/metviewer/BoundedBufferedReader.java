/**
 * BoundedBufferedReader.java Copyright UCAR (c) 2019. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2019.
 */

package edu.ucar.metviewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author : tatiana $
 * @version : 1.0 : 2019-02-21 09:04 $
 */
public class BoundedBufferedReader extends BufferedReader {

  private static final int DEFAULT_MAX_LINES = 10000;      //Max lines per file
  private static final int DEFAULT_MAX_LINE_LENGTH = 1024;  //Max bytes per line

  private int readerMaxLines;
  private int readerMaxLineLen;
  private int currentLine = 1;

  public BoundedBufferedReader(FileReader reader, int maxLines, int maxLineLen) {
    super(reader);
    readerMaxLines = maxLines;
    readerMaxLineLen = maxLineLen;
  }

  public BoundedBufferedReader(InputStreamReader reader, int maxLines, int maxLineLen) {
    super(reader);
    if ((maxLines <= 0) || (maxLineLen <= 0)) {
      throw new IllegalArgumentException(
              "BoundedBufferedReader - maxLines and maxLineLen must be greater than 0");
    }

    readerMaxLines = maxLines;
    readerMaxLineLen = maxLineLen;
  }

  public BoundedBufferedReader(FileReader reader) {
    super(reader);
    readerMaxLines = DEFAULT_MAX_LINES;
    readerMaxLineLen = DEFAULT_MAX_LINE_LENGTH;
  }

  public BoundedBufferedReader(InputStreamReader reader) {
    super(reader);
    readerMaxLines = DEFAULT_MAX_LINES;
    readerMaxLineLen = DEFAULT_MAX_LINE_LENGTH;
  }

  public String readLineBounded() throws IOException {
    //Check readerMaxLines limit
    if (currentLine > readerMaxLines) {
      throw new IOException("BoundedBufferedReader - Line read limit has been reached.");
    }
    currentLine++;

    int currentPos = 0;
    char[] data = new char[readerMaxLineLen];
    final int CR = 13;
    final int LF = 10;
    int currentCharVal = super.read();

    //Read characters and add them to the data buffer until we hit the end of a line or the end of the file.
    while ((currentCharVal != CR) && (currentCharVal != LF) && (currentCharVal >= 0)) {
      data[currentPos++] = (char) currentCharVal;
      //Check readerMaxLineLen limit
      if (currentPos < readerMaxLineLen) {
        currentCharVal = super.read();
      } else {
        break;
      }
    }

    if (currentCharVal < 0) {
      //End of file
      if (currentPos > 0)
      //Return last line
      {
        return (new String(data, 0, currentPos));
      } else {
        return null;
      }
    } else {
      //Remove newline characters from the buffer
      if (currentCharVal == CR) {
        //Check for LF and remove from buffer
        super.mark(1);
        if (super.read() != LF) {
          super.reset();
        }
      } else if (currentCharVal != LF) {
        //readerMaxLineLen has been hit, but we still need to remove newline characters.
        super.mark(1);
        int nextCharVal = super.read();
        if (nextCharVal == CR) {
          super.mark(1);
          if (super.read() != LF) {
            super.reset();
          }
        } else if (nextCharVal != LF) {
          super.reset();
        }
      }
      return (new String(data, 0, currentPos));
    }

  }
}