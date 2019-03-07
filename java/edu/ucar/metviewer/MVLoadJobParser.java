package edu.ucar.metviewer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MVLoadJobParser {

  private final Map dateListDecl = new HashMap();
  private MVNode loadSpec;
  protected MVLoadJob job = null;


  public MVLoadJobParser(String spec) throws ParserConfigurationException, IOException, SAXException {

    super();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    dbf.setValidating(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    documentBuilder.setErrorHandler(null);
    Document doc = documentBuilder.parse(spec);
    loadSpec = new MVNode(doc.getFirstChild());
    parseLoadJobSpec();
  }

  public MVLoadJob getLoadJob() {
    return job;
  }

  private void parseLoadJobSpec() {
    MVLoadJob loadJob = new MVLoadJob();
    List<String> listLoadFiles;
    List<String> listVal;
    String strFieldName;
    MVNode nodeChild;
    MVNode nodeField;
    for (int i = 0; null != loadSpec && i < loadSpec.children.length; i++) {
      MVNode node = loadSpec.children[i];

      //  <connection>
      if (node.tag.equals("connection")) {
        for (int j = 0; j < node.children.length; j++) {
          if (node.children[j].tag.equals("host")) {
            loadJob.setDBHost(node.children[j].value);
          } else if (node.children[j].tag.equals("database")) {
            loadJob.setDBName(node.children[j].value);
          } else if (node.children[j].tag.equals("user")) {
            loadJob.setDBUser(node.children[j].value);
          } else if (node.children[j].tag.equals("password")) {
            loadJob.setDBPassword(node.children[j].value);
          } else if (node.children[j].tag.equals("management_system")) {
            loadJob.setDBManagementSystem(node.children[j].value);
          }
        }
      }

      //  <date_list>
      else if (node.tag.equals("date_list")) {
        dateListDecl.put(node.name, MVUtil.buildDateList(node, System.out));
      }

      //  simple string fields
      else if (node.tag.equals("mod_schema")) {
        loadJob.setModSchema(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("load_stat")) {
        loadJob.setLoadStat(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("load_mode")) {
        loadJob.setLoadMode(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("load_mtd")) {
        loadJob.setLoadMtd(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("load_mpr")) {
        loadJob.setLoadMpr(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("load_orank")) {
        loadJob.setLoadOrank(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("force_dup_file")) {
        loadJob.setForceDupFile(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("insert_size")) {
        loadJob.setInsertSize(Integer.parseInt(node.value));
      } else if (node.tag.equals("verbose")) {
        loadJob.setVerbose(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("mode_header_db_check")) {
        loadJob.setModeHeaderDBCheck(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("mtd_header_db_check")) {
        loadJob.setMtdHeaderDBCheck(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("stat_header_db_check")) {
        loadJob.setStatHeaderDBCheck(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("drop_indexes")) {
        loadJob.setDropIndexes(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("apply_indexes")) {
        loadJob.setApplyIndexes(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("folder_tmpl")) {
        loadJob.setFolderTmpl(node.value);
      } else if (node.tag.equals("load_note")) {
        loadJob.setLoadNote(node.value);
      } else if (node.tag.equals("load_xml")) {
        loadJob.setLoadXML(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("group")) {
        loadJob.setGroup(node.value);
      } else if (node.tag.equals("description")) {
        loadJob.setDescription(node.value);
      }

      //  <load_files>
      else if (node.tag.equals("load_files")) {
        listLoadFiles = new ArrayList<>();
        for (int j = 0; j < node.children.length; j++) {
          listLoadFiles.add(node.children[j].value);
        }
        loadJob.setLoadFiles(MVUtil.toArray(listLoadFiles));
      }

      //  <load_val>
      else if (node.tag.equals("load_val")) {
        for (int j = 0; j < node.children.length; j++) {
          nodeField = node.children[j];
          strFieldName = nodeField.name;
          listVal = new ArrayList<>();
          for (int k = 0; k < nodeField.children.length; k++) {
            nodeChild = nodeField.children[k];

            //  <val>
            if (nodeChild.tag.equals("val")) {
              listVal.add(nodeChild.value);
            }

            //  <date_list>
            else if (nodeChild.tag.equals("date_list")) {
              if (dateListDecl.get(nodeChild.name) instanceof List) {
                listVal.addAll((List) dateListDecl.get(nodeChild.name));
              } else {
                listVal.addAll(Arrays.asList((String[]) dateListDecl.get(nodeChild.name)));
              }
            }
          }
          loadJob.addLoadVal(strFieldName, MVUtil.toArray(listVal));
        }
      }

      //  <line_type>
      else if (node.tag.equals("line_type")) {
        for (int j = 0; j < node.children.length; j++) {
          loadJob.addLineTypeLoad(node.children[j].value);
        }
        if (0 < loadJob.getLineTypeLoadMap().size()) {
          loadJob.setLineTypeLoad(true);
        }
      }
    }

    //check if all load values are present in folder_tmpl
    String[] loadVals = loadJob.getLoadVal().getKeyList();
    for (String val : loadVals) {
      if (!loadJob.getFolderTmpl().contains("{" + val + "}")) {
        //remove value
        loadJob.getLoadVal().remove(val);
      }
    }

    this.job = loadJob;
  }

}
