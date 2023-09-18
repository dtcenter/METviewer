package edu.ucar.metviewer.scorecard.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.ResourceBundle;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ScorecardServlet")
public class ScorecardServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static String dataPath;
  private static String dataUrl;

  static {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle("scorecard");
      dataPath = bundle.getString("scorecard.data.dir");
      dataUrl = "/" + bundle.getString("scorecard.data.url") + "/";
    } catch (Exception e) {
      System.out.println("can't get properties from the config file");
    }
  }


  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    ObjectMapper mapper = new ObjectMapper();
    ArrayNode scorecards = mapper.createArrayNode();
    ObjectNode result = mapper.createObjectNode();
    if (dataPath != null) {
      File file = new File(dataPath);
      File[] directories = file.listFiles((dir, name) -> new File(dir, name).isDirectory());
      sortFilesByDateCreated(directories);
      if (directories != null) {
        for (File f : directories) {
          scorecards.add(f.getName());
        }

      }
    }
    result.set("scorecards", scorecards);
    result.put("data_url", dataUrl);
    response.setContentType("text/plain");
    try {
      response.getWriter().write(result.toString());
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void sortFilesByDateCreated (File[] files) {
    Arrays.sort(files, (f1, f2) -> {
      long l1 = getFileCreationEpoch(f1);
      long l2 = getFileCreationEpoch(f2);
      return Long.compare(l2, l1);
    });
  }
  public static long getFileCreationEpoch (File file) {
    try {
      BasicFileAttributes attr = Files.readAttributes(file.toPath(),
              BasicFileAttributes.class);
      return attr.creationTime().toInstant().toEpochMilli();
    } catch (IOException e) {
      return 0;
    }
  }

}
