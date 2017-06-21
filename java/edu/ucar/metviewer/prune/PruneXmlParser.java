/**
 * PruneXmlParser.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.prune;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/12/16 11:24 $
 */
class PruneXmlParser {

  private static final Logger logger = LogManager.getLogger("PruneXmlParser");


  /**
   * Parses XPL file with DB connection info, fields and thresholds
   *
   * @param filename - path to parameters file
   * @return
   */
  public MVPruneDB parseParameters(String filename) {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    MVPruneDB mvPruneDB = new MVPruneDB();
    try {
      db = dbf.newDocumentBuilder();
      Document doc = db.parse(new File(filename));
      Node pruneSpec = doc.getFirstChild();
      NodeList pruneSpecNodes = pruneSpec.getChildNodes();
      for (int i = 0; i < pruneSpecNodes.getLength(); i++) {
        Node pruneSpecNode = pruneSpecNodes.item(i);
        //for each node
        if (pruneSpecNode.getNodeType() == Node.ELEMENT_NODE && "connection".equals(pruneSpecNode.getNodeName())) {
          setDbConnection(mvPruneDB, pruneSpecNode);
        } else if (pruneSpecNode.getNodeType() == Node.ELEMENT_NODE && "info_only".equals(pruneSpecNode.getNodeName())) {
          setInfo(mvPruneDB, pruneSpecNode);
        } else if (pruneSpecNode.getNodeType() == Node.ELEMENT_NODE && "fields".equals(pruneSpecNode.getNodeName())) {
          setFields(mvPruneDB, pruneSpecNode);
        } else if (pruneSpecNode.getNodeType() == Node.ELEMENT_NODE && "files".equals(pruneSpecNode.getNodeName())) {
          setFiles(mvPruneDB, pruneSpecNode);
        } else if (pruneSpecNode.getNodeType() == Node.ELEMENT_NODE && "folders".equals(pruneSpecNode.getNodeName())) {
          setDirectoriesFromFolders(mvPruneDB, pruneSpecNode);
        }

      }
    } catch (Exception e) {
      logger.error("ERROR during reading XML file : " + e.getMessage());
    }
    return mvPruneDB;
  }

  private void setDirectoriesFromFolders(MVPruneDB mvPruneDB, Node pruneSpecNode) throws Exception {
    NodeList foldersNodeList = pruneSpecNode.getChildNodes();
    String folderTmpl = null;
    Map<String, List<String>> fieldTOValue = null;
    Map<String, List<String>> dateListToValues = new HashMap<>();
    //read template and values
    for (int j = 0; j < foldersNodeList.getLength(); j++) {
      Node foldersNode = foldersNodeList.item(j);
      if (foldersNode.getNodeType() == Node.ELEMENT_NODE && "folder_tmpl".equals(foldersNode.getNodeName())) {
        folderTmpl = foldersNode.getTextContent();
      } else if (foldersNode.getNodeType() == Node.ELEMENT_NODE && "load_val".equals(foldersNode.getNodeName())) {
        fieldTOValue = readDirectoriesTemplates(foldersNode);
      } else if (foldersNode.getNodeType() == Node.ELEMENT_NODE && "date_list".equals(foldersNode.getNodeName())) {
        DateList dateList = new DateList();
        dateList.setName(getFieldName(foldersNode));
        NodeList dateListNodeList = foldersNode.getChildNodes();

        for (int i = 0; i < dateListNodeList.getLength(); i++) {
          Node dateListNode = dateListNodeList.item(i);
          if (dateListNode.getNodeType() == Node.ELEMENT_NODE && "start".equals(dateListNode.getNodeName())) {
            dateList.setStartStr(dateListNode.getTextContent());
          } else if (dateListNode.getNodeType() == Node.ELEMENT_NODE && "end".equals(dateListNode.getNodeName())) {
            dateList.setEndStr(dateListNode.getTextContent());
          } else if (dateListNode.getNodeType() == Node.ELEMENT_NODE && "format".equals(dateListNode.getNodeName())) {
            dateList.setFormatStr(dateListNode.getTextContent());
          } else if (dateListNode.getNodeType() == Node.ELEMENT_NODE && "inc".equals(dateListNode.getNodeName())) {
            dateList.setIncrement(Integer.valueOf(dateListNode.getTextContent()));
          }

        }
        dateListToValues.put(dateList.getName(), dateList.getValues());

      }
    }
    if (!dateListToValues.isEmpty() && fieldTOValue != null ) {
      for (Map.Entry<String, List<String>> fieldTOValueEntry : fieldTOValue.entrySet()) {
        for (Map.Entry<String, List<String>> dateListToValuesEntry : dateListToValues.entrySet()) {
          if (fieldTOValueEntry.getValue().size() == 1 && fieldTOValueEntry.getValue().get(0).equals(dateListToValuesEntry.getKey())) {
            fieldTOValueEntry.setValue(dateListToValuesEntry.getValue());
            break;
          }
        }
      }
    }
    setDirectories(mvPruneDB, folderTmpl, fieldTOValue);

  }

  private void setDirectories(MVPruneDB mvPruneDB, String folderTmpl, Map<String, List<String>> fieldTOValue) {
    List<Map<String, String>> listOfCombinations = new ArrayList<>();
    if (fieldTOValue != null && folderTmpl != null) {
      createAllCombinations(fieldTOValue, new ArrayList<>(fieldTOValue.keySet()).listIterator(), new HashMap<>(), listOfCombinations);

      for (Map<String, String> combinationMap : listOfCombinations) {
        String directoryStr = folderTmpl;
        for (Map.Entry<String, String> combinationEntry : combinationMap.entrySet()) {
          directoryStr = directoryStr.replace("{" + combinationEntry.getKey() + "}", combinationEntry.getValue());
        }
        if (directoryStr.charAt(directoryStr.length() - 1) == '/') {
          directoryStr = directoryStr.substring(0, directoryStr.length() - 1);
        }
        mvPruneDB.getDirectories().add(directoryStr);
      }
    }
  }

  private void createAllCombinations(Map<String, List<String>> map, ListIterator<String> fieldsIterator, Map<String, String> combination, List<Map<String, String>> listOfCombinations) {
    // we're at a leaf node in the recursion tree, add solution to list
    if (!fieldsIterator.hasNext()) {
      listOfCombinations.add(new HashMap<>(combination));
    } else {
      String field = fieldsIterator.next();
      List<String> values = map.get(field);

      for (String value : values) {
        combination.put(field, value);
        createAllCombinations(map, fieldsIterator, combination, listOfCombinations);
        combination.remove(field);
      }

      fieldsIterator.previous();
    }
  }

  private Map<String, List<String>> readDirectoriesTemplates(Node foldersNode) {
    Map<String, List<String>> fieldToValue = new HashMap<>();
    NodeList loadValNodeList = foldersNode.getChildNodes();
    for (int i = 0; i < loadValNodeList.getLength(); i++) {
      Node loadValNode = loadValNodeList.item(i);
      if (loadValNode.getNodeType() == Node.ELEMENT_NODE && "field".equals(loadValNode.getNodeName())) {
        String name = getFieldName(loadValNode);
        NodeList listNodesList = loadValNode.getChildNodes();
        fieldToValue.put(name, new ArrayList<>());
        for (int h = 0; h < listNodesList.getLength(); h++) {
          Node listNode = listNodesList.item(h);
          if (listNode.getNodeType() == Node.ELEMENT_NODE && "val".equals(listNode.getNodeName())) {
            fieldToValue.get(name).add(listNode.getTextContent());
          } else if (listNode.getNodeType() == Node.ELEMENT_NODE && "date_list".equals(listNode.getNodeName())) {
            fieldToValue.get(name).add(getFieldName(listNode));
          }
        }
      }
    }
    return fieldToValue;
  }

  /**
   * gets field name by traversing Node's attributes and reading the value of 'name'
   *
   * @param fieldsNode
   * @return
   */
  private String getFieldName(Node fieldsNode) {
    String fieldName = null;
    NamedNodeMap fieldAtts = fieldsNode.getAttributes();
    for (int k = 0; k < fieldAtts.getLength(); k++) {
      if ("name".equals(fieldAtts.item(k).getNodeName())) {
        fieldName = fieldAtts.item(k).getNodeValue();
        break;
      }
    }
    return fieldName;
  }

  /**
   * Parses "files" element from the parameters file and sets corresponding filesNames in MVPruneDB obj.
   *
   * @param mvPruneDB
   * @param pruneSpecNode
   */
  private void setFiles(MVPruneDB mvPruneDB, Node pruneSpecNode) {
    NodeList filesNodeList = pruneSpecNode.getChildNodes();
    for (int j = 0; j < filesNodeList.getLength(); j++) {
      Node filesNode = filesNodeList.item(j);
      if (filesNode.getNodeType() == Node.ELEMENT_NODE && "file".equals(filesNode.getNodeName())) {
        mvPruneDB.getFiles().add(filesNode.getTextContent());
      }
    }
  }

  /**
   * Parses "fields" element from the parameters file and sets corresponding fields in MVPruneDB obj.
   *
   * @param mvPruneDB
   * @param pruneSpecNode
   */
  private void setFields(MVPruneDB mvPruneDB, Node pruneSpecNode) {
    NodeList fieldsNodeList = pruneSpecNode.getChildNodes();
    for (int j = 0; j < fieldsNodeList.getLength(); j++) {
      Node fieldsNode = fieldsNodeList.item(j);
      if (fieldsNode.getNodeType() == Node.ELEMENT_NODE && "field".equals(fieldsNode.getNodeName())) {
        String fieldName = getFieldName(fieldsNode);
        if (fieldName != null) {
          setFieldToValues(mvPruneDB, fieldsNode, fieldName);
        }
      }
    }
  }

  /**
   * Parses "field" element from the parameter file and sets corresponding fields in MVPruneDB obj.
   *
   * @param mvPruneDB
   * @param fieldsNode
   * @param fieldName
   */
  private void setFieldToValues(MVPruneDB mvPruneDB, Node fieldsNode, String fieldName) {
    NodeList fieldNodeList = fieldsNode.getChildNodes();
    for (int k = 0; k < fieldNodeList.getLength(); k++) {
      Node valueNode = fieldNodeList.item(k);
      if (valueNode.getNodeType() == Node.ELEMENT_NODE && "value_range".equals(valueNode.getNodeName())) {
        setRangeValues(mvPruneDB, fieldName, valueNode);
      } else if (valueNode.getNodeType() == Node.ELEMENT_NODE && "value_list".equals(valueNode.getNodeName())) {
        setListValues(mvPruneDB, fieldName, valueNode);
      }
    }
  }

  /**
   * Parses "value_list" element from the parameter file and sets corresponding fields in MVPruneDB obj. Specifies the list of pruned values for the field
   *
   * @param mvPruneDB
   * @param fieldName
   * @param valueNode
   */
  private void setListValues(MVPruneDB mvPruneDB, String fieldName, Node valueNode) {
    NodeList listNodesList = valueNode.getChildNodes();
    mvPruneDB.getFieldToListValues().put(fieldName, new ArrayList<>());
    for (int h = 0; h < listNodesList.getLength(); h++) {
      Node listNode = listNodesList.item(h);
      if (listNode.getNodeType() == Node.ELEMENT_NODE && "value".equals(listNode.getNodeName())) {
        mvPruneDB.getFieldToListValues().get(fieldName).add(listNode.getTextContent());
      }
    }
  }

  /**
   * Parses "value_range" element from the parameter file and sets corresponding fields in MVPruneDB obj. Specifies min and max value of the field threshold
   *
   * @param mvPruneDB
   * @param fieldName
   * @param valueNode
   */
  private void setRangeValues(MVPruneDB mvPruneDB, String fieldName, Node valueNode) {
    NodeList rangeNodesList = valueNode.getChildNodes();
    mvPruneDB.getFieldToRangeValues().put(fieldName, new ArrayList<>(2));
    for (int h = 0; h < rangeNodesList.getLength(); h++) {
      Node rangeNode = rangeNodesList.item(h);
      if (rangeNode.getNodeType() == Node.ELEMENT_NODE && "start".equals(rangeNode.getNodeName())) {
        mvPruneDB.getFieldToRangeValues().get(fieldName).add(0, rangeNode.getTextContent());
      } else if (rangeNode.getNodeType() == Node.ELEMENT_NODE && "end".equals(rangeNode.getNodeName())) {
        mvPruneDB.getFieldToRangeValues().get(fieldName).add(1, rangeNode.getTextContent());
      }
    }
  }

  /**
   * initialises database connection properties from <connection> element
   *
   * @param mvPruneDB
   * @param pruneSpecNode
   */
  private void setDbConnection(MVPruneDB mvPruneDB, Node pruneSpecNode) {
    NodeList connectionNodeList = pruneSpecNode.getChildNodes();
    for (int j = 0; j < connectionNodeList.getLength(); j++) {
      Node connectionNode = connectionNodeList.item(j);
      if (connectionNode.getNodeType() == Node.ELEMENT_NODE) {
        if ("host".equals(connectionNode.getNodeName())) {
          mvPruneDB.setHost(connectionNode.getTextContent());
        } else if ("database".equals(connectionNode.getNodeName())) {
          mvPruneDB.setDatabaseName(connectionNode.getTextContent());
        } else if ("user".equals(connectionNode.getNodeName())) {
          mvPruneDB.setUser(connectionNode.getTextContent());
        } else if ("password".equals(connectionNode.getNodeName())) {
          mvPruneDB.setPwd(connectionNode.getTextContent());
        }
      }
    }
  }

  /**
   * gets the value of <info_only> element and if it is valid initialises it
   *
   * @param mvPruneDB
   * @param pruneSpecNode
   */
  private void setInfo(MVPruneDB mvPruneDB, Node pruneSpecNode) {
    String isInfoStr = pruneSpecNode.getTextContent();
    if ("true".equalsIgnoreCase(isInfoStr) || "false".equalsIgnoreCase(isInfoStr)) {
      mvPruneDB.setInfoOnly(Boolean.valueOf(isInfoStr));
    }

  }

}
