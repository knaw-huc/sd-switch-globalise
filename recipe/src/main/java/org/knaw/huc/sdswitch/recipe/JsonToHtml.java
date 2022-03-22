package org.knaw.huc.sdswitch.recipe;

import mjson.Json;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonToHtml {

  private static Json jsonObject;


  public static String jsonToHtml(String json) {
    jsonObject = Json.read(json);
    Document schema = readSchema();
    return "";
  }

  public static Document readSchema() {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = null;

    return doc;
  }
}
