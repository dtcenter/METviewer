/**
 * XmlParser.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.ValidationException;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import edu.ucar.metviewer.scorecard.model.WorkingFolders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/12/16 15:13 $
 */
class XmlParser {

  private static final Logger logger = LogManager.getLogger("XmlParser");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");


  public Scorecard parseParameters(String filename) throws ValidationException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    Scorecard scorecard = new Scorecard();
    try {
      dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
      dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbf.setValidating(true);

      db = dbf.newDocumentBuilder();
      db.setErrorHandler(null);

      filename = MVUtil.cleanString(filename);
      Document doc = db.parse(new File(filename));
      Node pruneSpec = doc.getFirstChild();
      NodeList scorecardSpecNodes = pruneSpec.getChildNodes();
      for (int i = 0; i < scorecardSpecNodes.getLength(); i++) {
        Node scorecardSpecNode = scorecardSpecNodes.item(i);
        //for each node
        if (scorecardSpecNode.getNodeType() == Node.ELEMENT_NODE && "connection".equals(
                scorecardSpecNode.getNodeName())) {
          setDbConnection(scorecard, scorecardSpecNode);
        } else if (scorecardSpecNode.getNodeType() == Node.ELEMENT_NODE && "folders".equals(
                scorecardSpecNode.getNodeName())) {
          setFolders(scorecard, scorecardSpecNode);
        } else if (scorecardSpecNode.getNodeType() == Node.ELEMENT_NODE && "plot".equals(
                scorecardSpecNode.getNodeName())) {
          setPlot(scorecard, scorecardSpecNode);
          if (scorecard.getViewSymbol() && scorecard.getStat() == null && scorecard.getStatSymbol() == null){
            throw new ValidationException("XML ERROR - Provide the statistic for symbols");
          }

          if (scorecard.getViewSymbol() && scorecard.getStat() != null && scorecard.getStatSymbol() != null){
            throw new ValidationException("XML ERROR - Ambiguous statistic for symbols.Provide <stat> or <stat_symbol> but not both");
          }

          if (scorecard.getViewValue() && scorecard.getStat() == null && scorecard.getStatValue() == null){
            throw new ValidationException("XML ERROR - Provide the statistic for values");
          }
          if (scorecard.getViewValue() && scorecard.getStat() != null && scorecard.getStatValue() != null){
            throw new ValidationException("XML ERROR - Ambiguous the statistic for values. Provide <stat> or <stat_value> but not both");
          }

          //init stats for number and symbol if they don't exist
          if(scorecard.getStat() != null && scorecard.getStatValue() == null){
            scorecard.setStatValue(scorecard.getStat());
          }
          if(scorecard.getStat() != null && scorecard.getStatSymbol() == null){
            scorecard.setStatSymbol(scorecard.getStat());
          }


          //if do not display symbols - set stat for symbols to null
          if( !scorecard.getViewSymbol() ){
            scorecard.setStatSymbol(null);
          }

        } else if (scorecardSpecNode.getNodeType() == Node.ELEMENT_NODE && "rscript".equals(
                scorecardSpecNode.getNodeName())) {
          scorecard.setrScriptCommand(scorecardSpecNode.getTextContent());
        }

      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      logger.info("ERROR during reading XML file : " + e.getMessage());
      logger.error(ERROR_MARKER, e.getMessage());
    }
    return scorecard;
  }

  private void setPlot(Scorecard scorecard, Node scorecardSpecNode) {
    NodeList plotNodeList = scorecardSpecNode.getChildNodes();
    for (int j = 0; j < plotNodeList.getLength(); j++) {
      Node plotNode = plotNodeList.item(j);
      if (plotNode.getNodeType() == Node.ELEMENT_NODE) {
        if ("template".equals(plotNode.getNodeName())) {
          scorecard.setrTemplate(plotNode.getTextContent());
        } else if ("plot_fix".equals(plotNode.getNodeName())) {
          scorecard.setFixedVars(constructFields(plotNode));
        } else if ("rows".equals(plotNode.getNodeName())) {
          scorecard.setRows(constructFields(plotNode));
        } else if ("columns".equals(plotNode.getNodeName())) {
          scorecard.setColumns(constructFields(plotNode));
        } else if ("agg_stat".equals(plotNode.getNodeName())) {
          if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.TRUE))) {
            scorecard.setAggStat(Boolean.TRUE);
          } else if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
            scorecard.setAggStat(Boolean.FALSE);
          }
        } else if ("boot_repl".equals(plotNode.getNodeName())) {
          try {
            scorecard.setNumBootReplicates(Integer.parseInt(plotNode.getTextContent()));
          } catch (NumberFormatException e) {
            logger.info("Incorrect value for <boot_repl> :"
                    + plotNode.getTextContent() + ". Using default value 1000");
          }
        } else if ("boot_random_seed".equals(plotNode.getNodeName())) {
          try {
            scorecard.setBootRandomSeed(Integer.valueOf(plotNode.getTextContent()));
          } catch (NumberFormatException e) {
            logger.info("Incorrect value for <boot_random_seed> :"
                    + plotNode.getTextContent() + ". Using default value NULL");
          }
        } else if ("plot_stat".equals(plotNode.getNodeName())) {
          scorecard.setPlotStat(plotNode.getTextContent());
        } else if ("tmpl".equals(plotNode.getNodeName())) {
          setTmpl(scorecard, plotNode);
        } else if ("view_value".equals(plotNode.getNodeName())) {
          if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.TRUE))) {
            scorecard.setViewValue(Boolean.TRUE);
          } else if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
            scorecard.setViewValue(Boolean.FALSE);
          }
        } else if ("view_symbol".equals(plotNode.getNodeName())) {
          if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.TRUE))) {
            scorecard.setViewSymbol(Boolean.TRUE);
          } else if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
            scorecard.setViewSymbol(Boolean.FALSE);
          }
        } else if ("view_legend".equals(plotNode.getNodeName())) {
          if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.TRUE))) {
            scorecard.setViewLegend(Boolean.TRUE);
          } else if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
            scorecard.setViewLegend(Boolean.FALSE);
          }
        } else if ("execution_type".equals(plotNode.getNodeName())) {
          scorecard.setExecutionType(plotNode.getTextContent());

        } else if ("stat_flag".equals(plotNode.getNodeName())) {
          scorecard.setStatFlag(plotNode.getTextContent());

        } else if ("stat_value".equals(plotNode.getNodeName())) {
          scorecard.setStatValue(plotNode.getTextContent());

        } else if ("stat_symbol".equals(plotNode.getNodeName())) {
          scorecard.setStatSymbol(plotNode.getTextContent());

        } else if ("stat".equals(plotNode.getNodeName())) {
          if (plotNode.getTextContent().equals("DIFF")
                  || plotNode.getTextContent().equals("DIFF_SIG")
                  || plotNode.getTextContent().equals("SINGLE")) {
            scorecard.setStat(plotNode.getTextContent());
          }
        } else if ("threshold_file".equals(plotNode.getNodeName())) {
          scorecard.setThresholdFile(plotNode.getTextContent());
        } else if ("weight_file".equals(plotNode.getNodeName())) {
          scorecard.setWeightFile(plotNode.getTextContent());
        } else if ("printSQL".equals(plotNode.getNodeName())) {
          if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.TRUE))) {
            scorecard.setPrintSQL(Boolean.TRUE);
          } else if (plotNode.getTextContent().equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
            scorecard.setPrintSQL(Boolean.FALSE);
          }
        } else if ("left_column_names".equals(plotNode.getNodeName())) {
          NodeList leftColumnNamesList = plotNode.getChildNodes();
          for (int k = 0; k < leftColumnNamesList.getLength(); k++) {
            Node columnNameNode = leftColumnNamesList.item(k);
            if (columnNameNode.getNodeType() == Node.ELEMENT_NODE
                    && "val".equals(columnNameNode.getNodeName())) {
              scorecard.setLeftColumnsNames(columnNameNode.getTextContent().trim());
            }
          }
        } else if ("symbol_size".equals(plotNode.getNodeName())) {
          scorecard.setSymbolSize(plotNode.getTextContent());
        }

      }

    }

  }

  private void setTmpl(Scorecard scorecard, Node node) {
    NodeList tmplNodeList = node.getChildNodes();
    for (int j = 0; j < tmplNodeList.getLength(); j++) {
      Node tmplNode = tmplNodeList.item(j);
      if (tmplNode.getNodeType() == Node.ELEMENT_NODE) {
        if ("data_file".equals(tmplNode.getNodeName())) {
          scorecard.setDataFile(tmplNode.getTextContent());
        } else if ("plot_file".equals(tmplNode.getNodeName())) {
          scorecard.setPlotFile(tmplNode.getTextContent());
        } else if ("title".equals(tmplNode.getNodeName())) {
          scorecard.setTitle(tmplNode.getTextContent());
        }
      }
    }
  }

  private List<Field> constructFields(Node plotNode) {
    List<Field> fieldList = new ArrayList<>();
    NodeList fixedNodeList = plotNode.getChildNodes();
    for (int j = 0; j < fixedNodeList.getLength(); j++) {
      Node fixedNode = fixedNodeList.item(j);
      if (fixedNode.getNodeType() == Node.ELEMENT_NODE && "field".equals(fixedNode.getNodeName())) {
        fieldList.add(constructField(fixedNode));
      }
    }
    return fieldList;
  }

  private Field constructField(Node fieldNode) {
    Field field = new Field();
    field.setName(getFieldName(fieldNode));
    field.setLabel(getFieldLabel(fieldNode));
    List<Entry> values = new ArrayList<>();
    List<Field> fields = new ArrayList<>();
    NodeList fieldNodeList = fieldNode.getChildNodes();
    for (int j = 0; j < fieldNodeList.getLength(); j++) {
      Node fNode = fieldNodeList.item(j);
      if (fNode.getNodeType() == Node.ELEMENT_NODE) {
        if ("val".equals(fNode.getNodeName())) {
          Entry entry = new Entry();
          entry.setName(getFieldName(fNode));
          entry.setLabel(getFieldLabel(fNode));
          values.add(entry);
        } else if ("field".equals(fNode.getNodeName())) {
          fields.add(constructField(fNode));
        }
      }
    }
    field.setValues(values);
    field.setFields(fields);
    return field;
  }

  private String getFieldName(Node fieldsNode) {
    String fieldName = null;
    NamedNodeMap fieldAtts = fieldsNode.getAttributes();
    for (int k = 0; k < fieldAtts.getLength(); k++) {
      if ("name".equals(fieldAtts.item(k).getNodeName())) {
        //remove all spaces
        fieldName = fieldAtts.item(k).getNodeValue();

        if (!fieldName.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
          fieldName = fieldName.replaceAll("\\s+", "");
        }
        break;
      }
    }
    return fieldName;
  }

  private String getFieldLabel(Node fieldsNode) {
    String fieldLabel = null;
    NamedNodeMap fieldAtts = fieldsNode.getAttributes();
    for (int k = 0; k < fieldAtts.getLength(); k++) {
      if ("label".equals(fieldAtts.item(k).getNodeName())) {
        fieldLabel = fieldAtts.item(k).getNodeValue();
        break;
      }
    }
    return fieldLabel;
  }

  private void setFolders(Scorecard scorecard, Node scorecardSpecNode) {
    WorkingFolders workingFolders = new WorkingFolders();

    NodeList foldersNodeList = scorecardSpecNode.getChildNodes();
    for (int j = 0; j < foldersNodeList.getLength(); j++) {
      Node folderNode = foldersNodeList.item(j);
      if (folderNode.getNodeType() == Node.ELEMENT_NODE) {
        if ("r_tmpl".equals(folderNode.getNodeName())) {
          workingFolders.setrTemplateDir(folderNode.getTextContent());
        } else if ("r_work".equals(folderNode.getNodeName())) {
          workingFolders.setrWorkDir(folderNode.getTextContent());
        } else if ("plots".equals(folderNode.getNodeName())) {
          workingFolders.setPlotsDir(folderNode.getTextContent());
        } else if ("data".equals(folderNode.getNodeName())) {
          workingFolders.setDataDir(folderNode.getTextContent());
        } else if ("scripts".equals(folderNode.getNodeName())) {
          workingFolders.setScriptsDir(folderNode.getTextContent());
        }
      }
    }
    scorecard.setWorkingFolders(workingFolders);
  }

  /**
   * initialises database connection properties from <connection> element
   *
   * @param scorecard
   * @param scorecardSpecNode
   */

  private void setDbConnection(Scorecard scorecard, Node scorecardSpecNode) {
    NodeList connectionNodeList = scorecardSpecNode.getChildNodes();
    for (int j = 0; j < connectionNodeList.getLength(); j++) {
      Node connectionNode = connectionNodeList.item(j);
      if (connectionNode.getNodeType() == Node.ELEMENT_NODE) {
        if ("host".equals(connectionNode.getNodeName())) {
          scorecard.setHost(connectionNode.getTextContent());
        } else if ("database".equals(connectionNode.getNodeName())) {
          String[] databases = connectionNode.getTextContent().trim().split(",");
          List<String> databasesList = new ArrayList<>();
          for (String database : databases) {
            databasesList.add(database.trim());
          }
          scorecard.setDatabaseNames(databasesList);
        } else if ("user".equals(connectionNode.getNodeName())) {
          scorecard.setUser(connectionNode.getTextContent());
        } else if ("password".equals(connectionNode.getNodeName())) {
          scorecard.setPwd(connectionNode.getTextContent());
        }
      }
    }
  }

}
