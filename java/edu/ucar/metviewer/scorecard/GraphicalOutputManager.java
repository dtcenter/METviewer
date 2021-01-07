/**
 * GraphicalOutputManager.java Copyright UCAR (c) 2016. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.ucar.metviewer.BoundedBufferedReader;
import edu.ucar.metviewer.scorecard.exceptions.MissingFileException;
import edu.ucar.metviewer.scorecard.html2image.HtmlImageGenerator;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import edu.ucar.metviewer.scorecard.model.LegendRange;
import edu.ucar.metviewer.scorecard.model.WeightRequirements;
import j2html.tags.ContainerTag;
import j2html.tags.UnescapedText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static j2html.TagCreator.*;


/**
 * @author : tatiana $
 * @version : 1.0 : 19/12/16 15:23 $
 */
class GraphicalOutputManager {

  private static final String WHITE_FFFFFF = "#FFFFFF";
  private static final String BLACK_000000 = "#000000";

  private static final Logger logger = LogManager.getLogger("GraphicalOutputManager");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private static final String CSS
          = "table {border-collapse: collapse;border-spacing: 0;}"
          + "table, th, td {border: 1px solid black;text-align:center;}"
          + "th {color: red;}"
          + "#thside {color: blue;}"
          + ".title1 {width:100%; text-align:center; color:red; font-size:18px; padding-top: 10px;}"
          + ".title2 {width:100%; text-align:center; color:black; font-size:12px;padding-bottom: 10px;}"
          + ".legendTable {margin-top:15px;margin-bottom:10px;}"
          + ".weightsTable {margin-top:15px;}"
          + ".legendText {text-align:left;}";
  public static final String CLASS = "class";
  public static final String STYLE = "style";


  private final ContainerTag html;
  private final ContainerTag title1;
  private final ContainerTag title2;
  private final ContainerTag title3;
  private final ContainerTag htmlTable = table().attr("align", "center");
  private final ContainerTag htmlBody = body();
  private List<LegendRange> rangeList;
  private List<Map<String, Integer>> rowFieldToCountMap;
  private final List<Map<String, Entry>> listRows;
  private final List<Map<String, Entry>> listColumns;
  private final String dataFileStr;
  private final String plotFileStr;
  private final boolean viewSymbol;
  private final boolean viewValue;
  private final boolean viewLegend;
  private final String diffStatValue;
  private final String diffStatSymbol;
  private final List<String> models = new ArrayList<>();
  private final List<String> leftColumnsNames;
  private final String symbolSize;
  private WeightRequirements weightRequirements = null;
  private final SortedMap<Double, String> weightToColor = new TreeMap<>(Collections.reverseOrder());


  public GraphicalOutputManager(final Scorecard scorecard) {
    html = html();
    title1 = div().attr(CLASS, "title1");
    title2 = div().attr(CLASS, "title2");
    title3 = div().attr(CLASS, "title2");
    //add head
    html.with(head().with(style().attr("type", "text/css").with(text(CSS))));

    //create range list
    initRangeList(scorecard.getThresholdFile());
    initWeightRequirements(scorecard.getWeightFile());
    dataFileStr = scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile();
    listRows = scorecard.getListOfEachRowWithDesc();
    listColumns = scorecard.getListOfEachColumnWithDesc();
    title1.withText(scorecard.getTitle());
    viewSymbol = scorecard.getViewSymbol();
    viewValue = scorecard.getViewValue();
    viewLegend = scorecard.getViewLegend();
    plotFileStr = scorecard.getWorkingFolders().getPlotsDir() + scorecard.getPlotFile();
    leftColumnsNames = scorecard.getLeftColumnsNames();
    diffStatValue = scorecard.getStatValue();
    diffStatSymbol = scorecard.getStatSymbol();
    symbolSize = scorecard.getSymbolSize();
    List<String> ranges = new ArrayList<>();

    for (Field fixField : scorecard.getFixedVars()) {
      if ("fcst_init_beg".equals(fixField.getName())
              || "fcst_valid_beg".equals(fixField.getName())) {

        if (fixField.getLabel() != null) {
          ranges.add(fixField.getLabel());
        } else {
          int fixedFieldValsSize = fixField.getValues().size();
          boolean isSizeEven = fixedFieldValsSize % 2 == 0;
          if (isSizeEven) {
            for (int i = 0; i < fixedFieldValsSize; i = i + 2) {
              String line = fixField.getValues().get(i).getLabel()
                      + " - " + fixField.getValues().get(i + 1).getLabel();
              if (i < fixedFieldValsSize - 2) {
                line = line + ",";
              }
              ranges.add(line);
            }
          } else {
            for (int i = 0; i < fixedFieldValsSize - 1; i = i + 2) {
              ranges.add(fixField.getValues().get(i).getLabel()
                      + " - " + fixField.getValues().get(i + 1).getLabel()
                      + ", ");

            }
            ranges.add(fixField.getValues().get(fixedFieldValsSize - 1).getName());
          }
        }


      } else if ("model".equals(fixField.getName())) {
        for (Entry entry : fixField.getValues()) {
          models.add(entry.getLabel());
        }

      }
    }
    StringBuilder titleText = new StringBuilder("for ");
    for (int i = 0; i < this.models.size(); i = i + 2) {
      if (i + 1 < this.models.size()) {
        if (i > 0) {
          titleText.append(", ");
        }
        titleText.append(models.get(i));
        if (scorecard.getStatSymbol() != null && !scorecard.getStatSymbol().equals("SINGLE") && !scorecard.getStatValue().equals("SINGLE")) {
          titleText.append(" and ").append(models.get(i + 1));
        }

      }
    }
    title2.withText(titleText.toString());
    for (String range : ranges) {
      title3.with(div().withText(range));
    }
  }

  private void initWeightRequirements(final String weightRequirementsFile) {
    if (weightRequirementsFile != null) {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db;
      try {
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setValidating(true);

        db = dbf.newDocumentBuilder();
        db.setErrorHandler(null);

        Document doc = db.parse(new File(weightRequirementsFile));
        Node weightRequirementsNode = doc.getFirstChild();
        weightRequirements = new WeightRequirements(weightRequirementsNode.getAttributes().item(0).getNodeValue());
        List<WeightRequirements.Weight> weightList = new ArrayList<>();
        weightRequirements.setWeightList(weightList);

        NodeList weightNodeList = weightRequirementsNode.getChildNodes();
        for (int j = 0; j < weightNodeList.getLength(); j++) {
          Node weightNode = weightNodeList.item(j);
          if (weightNode.getNodeType() == Node.ELEMENT_NODE &&
                  "Weight".equals(weightNode.getNodeName())) {
            double weightValue = Double.parseDouble(weightNode.getAttributes().getNamedItem("weight").getNodeValue());

            List<WeightRequirements.Criteria> criteriaList = new ArrayList<>();
            WeightRequirements.Weight weight = new WeightRequirements.Weight(weightValue, criteriaList);
            NodeList criteriaNodeList = weightNode.getChildNodes();
            for (int i = 0; i < criteriaNodeList.getLength(); i++) {
              Node criteriaNode = criteriaNodeList.item(i);
              if (criteriaNode.getNodeType() == Node.ELEMENT_NODE &&
                      "Criteria".equals(criteriaNode.getNodeName())) {
                NamedNodeMap attributes = criteriaNode.getAttributes();
                String field = "";
                String value = "";
                for (int k = 0; k < attributes.getLength(); k++) {
                  if ("field".equals(attributes.item(k).getNodeName())) {
                    field = attributes.item(k).getNodeValue();
                  } else if ("value".equals(attributes.item(k).getNodeName())) {
                    value = attributes.item(k).getNodeValue();
                  }
                }
                criteriaList.add(new WeightRequirements.Criteria(field, value));
              }
            }
            weightList.add(weight);
          } else if (weightNode.getNodeType() == Node.ELEMENT_NODE &&
                  "WeightToColor".equals(weightNode.getNodeName())) {
            Double weightValue = Double.parseDouble(weightNode.getAttributes().getNamedItem("weight").getNodeValue());
            String color = String.valueOf(weightNode.getAttributes().getNamedItem("color").getNodeValue());
            weightToColor.put(weightValue, color);
          }
        }


      } catch (ParserConfigurationException | IOException | SAXException e) {
        logger.info("ERROR during reading weightRequirements XML file : " + e.getMessage());
        logger.error(ERROR_MARKER, e.getMessage());
        rangeList.clear();

      }
    }
  }

  private void initRangeList(final String thresholdFile) {
    rangeList = new ArrayList<>();
    if (thresholdFile != null) {

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db;

      try {
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setValidating(true);

        db = dbf.newDocumentBuilder();
        db.setErrorHandler(null);

        Document doc = db.parse(new File(thresholdFile));
        Node pruneSpec = doc.getFirstChild();
        NodeList ranges = pruneSpec.getChildNodes();
        for (int i = 0; i < ranges.getLength(); i++) {
          Node node = ranges.item(i);

          if (node.getNodeType() == Node.ELEMENT_NODE) {
            NodeList legendRangeProperties = node.getChildNodes();
            LegendRange legendRange = new LegendRange();

            for (int j = 0; j < legendRangeProperties.getLength(); j++) {
              Node propertyNode = legendRangeProperties.item(j);

              if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {

                if ("symbol".equals(propertyNode.getNodeName())) {
                  legendRange.setSymbol(propertyNode.getTextContent());
                } else if ("color".equals(propertyNode.getNodeName())) {
                  legendRange.setColor(propertyNode.getTextContent());
                } else if ("background".equals(propertyNode.getNodeName())) {
                  legendRange.setBackground(propertyNode.getTextContent());
                } else if ("lower_limit".equals(propertyNode.getNodeName())) {
                  legendRange.setLowerLimit(new BigDecimal(propertyNode.getTextContent()));
                } else if ("upper_limit".equals(propertyNode.getNodeName())) {
                  legendRange.setUpperLimit(new BigDecimal(propertyNode.getTextContent()));
                } else if ("include_lower_limit".equals(propertyNode.getNodeName())) {
                  if (propertyNode.getTextContent()
                          .equalsIgnoreCase(String.valueOf(Boolean.TRUE))) {
                    legendRange.setIncludeLowerLimit(Boolean.TRUE);
                  } else if (propertyNode.getTextContent()
                          .equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
                    legendRange.setIncludeLowerLimit(Boolean.FALSE);
                  }
                } else if ("include_upper_limit".equals(propertyNode.getNodeName())) {
                  if (propertyNode.getTextContent()
                          .equalsIgnoreCase(String.valueOf(Boolean.TRUE))) {
                    legendRange.setIncludeUpperLimit(Boolean.TRUE);
                  } else if (propertyNode.getTextContent()
                          .equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
                    legendRange.setIncludeUpperLimit(Boolean.FALSE);
                  }
                } else if ("format_string".equals(propertyNode.getNodeName())) {
                  legendRange.setFormatString(propertyNode.getTextContent());
                }
              }

            }
            rangeList.add(legendRange);

          }
        }
      } catch (ParserConfigurationException | IOException | SAXException e) {
        logger.info("ERROR during reading threshold XML file : " + e.getMessage());
        logger.info("Default threshold configurations will be used");
        logger.error(ERROR_MARKER, e.getMessage());
        rangeList.clear();

      }
    }
    if (rangeList.isEmpty()) {
      LegendRange legendRange = new LegendRange();
      legendRange.setSymbol("&#9650;");//UP-POINTING TRIANGLE
      legendRange.setColor("#009120");//green
      legendRange.setBackground(WHITE_FFFFFF);//white
      legendRange.setLowerLimit(BigDecimal.valueOf(0.999));
      legendRange.setUpperLimit(BigDecimal.valueOf(1));
      legendRange.setIncludeLowerLimit(Boolean.TRUE);
      legendRange.setIncludeUpperLimit(Boolean.TRUE);
      legendRange.setFormatString("%s is better than %s at the 99.9%% significance level");
      rangeList.add(legendRange);

      legendRange = new LegendRange();
      legendRange.setSymbol("&#9652;");//UP-POINTING SMALL TRIANGLE
      legendRange.setColor("#009120");//green
      legendRange.setBackground(WHITE_FFFFFF);//white
      legendRange.setLowerLimit(BigDecimal.valueOf(0.99));
      legendRange.setUpperLimit(BigDecimal.valueOf(0.999));
      legendRange.setIncludeLowerLimit(Boolean.TRUE);
      legendRange.setIncludeUpperLimit(Boolean.FALSE);
      legendRange.setFormatString("%s is better than %s at the 99%% significance level");
      rangeList.add(legendRange);

      legendRange = new LegendRange();
      legendRange.setColor(BLACK_000000);//black
      legendRange.setBackground("#A9F5A9");//green
      legendRange.setLowerLimit(BigDecimal.valueOf(0.95));
      legendRange.setUpperLimit(BigDecimal.valueOf(0.99));
      legendRange.setIncludeLowerLimit(Boolean.TRUE);
      legendRange.setIncludeUpperLimit(Boolean.FALSE);
      legendRange.setFormatString("%s is better than %s at the 95%% significance level");
      rangeList.add(legendRange);

      legendRange = new LegendRange();
      legendRange.setColor(BLACK_000000);//black
      legendRange.setBackground("#BDBDBD");//grey
      legendRange.setLowerLimit(BigDecimal.valueOf(-0.95));
      legendRange.setUpperLimit(BigDecimal.valueOf(0.95));
      legendRange.setIncludeLowerLimit(Boolean.FALSE);
      legendRange.setIncludeUpperLimit(Boolean.FALSE);
      legendRange.setFormatString("No statistically significant difference between %s and %s");
      rangeList.add(legendRange);


      legendRange = new LegendRange();
      legendRange.setColor(BLACK_000000);//black
      legendRange.setBackground("#F5A9BC");//pink
      legendRange.setLowerLimit(BigDecimal.valueOf(-0.99));
      legendRange.setUpperLimit(BigDecimal.valueOf(-0.95));
      legendRange.setIncludeLowerLimit(Boolean.FALSE);
      legendRange.setIncludeUpperLimit(Boolean.TRUE);
      legendRange.setFormatString("%s is worse than %s at the 95%% significance level");
      rangeList.add(legendRange);

      legendRange = new LegendRange();
      legendRange.setSymbol("&#9662;");//DOWN-POINTING SMALL TRIANGLE
      legendRange.setColor("#FF0000");//red
      legendRange.setBackground(WHITE_FFFFFF);//white
      legendRange.setLowerLimit(BigDecimal.valueOf(-0.999));
      legendRange.setUpperLimit(BigDecimal.valueOf(-0.99));
      legendRange.setIncludeLowerLimit(Boolean.FALSE);
      legendRange.setIncludeUpperLimit(Boolean.TRUE);
      legendRange.setFormatString("%s is worse than %s at the 99%% significance level");
      rangeList.add(legendRange);

      legendRange = new LegendRange();
      legendRange.setSymbol("&#9660;");//DOWN-POINTING  TRIANGLE
      legendRange.setColor("#FF0000");//red
      legendRange.setBackground(WHITE_FFFFFF);//white
      legendRange.setLowerLimit(BigDecimal.valueOf(-1));
      legendRange.setUpperLimit(BigDecimal.valueOf(-0.999));
      legendRange.setIncludeLowerLimit(Boolean.TRUE);
      legendRange.setIncludeUpperLimit(Boolean.TRUE);
      legendRange.setFormatString("%s is worse than %s at the 99.9%% significance level");
      rangeList.add(legendRange);

      legendRange = new LegendRange();
      legendRange.setColor(BLACK_000000);//black
      legendRange.setBackground("#58ACFA");//blue
      legendRange.setIncludeLowerLimit(Boolean.FALSE);
      legendRange.setIncludeUpperLimit(Boolean.FALSE);
      legendRange.setFormatString("Not statistically relevant");
      rangeList.add(legendRange);
    }

  }

  public void createGraphics() throws IOException, MissingFileException {
    File dataFile = new File(dataFileStr);
    if (dataFile.exists()) {
      ArrayNode table = readFileToJsonTable(dataFile);


      //calculate rowspans for row headers
      rowFieldToCountMap = getRowspansForRowHeader();


      //create and add table header
      htmlTable.with(createTableHead());


      //create and add table body
      htmlTable.with(createHtmlTableBody(table));

      //add all html tags together
      htmlBody.with(title1).with(title2).with(title3).with(htmlTable);
      if (!weightToColor.isEmpty()){
        htmlBody.with(createHtmlWeightLegend());
      }
      if (viewLegend) {
        htmlBody.with(createHtmlLegend());
      }
      String htmlPageStr = html.with(htmlBody).render();

      //create  HTML file
      String htmlFileName = plotFileStr.replaceAll(".png", ".html").replaceAll(".jpeg", ".html");
      try (PrintWriter out = new PrintWriter(htmlFileName)) {
        out.println(htmlPageStr);
        out.flush();
      }

      //create an image

      System.setProperty("java.awt.headless", "true");
      HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
      imageGenerator.loadHtml(htmlPageStr);
      imageGenerator.saveAsImage(plotFileStr);

      logger.info("Image was saved to " + plotFileStr);
    } else {
      throw new MissingFileException(dataFile.getAbsolutePath());
    }
  }

  private ContainerTag createHtmlWeightLegend() {
    ContainerTag legendTable = table().attr(CLASS, "weightsTable").attr("align", "center");
    // create title
    ContainerTag tr = tr();
    ContainerTag td = td().attr("colspan", String.valueOf(weightToColor.size()))
            .with(new UnescapedText("Weights"));
    tr.with(td);
    legendTable.with(tr);

    tr = tr();
    for (Map.Entry<Double, String> entry: weightToColor.entrySet()){
      td = td().attr(STYLE, "border-color:" + entry.getValue() + ";" + "border-width:4px;")
              .with(new UnescapedText(String.valueOf(entry.getKey())));
      tr.with(td);
    }
    legendTable.with(tr);
    return legendTable;
  }

  private ContainerTag createHtmlLegend() {
    ContainerTag legendTable = table().attr(CLASS, "legendTable").attr("align", "center");
    for (LegendRange range : rangeList) {
      ContainerTag td1 = td().attr(STYLE, "color:" + range.getColor() + ";"
              + "background:" + range.getBackground() + ";")
              .with(new UnescapedText(range.getSymbol()));
      ContainerTag td2 = td().attr(CLASS, "legendText");

      try {
        if (this.models.size() == 2) {
          td2.with(new UnescapedText(String.format(range.getFormatString(), this.models.get(0), this.models.get(1))));
        } else {
          td2.with(new UnescapedText(String.format(range.getFormatString(), "1st model", "2nd model")));
        }
      } catch (FormatFlagsConversionMismatchException f) {
        logger.error("Error during printing the legend text for " + range.getFormatString());
        logger.error("Escaped percent sign is double percent (%%)");
        td2.with(new UnescapedText("&nbsp;"));
        logger.debug(f);
      }

      legendTable.with(tr().with(td1).with(td2));
    }
    //add one more line describing the stats for symbols and numbers
    StringBuilder description = new StringBuilder();
    if (viewSymbol) {
      description.append("Statistic for symbols: ").append(diffStatSymbol);
    }
    if (viewValue) {
      if (description.length() > 0) {
        description.append(" , ");
      }
      description.append("Statistic for values: ").append(diffStatValue);
    }

    ContainerTag td = td().attr(CLASS, "legendText").attr("colspan", "2")
            .attr(STYLE, "text-align:center;")
            .with(new UnescapedText(description.toString()));

    legendTable.with(tr().with(td));

    return legendTable;
  }

  private ContainerTag createHtmlTableBody(final ArrayNode table) {
    ContainerTag tBody = tbody();
    //for each data row
    for (int rowCounter = 0; rowCounter < listRows.size(); rowCounter++) {
      ContainerTag htmlTr = tr();

      createRowHeader(rowCounter, htmlTr);
      for (int j = 0; j < this.models.size(); j = j + 2) {
        String model1 = this.models.get(j);
        String model2 = null;
        if (j + 1 < this.models.size()) {
          model2 = this.models.get(j + 1);
        }
        //for each data column
        for (Map<String, Entry> mapColumn : listColumns) {
          Map<String, Entry> cellFieldsValues = new HashMap<>(listRows.get(rowCounter));
          cellFieldsValues.putAll(mapColumn);
          int index = -1;
          boolean isCellCreated = false;
          // find the corresponding value in the JSON table and create a cell
          BigDecimal valueForSymbol = BigDecimal.valueOf(-9999);
          BigDecimal valueForNumber = BigDecimal.valueOf(-9999);
          WeightRequirements.Weight satisfied = null;
          JsonNode cellNode = null;


          for (int i = 0; i < table.size(); i++) {

            JsonNode node = table.get(i);
            if (node.get("model1").asText().equals(model1) &&
                    node.get("model2").asText().equals(model2)) {

              if (weightRequirements != null && isJsonRowMatch(cellFieldsValues, node)) {

                for (WeightRequirements.Weight weight : weightRequirements.getWeightList()) {
                  boolean isSatisfied = true;
                  for (WeightRequirements.Criteria criteria : weight.getCriteriaList()) {
                    if (node.has(criteria.getField())) {
                      if (!node.get(criteria.getField()).asText().equals(criteria.getValue())) {
                        isSatisfied = false;
                        break;
                      }
                    }
                  }
                  if (isSatisfied) {
                    satisfied = weight;
                    break;
                  }
                }
              }


              if (viewSymbol) {
                boolean isMatch = isJsonRowMatchSymbol(cellFieldsValues, node);
                if (isMatch && !"NA".equals(node.findValue("stat_value").asText())) {
                  try {
                    valueForSymbol = new BigDecimal(node.findValue("stat_value").asText());
                    valueForSymbol = valueForSymbol.setScale(3, RoundingMode.HALF_UP);
                  } catch (NumberFormatException e) {
                    logger.error(e);
                  }
                }
              }
              if (viewValue) {
                boolean isMatch = isJsonRowMatchNumber(cellFieldsValues, node);
                if (isMatch && !"NA".equals(node.findValue("stat_value").asText())) {
                  try {
                    valueForNumber = new BigDecimal(node.findValue("stat_value").asText());
                    valueForNumber = valueForNumber.setScale(3, RoundingMode.HALF_UP);
                  } catch (NumberFormatException e) {
                    logger.error(e);
                  }
                }
              }
            }
          }
          if (valueForNumber.equals(BigDecimal.valueOf(-9999))
                  && valueForSymbol.equals(BigDecimal.valueOf(-9999))) {
            //insert empty cell
            htmlTr.with(createEmptyCell());
          } else {
            htmlTr.with(createTableCell(valueForSymbol, valueForNumber, satisfied));
          }
        }
      }
      tBody.with(htmlTr);
    }
    return tBody;
  }

  private ContainerTag createEmptyCell() {
    String background = WHITE_FFFFFF;
    for (LegendRange range : rangeList) {
      if (range.getLowerLimit() == null || range.getUpperLimit() == null) {
        background = range.getBackground();
        break;
      }
    }
    return td().attr(STYLE, "background-color: " + background);
  }

  private boolean isJsonRowMatch(Map<String, Entry> cellFieldsValues, JsonNode node) {


    boolean isMatch = true;
    for (Map.Entry<String, Entry> entry : cellFieldsValues.entrySet()) {
      String name;
      if ("stat".equals(entry.getKey())) {
        name = "stat_name";
      } else {
        name = entry.getKey();
      }

      if (node.findValue(name) != null
              && !node.findValue(name).asText().equals(entry.getValue().getName())) {
        isMatch = false;
        break;
      }

    }
    return isMatch;
  }

  private boolean isJsonRowMatchSymbol(Map<String, Entry> cellFieldsValues, JsonNode node) {

    if (!node.findValue("derived_stat").asText().equals(diffStatSymbol)) {
      return false;
    } else {
      return isJsonRowMatch(cellFieldsValues, node);
    }
  }

  private boolean isJsonRowMatchNumber(Map<String, Entry> cellFieldsValues, JsonNode node) {

    if (!node.findValue("derived_stat").asText().equals(diffStatValue)) {
      return false;
    } else {
      return isJsonRowMatch(cellFieldsValues, node);
    }
  }


  private void createRowHeader(int rowCounter, ContainerTag htmlTr) {
    int columnNumber = 0;
    for (Map.Entry<String, Entry> entry : listRows.get(rowCounter).entrySet()) {
      if (rowFieldToCountMap.get(rowCounter).get(entry.getKey()) != 0) {
        ContainerTag td = td(entry.getValue().getLabel());
        if (columnNumber == 0) {
          //add color for the first column
          td.withId("thside");
        }
        htmlTr.with(td.attr("rowspan", String.valueOf(
                rowFieldToCountMap.get(rowCounter).get(entry.getKey()))));
      }
      columnNumber++;
    }
  }

  private ContainerTag createTableCell(BigDecimal valueForSymbol, BigDecimal valueForNumber, WeightRequirements.Weight weight) {
    String color = BLACK_000000;
    String background = WHITE_FFFFFF;
    String title = "";
    String text = "&nbsp;";
    String borderColor = null;
    boolean checkLowLimit = false;
    boolean checkUpperLimit = false;
    StringBuilder textStr = new StringBuilder();
    ContainerTag valueContainer = null;
    ContainerTag symbolContainer = null;
    if (!valueForSymbol.equals(BigDecimal.valueOf(-9999))) {
      for (LegendRange legendRange : rangeList) {

        //check if the low limit works
        if (legendRange.getLowerLimit() != null) {
          if (legendRange.isIncludeLowerLimit()) {
            checkLowLimit = valueForSymbol.compareTo(legendRange.getLowerLimit()) >= 0;
          } else {
            checkLowLimit = valueForSymbol.compareTo(legendRange.getLowerLimit()) > 0;
          }
        }
        //check if the upper limit works
        if (legendRange.getUpperLimit() != null) {
          if (legendRange.isIncludeUpperLimit()) {
            checkUpperLimit = valueForSymbol.compareTo(legendRange.getUpperLimit()) <= 0;
          } else {
            checkUpperLimit = valueForSymbol.compareTo(legendRange.getUpperLimit()) < 0;
          }
        }

        //if inside of limits
        if (checkLowLimit && checkUpperLimit) {
          if (viewSymbol && !legendRange.getSymbol().isEmpty()) {
            symbolContainer = div().attr(STYLE, "font-size:" + symbolSize + ";").with(new UnescapedText(legendRange.getSymbol()));
          }
          if (viewValue) {
            valueContainer = div().with(new UnescapedText(valueForNumber.toString()));
          }
          color = legendRange.getColor();
          background = legendRange.getBackground();
          title = String.valueOf(valueForSymbol);
          if (weight != null) {
            title = title + "(w" + weight.getWeight() + ")";
            borderColor = weightToColor.get(weight.getWeight());
          }
          break;
        }

      }
      if (!checkLowLimit || !checkUpperLimit) {
        if (viewValue) {
          String cellText = valueForNumber.toString();
          if (weight != null) {
            cellText = cellText + "(w" + weight.getWeight() + ")";
            borderColor = weightToColor.get(weight.getWeight());
          }
          valueContainer = div().with(new UnescapedText(cellText));
        }

        color = BLACK_000000;//black
        background = WHITE_FFFFFF;//white
        title = String.valueOf(valueForSymbol);
        if (weight != null) {
          title = title + "(w" + weight.getWeight() + ")";
          borderColor = weightToColor.get(weight.getWeight());
        }

      }

    } else {
      // if p_value is undefined
      if (viewValue) {
        String cellText = valueForNumber.toString();
        if (weight != null) {
          cellText = cellText + "(w" + weight.getWeight() + ")";
          borderColor = weightToColor.get(weight.getWeight());
        }
        valueContainer = div().with(new UnescapedText(cellText));
      }
      if (viewSymbol) {
        for (LegendRange range : rangeList) {
          if (range.getLowerLimit() == null || range.getUpperLimit() == null) {
            background = range.getBackground();
            color = range.getColor();
            break;
          }
        }
      }
    }

    ContainerTag result;
    if (borderColor == null) {
      result = td().attr(STYLE, "color:" + color + ";background-color:" + background + ";padding:0;")
              .attr("title", title);

    } else {
      result = td().attr(STYLE, "color:" + color + ";background-color:" + background + ";padding:0;"
              + "border-color:" + borderColor + ";border-width:4px;")
              .attr("title", title);
    }
    if (symbolContainer != null) {
      result.with(symbolContainer);
    }
    if (valueContainer != null) {
      result.with(valueContainer);
    } else {
      if (weight != null) {
        String cellText = "w" + weight.getWeight();
        valueContainer = div().with(new UnescapedText(cellText));
        // result.with(valueContainer);
      }

    }
    return result;
  }

  private List<Map<String, Integer>> getRowspansForRowHeader() {
    List<Map<String, Integer>> fieldToCountMap = new ArrayList<>(listRows.size());
    List<List<Entry>> allCombinationsOfValues = new ArrayList<>();
    //for each row
    for (Map<String, Entry> row : listRows) {

      Map<String, Integer> rowFieldToCount = new LinkedHashMap<>(row.size());
      List<String> fieldsFromRow = new ArrayList<>(row.keySet().size());

      //for each field from this row
      for (String fieldName : row.keySet()) {

        fieldsFromRow.add(fieldName);
        List<Map<String, Entry>> copyOfAllOriginalRows = new ArrayList<>(listRows);
        List<Entry> valueCombination = new ArrayList<>();

        //find all rows that have current field and value
        for (String fieldFromRow : fieldsFromRow) {
          //get fields value
          Entry fieldValue = row.get(fieldFromRow);
          valueCombination.add(fieldValue);
          //remove rows that don't have this field value
          for (Map<String, Entry> aRow : listRows) {
            if (aRow.containsKey(fieldFromRow) && !aRow.get(fieldFromRow).equals(fieldValue)) {
              copyOfAllOriginalRows.remove(aRow);
            }
          }
        }

        //copyOfAllOriginalRows contains only rows with the unique combination
        if (!allCombinationsOfValues.contains(valueCombination)) {
          allCombinationsOfValues.add(valueCombination);
          rowFieldToCount.put(fieldName, copyOfAllOriginalRows.size());
        } else {
          rowFieldToCount.put(fieldName, 0);
        }

      }
      fieldToCountMap.add(rowFieldToCount);
    }
    return fieldToCountMap;
  }

  private ContainerTag createTableHead() {
    ContainerTag thead = thead();
    //total number of rows in the head - the number of keys in each column
    // (for example vx_mask and  fsct_lead)
    Set<String> columnsVars = listColumns.get(0).keySet();
    boolean isFirstHeatherRow = true;
    for (String field : columnsVars) {
      ContainerTag htmlTrH = tr();
      ContainerTag htmlTrHCopy = tr();
      int colspan = 0;
      // cells that are above row headers
      // empty - if user did not specify the column names
      // or with the names in the last heather's row
      if (isFirstHeatherRow) {
        int rowsapn = columnsVars.size();
        if (this.models.size() > 2) {
          rowsapn = rowsapn + 1;
        }
        for (int i = 0; i < listRows.get(0).size(); i++) {
          if (i < leftColumnsNames.size()) {
            htmlTrH.with(createHeaderCellRowspan(leftColumnsNames.get(i), rowsapn));
            htmlTrHCopy.with(createHeaderCellRowspan(leftColumnsNames.get(i), rowsapn));
          } else {
            htmlTrH.with(createHeaderCellRowspan("", rowsapn));
            htmlTrHCopy.with(createHeaderCellRowspan("", rowsapn));
          }
        }
        isFirstHeatherRow = false;
        if (this.models.size() > 2) {
          //add extra row for models names
          for (int i = 0; i < this.models.size(); i = i + 2) {
            StringBuilder label = new StringBuilder(this.models.get(i));
            if (i + 1 < this.models.size()) {
              label.append(" - ").append(this.models.get(i + 1));
            }
            htmlTrH.with(createHeaderCellColspan(0, label.toString(), listColumns.size()));
          }
          thead.with(htmlTrH);
          htmlTrH = tr();
        }
      }

      String previousField = listColumns.get(0).get(field).getLabel();

      for (int i = 0; i < this.models.size(); i = i + 2) {
        for (Map<String, Entry> column : listColumns) {
          if (column.get(field).getLabel().equals(previousField)) {
            //if this is the same field - do  create a cell but increase colspan
            colspan++;
          } else {
            //create a cell
            htmlTrH.with(createHeaderCellColspan(thead.getNumChildren(), previousField, colspan));
            //init colspan and field
            colspan = 1;
            previousField = column.get(field).getLabel();
          }
        }
      }
      //add a cell for the last column
      htmlTrH.with(createHeaderCellColspan(thead.getNumChildren(), previousField, colspan));
      thead.with(htmlTrH);
    }
    return thead;
  }

  private ContainerTag createHeaderCellColspan(int rowNumber, String text, int colspan) {
    ContainerTag th;
    if (rowNumber == 0) {
      //for the first row - red font
      th = th(text).attr("colspan", String.valueOf(colspan));
    } else {
      th = td(text).attr("colspan", String.valueOf(colspan));
    }
    return th;
  }

  private ContainerTag createHeaderCellRowspan(String text, int rowspan) {
    return th(text).attr("rowspan", String.valueOf(rowspan));
  }


  /**
   * reads tabluar data file and saves rows with derived curevs data into the JSON table format
   *
   * @param dataFile
   * @return
   * @throws IOException
   */
  private ArrayNode readFileToJsonTable(File dataFile) throws IOException {
    ArrayNode table = new ArrayNode(JsonNodeFactory.instance);

    try (FileReader is = new FileReader(dataFile);
         BoundedBufferedReader buf = new BoundedBufferedReader(is, 10000, 1024)) {
      //read the header line
      String line = buf.readLineBounded();
      String[] headers = line.split("\\t");

      //read data lines
      line = buf.readLineBounded();

      while (line != null) {

        String[] values = line.split("\\t");
        //only use lines with full set of data
        //ignore rows with original values  - not derived curves
        if (values.length == headers.length
                && (line.contains("DIFF") || line.contains("SINGLE"))) {
          table.add(addJsonRow(headers, values));
        }
        line = buf.readLineBounded();

      }
    } catch (IOException e) {
      throw e;
    }
    return table;
  }

  private ObjectNode addJsonRow(String[] headers, String[] values) {
    ObjectNode row = new ObjectNode(JsonNodeFactory.instance);
    //examine each field in the row
    for (int i = 0; i < headers.length; i++) {
      //ignore model values in the actual table
      if (!"model".equals(headers[i])) {
        row.put(headers[i], values[i]);
      } else {
        row.put("derived_stat", values[i].split("\\(")[0]);
        // retrieve model names
        String truncated = values[i].replace("DIFF_SIG(", "").replace(")", "").replace("-", "");
        String[] truncatedArr = truncated.split(" ");
        String model1 = null;
        String model2 = null;
        for (String st : truncatedArr) {
          if (this.models.contains(st)) {
            if (model1 == null) {
              model1 = st;
            } else {
              model2 = st;
              break;
            }
          }
        }
        row.put("model1", model1);
        row.put("model2", model2);
      }
    }
    return row;
  }

}
