package edu.ucar.metviewer;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class MVLoadJobParser extends MVUtil {

  protected Hashtable _tableDateListDecl = new Hashtable();
  protected Hashtable _tableDateRangeDecl = new Hashtable();
  protected MVNode _nodeLoadSpec = null;
  protected MVLoadJob _job = null;

  protected Connection _con = null;

  public MVLoadJobParser(String spec) throws Exception {

    //  instantiate and configure the xml parser
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    //dbf.setSchema(schema);
    dbf.setValidating(false);
    dbf.setNamespaceAware(false);

    DocumentBuilder builder = dbf.newDocumentBuilder();
    builder.setErrorHandler(new ErrorHandler() {
      public void error(SAXParseException exception) {
        printException("error", exception);
      }

      public void fatalError(SAXParseException exception) {
        printException("fatalError", exception);
      }

      public void warning(SAXParseException exception) {
        printException("warning", exception);
      }

      public void printException(String type, SAXParseException e) {
        System.out.println("  **  ERROR: " + e.getMessage() + "\n" +
          "      line: " + e.getLineNumber() + "  column: " + e.getColumnNumber());
      }
    });

    //  parse the input document and build the MVNode data structure
    Document doc = builder.parse(spec);
    _nodeLoadSpec = new MVNode(doc.getFirstChild());

    parseLoadJobSpec();
  }

  public MVLoadJob getLoadJob() {
    return _job;
  }

  public void parseLoadJobSpec() throws Exception {
    MVLoadJob job = new MVLoadJob();
    for (int i = 0; null != _nodeLoadSpec && i < _nodeLoadSpec._children.length; i++) {
      MVNode node = _nodeLoadSpec._children[i];

      //  <connection>
      if (node._tag.equals("connection")) {
        for (int j = 0; j < node._children.length; j++) {
          if (node._children[j]._tag.equals("host")) {
            job.setDBHost(node._children[j]._value);
          } else if (node._children[j]._tag.equals("database")) {
            job.setDBName(node._children[j]._value);
          } else if (node._children[j]._tag.equals("user")) {
            job.setDBUser(node._children[j]._value);
          } else if (node._children[j]._tag.equals("password")) {
            job.setDBPassword(node._children[j]._value);
          }
        }

        try {
          //  connect to the database
          Class.forName("com.mysql.jdbc.Driver").newInstance();
          Connection con = DriverManager.getConnection("jdbc:mysql://" + job.getDBHost() + "/" + job.getDBName(), job.getDBUser(), job.getDBPassword());
          if (con.isClosed())
            throw new Exception("METViewer load error: database connection failed");
          _con = con;
          job.setConnection(con);
        } catch (Exception ex) {
          System.out.println("  **  ERROR: parseLoadJobSpec() caught " + ex.getClass() + " connecting to database: " + ex.getMessage());
          throw ex;
        }
      }

      //  <date_list>
      else if (node._tag.equals("date_list")) {
        _tableDateListDecl.put(node._name, buildDateList(node));
      }

      //  simple string fields
      else if (node._tag.equals("mod_schema")) {
        job.setModSchema(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("load_stat")) {
        job.setLoadStat(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("load_mode")) {
        job.setLoadMode(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("load_mpr")) {
        job.setLoadMpr(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("load_orank")) {
        job.setLoadOrank(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("force_dup_file")) {
        job.setForceDupFile(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("met_version")) {
        job.setMetVersion(node._value);
      } else if (node._tag.equals("insert_size")) {
        job.setInsertSize(Integer.parseInt(node._value));
      } else if (node._tag.equals("verbose")) {
        job.setVerbose(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("mode_header_db_check")) {
        job.setModeHeaderDBCheck(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("drop_indexes")) {
        job.setDropIndexes(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("apply_indexes")) {
        job.setApplyIndexes(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("folder_tmpl")) {
        job.setFolderTmpl(node._value);
      } else if (node._tag.equals("load_note")) {
        job.setLoadNote(node._value);
      } else if (node._tag.equals("load_xml")) {
        job.setLoadXML(node._value.equalsIgnoreCase("true"));
      }

      //  <load_files>
      else if (node._tag.equals("load_files")) {
        ArrayList listLoadFiles = new ArrayList();
        for (int j = 0; j < node._children.length; j++) {
          listLoadFiles.add(node._children[j]._value);
        }
        job.setLoadFiles(toArray(listLoadFiles));
      }

      //  <load_val>
      else if (node._tag.equals("load_val")) {
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeField = node._children[j];
          String strFieldName = nodeField._name;
          ArrayList listVal = new ArrayList();
          for (int k = 0; k < nodeField._children.length; k++) {
            MVNode nodeChild = nodeField._children[k];

            //  <val>
            if (nodeChild._tag.equals("val")) {
              listVal.add(nodeChild._value);
            }

            //  <date_list>
            else if (nodeChild._tag.equals("date_list")) {
              listVal.addAll(Arrays.asList((String[]) _tableDateListDecl.get(nodeChild._name)));
            }
          }
          job.addLoadVal(strFieldName, toArray(listVal));
        }
      }

      //  <line_type>
      else if (node._tag.equals("line_type")) {
        for (int j = 0; j < node._children.length; j++) {
          job.addLineTypeLoad(node._children[j]._value);
        }
        if (0 < job.getLineTypeLoadMap().size()) {
          job.setLineTypeLoad(true);
        }
      }
    }

    _job = job;
  }

  public static boolean checkJobCompleteness(MVLoadJob job) {
    if (null == job._con) {
      return false;
    } else if (job.getFolderTmpl().equals("")) {
      return false;
    } else if (1 > job.getLoadVal().size()) {
      return false;
    }

    return true;
  }

}
