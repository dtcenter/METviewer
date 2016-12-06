package edu.ucar.metviewer;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

public class MVLoadJobParser extends MVUtil {

  protected final Map _tableDateListDecl = new HashMap();
  protected MVNode _nodeLoadSpec = null;
  protected MVLoadJob _job = null;


  public MVLoadJobParser(String spec) throws Exception {

  super();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document doc = dbf.newDocumentBuilder().parse(spec);

    _nodeLoadSpec = new MVNode(doc.getFirstChild());

    parseLoadJobSpec();
  }

  public MVLoadJob getLoadJob() {
    return _job;
  }

  private void parseLoadJobSpec() {
    MVLoadJob job = new MVLoadJob();
    List<String> listLoadFiles;
    List<String> listVal;
    String strFieldName;
    MVNode nodeChild;
    MVNode nodeField;
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
          } else if (node._children[j]._tag.equals("management_system")) {
            job.setDBManagementSystem(node._children[j]._value);
          }
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
      } else if (node._tag.equals("insert_size")) {
        job.setInsertSize(Integer.parseInt(node._value));
      } else if (node._tag.equals("verbose")) {
        job.setVerbose(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("mode_header_db_check")) {
        job.setModeHeaderDBCheck(node._value.equalsIgnoreCase("true"));
      } else if (node._tag.equals("stat_header_db_check")) {
        job.setStatHeaderDBCheck(node._value.equalsIgnoreCase("true"));
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
         listLoadFiles = new ArrayList<>();
        for (int j = 0; j < node._children.length; j++) {
          listLoadFiles.add(node._children[j]._value);
        }
        job.setLoadFiles(toArray(listLoadFiles));
      }

      //  <load_val>
      else if (node._tag.equals("load_val")) {
        for (int j = 0; j < node._children.length; j++) {
           nodeField = node._children[j];
           strFieldName = nodeField._name;
           listVal = new ArrayList<>();
          for (int k = 0; k < nodeField._children.length; k++) {
             nodeChild = nodeField._children[k];

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

    //check if all load values are present in folder_tmpl
    String[] loadVals = job.getLoadVal().getKeyList();
    if(loadVals != null) {
      for (String val : loadVals) {
        if (!job.getFolderTmpl().contains("{" + val + "}")) {
          //remove value
          job.getLoadVal().remove(val);
        }
      }
    }

    _job = job;
  }

}
