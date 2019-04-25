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
import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import j2html.tags.ContainerTag;
import j2html.tags.UnescapedText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.style;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;


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
          + ".legendText {text-align:left;}";
  public static final String CLASS = "class";
  public static final String STYLE = "style";


  private final ContainerTag html;
  private final ContainerTag title1;
  private final ContainerTag title2;
  private final ContainerTag title3;
  private final ContainerTag htmlTable = table();
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
  private String model1;
  private String model2;

  public GraphicalOutputManager(final Scorecard scorecard) {
    html = html();
    title1 = div().attr(CLASS, "title1");
    title2 = div().attr(CLASS, "title2");
    title3 = div().attr(CLASS, "title2");
    //add head
    html.with(head().with(style().attr("type", "text/css").with(text(CSS))));

    //create range list
    initRangeList(scorecard.getThresholdFile());
    dataFileStr = scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile();
    listRows = scorecard.getListOfEachRowWithDesc();
    listColumns = scorecard.getListOfEachColumnWithDesc();
    title1.withText(scorecard.getTitle());
    viewSymbol = scorecard.getViewSymbol();
    viewValue = scorecard.getViewValue();
    viewLegend = scorecard.getViewLegend();
    plotFileStr = scorecard.getWorkingFolders().getPlotsDir() + scorecard.getPlotFile();

    String range = "";

    for (Field fixField : scorecard.getFixedVars()) {
      if ("fcst_init_beg".equals(fixField.getName())
              || "fcst_valid_beg".equals(fixField.getName())) {
        range = fixField.getValues().get(0).getLabel()
                + " - " + fixField.getValues().get(1).getLabel();
      } else if ("model".equals(fixField.getName())) {
        model1 = fixField.getValues().get(0).getLabel();
        model2 = fixField.getValues().get(1).getLabel();
      }
    }
    title2.withText("for " + model1 + " and " + model2);
    title3.withText(range);
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

  private ContainerTag createHtmlLegend() {
    ContainerTag legendTable = table().attr(CLASS, "legendTable");
    for (LegendRange range : rangeList) {
      ContainerTag td1 = td().attr(STYLE, "color:" + range.getColor() + ";"
              + "background:" + range.getBackground() + ";")
              .with(new UnescapedText(range.getSymbol()));
      ContainerTag td2 = td().attr(CLASS, "legendText");

      try {
        td2.with(new UnescapedText(String.format(range.getFormatString(), model1, model2)));
      } catch (FormatFlagsConversionMismatchException f) {
        logger.error("Error during printing the legend text for " + range.getFormatString());
        logger.error("Escaped percent sign is double percent (%%)");
        td2.with(new UnescapedText("&nbsp;"));
        logger.debug(f);
      }

      legendTable.with(tr().with(td1).with(td2));
    }


    return legendTable;
  }

  private ContainerTag createHtmlTableBody(final ArrayNode table) {
    ContainerTag tBody = tbody();
    //for each data row
    for (int rowCounter = 0; rowCounter < listRows.size(); rowCounter++) {
      ContainerTag htmlTr = tr();

      createRowHeader(rowCounter, htmlTr);

      //for each data column
      for (Map<String, Entry> mapColumn : listColumns) {
        Map<String, Entry> cellFieldsValues = new HashMap<>(listRows.get(rowCounter));
        cellFieldsValues.putAll(mapColumn);
        int index = -1;
        boolean isCellCreated = false;
        // find the corresponding value in the JSON table and create a cell
        for (int i = 0; i < table.size(); i++) {
          JsonNode node = table.get(i);
          boolean isMatch = isJsonRowMatch(cellFieldsValues, node);

          if (isMatch && !"NA".equals(node.findValue("stat_value").asText())) {
            //this is correct row - get value and create a cell
            index = i;
            BigDecimal value;
            try {
              value = new BigDecimal(node.findValue("stat_value").asText());
              value = value.setScale(3, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
              logger.error(e);
              value = BigDecimal.valueOf(-9999);
            }
            htmlTr.with(createTableCell(value));
            isCellCreated = true;

            break;
          } else {
            //this JSON row doesn't match
            //if no more JSON rows - no value for this combination - insert empty cell
            //if there are more JSON rows - continue the search
            if (i == table.size() - 1) {
              htmlTr.with(createEmptyCell());
              isCellCreated = true;
            }
          }

        }
        //we are done with the row from JSON table - remove it to speed up next searches
        if (index != -1) {
          table.remove(index);
        }
        if (!isCellCreated) {
          //the cell value is not in the data table
          htmlTr.with(createEmptyCell());
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

  private ContainerTag createTableCell(BigDecimal value) {
    String color = WHITE_FFFFFF;
    String background = BLACK_000000;
    String title = "";
    String text = "&nbsp;";
    boolean checkLowLimit = false;
    boolean checkUpperLimit = false;
    StringBuilder textStr = new StringBuilder();
    for (LegendRange legendRange : rangeList) {

      //check if the low limit works
      if (legendRange.getLowerLimit() != null) {
        if (legendRange.isIncludeLowerLimit()) {
          checkLowLimit = value.compareTo(legendRange.getLowerLimit()) >= 0;
        } else {
          checkLowLimit = value.compareTo(legendRange.getLowerLimit()) > 0;
        }
      }
      //check if the upper limit works
      if (legendRange.getUpperLimit() != null) {
        if (legendRange.isIncludeUpperLimit()) {
          checkUpperLimit = value.compareTo(legendRange.getUpperLimit()) <= 0;
        } else {
          checkUpperLimit = value.compareTo(legendRange.getUpperLimit()) < 0;
        }
      }

      //if inside of limits
      if (checkLowLimit && checkUpperLimit) {
        if (viewSymbol) {
          textStr.append(legendRange.getSymbol());
        }
        if (viewValue) {
          textStr.append(String.valueOf(value));
        }
        color = legendRange.getColor();
        background = legendRange.getBackground();
        title = String.valueOf(value);
        text = textStr.toString();
        break;
      }

    }
    if (!checkLowLimit || !checkUpperLimit) {
      if (viewValue) {
        textStr.append(String.valueOf(value));
      }
      color = BLACK_000000;//black
      background = WHITE_FFFFFF;//white
      title = String.valueOf(value);
      text = textStr.toString();
    }

    return td().attr(STYLE, "color:" + color + ";background-color:" + background + ";")
            .attr("title", title).with(new UnescapedText(text));
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
    //total number of rows in the thead - the number of keys in each column
    // (for example vx_mask and  fsct_lead)
    for (String field : listColumns.get(0).keySet()) {
      ContainerTag htmlTrH = tr();
      //empty cells that are above roe headers
      for (int i = 0; i < listRows.get(0).size(); i++) {
        htmlTrH.with(td());
      }
      String previousField = listColumns.get(0).get(field).getLabel();
      int colspan = 0;

      for (Map<String, Entry> column : listColumns) {
        if (column.get(field).getLabel().equals(previousField)) {
          //if this is the same field - do ot creat a cell but increase colspan
          colspan++;
        } else {
          //create a cell
          htmlTrH.with(createTableHeaderCell(thead.children.size(), previousField, colspan));
          //init colspan and field
          colspan = 1;
          previousField = column.get(field).getLabel();
        }
      }
      //add a cell for the last column
      htmlTrH.with(createTableHeaderCell(thead.children.size(), previousField, colspan));
      thead.with(htmlTrH);
    }
    return thead;
  }

  private ContainerTag createTableHeaderCell(int rowNumber, String previousField, int colspan) {
    ContainerTag th;
    if (rowNumber == 0) {
      //for the first row - red font
      th = th(previousField).attr("colspan", String.valueOf(colspan));
    } else {
      th = td(previousField).attr("colspan", String.valueOf(colspan));
    }
    return th;
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
         BoundedBufferedReader buf = new BoundedBufferedReader(is, 1024, 1024)) {
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
      }
    }
    return row;
  }

}
