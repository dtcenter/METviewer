/**
 * ScriptRunner.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.*;

/**
 * @author : tatiana $
 * @version : 1.0 : 18/Jul/14 11:57 $
 */
public class ScriptRunner {

  private static final String DEFAULT_DELIMITER = ";";

  private final Connection connection;

  private final boolean stopOnError;
  private final boolean autoCommit;

  private PrintWriter logWriter = new PrintWriter(System.out);
  private PrintWriter errorLogWriter = new PrintWriter(System.err);

  private String delimiter = DEFAULT_DELIMITER;
  private boolean fullLineDelimiter = false;

  /**
   * Default constructor
   */
  public ScriptRunner(Connection connection, boolean autoCommit,
                      boolean stopOnError) {
    this.connection = connection;
    this.autoCommit = autoCommit;
    this.stopOnError = stopOnError;
  }

  public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
    this.delimiter = delimiter;
    this.fullLineDelimiter = fullLineDelimiter;
  }

  /**
   * Setter for logWriter property
   *
   * @param logWriter - the new value of the logWriter property
   */
  public void setLogWriter(PrintWriter logWriter) {
    this.logWriter = logWriter;
  }

  /**
   * Setter for errorLogWriter property
   *
   * @param errorLogWriter - the new value of the errorLogWriter property
   */
  public void setErrorLogWriter(PrintWriter errorLogWriter) {
    this.errorLogWriter = errorLogWriter;
  }

  /**
   * Runs an SQL script (read in using the Reader parameter)
   *
   * @param reader - the source of the script
   */
  public void runScript(Reader reader) throws IOException, SQLException {
    try {
      boolean originalAutoCommit = connection.getAutoCommit();
      try {
        if (originalAutoCommit != this.autoCommit) {
          connection.setAutoCommit(this.autoCommit);
        }
        runScript(connection, reader);
      } finally {
        connection.setAutoCommit(originalAutoCommit);
      }
    } catch (IOException | SQLException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error running script.  Cause: " + e, e);
    }
  }

  /**
   * Runs an SQL script (read in using the Reader parameter) using the
   * connection passed in
   *
   * @param conn   - the connection to use for the script
   * @param reader - the source of the script
   * @throws SQLException if any SQL errors occur
   * @throws IOException  if there is an error reading from the Reader
   */
  private void runScript(Connection conn, Reader reader) throws IOException,
    SQLException {
    StringBuffer command = null;
    try {
      LineNumberReader lineReader = new LineNumberReader(reader);
      String line;
      while ((line = lineReader.readLine()) != null) {
        if (command == null) {
          command = new StringBuffer();
        }
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("--")) {
          println(trimmedLine);
        } else if (trimmedLine.length() < 1
          || trimmedLine.startsWith("//")) {
          // Do nothing
        } else if (trimmedLine.length() < 1
          || trimmedLine.startsWith("--")) {
          // Do nothing
        } else if (trimmedLine.startsWith("DELIMITER")) {
          this.delimiter = trimmedLine.substring(trimmedLine.length() - 1);

        } else if (!fullLineDelimiter
          && trimmedLine.endsWith(getDelimiter())
          || fullLineDelimiter
          && trimmedLine.equals(getDelimiter())) {
          command.append(line.substring(0, line
            .lastIndexOf(getDelimiter())));
          command.append(' ');
          Statement statement = conn.createStatement();


          println(command);

          boolean hasResults = false;
          if (stopOnError) {
            hasResults = statement.execute(command.toString());
          } else {
            try {
              statement.execute(command.toString());
            } catch (SQLException e) {
              e.fillInStackTrace();
              printlnError("Error executing: " + command);
              printlnError(e);
            }
          }

          if (autoCommit && !conn.getAutoCommit()) {
            conn.commit();
          }

          ResultSet rs = statement.getResultSet();
          if (hasResults && rs != null) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 0; i < cols; i++) {
              String name = md.getColumnLabel(i);
              print(name + "\t");
            }
            println("");
            while (rs.next()) {
              for (int i = 0; i < cols; i++) {
                String value = rs.getString(i);
                print(value + "\t");
              }
              println("");
            }
          }

          command = null;
          try {
            statement.close();
          } catch (Exception e) {
            // Ignore to workaround a bug in Jakarta DBCP
          }
          Thread.yield();
        } else {
          command.append(line);
          command.append(' ');
        }
      }
      if (!autoCommit) {
        conn.commit();
      }
    } catch (SQLException | IOException e) {
      e.fillInStackTrace();
      printlnError("Error executing: " + command);
      printlnError(e);
      throw e;
    } finally {
      conn.rollback();
      flush();
    }
  }

  private String getDelimiter() {
    return delimiter;
  }

  private void print(Object o) {
    if (logWriter != null) {
      System.out.print(o);
    }
  }

  private void println(Object o) {
    if (logWriter != null) {
      logWriter.println(o);
    }
  }

  private void printlnError(Object o) {
    if (errorLogWriter != null) {
      errorLogWriter.println(o);
    }
  }

  private void flush() {
    if (logWriter != null) {
      logWriter.flush();
    }
    if (errorLogWriter != null) {
      errorLogWriter.flush();
    }
  }
}
