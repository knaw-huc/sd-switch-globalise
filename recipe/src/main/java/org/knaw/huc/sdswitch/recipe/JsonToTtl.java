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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonToTtl {

  static String rdfSubject = "";
  static String rdfType = "";
  static Pattern MY_PATTERN = Pattern.compile("\\{(.*?)\\}");
  static HashMap<String, String[]> predicates = new HashMap<String, String[]>();
  private static Json jsonObject;


  public static String jsonToTtl(String json) {
    jsonObject = Json.read(json);
    Document schema = readSchema();
    Matcher m = MY_PATTERN.matcher(rdfSubject);
    while (m.find()) {
      String identifier = "" + jsonObject.at(m.group(1)).getValue();
      rdfSubject = rdfSubject.replace("{"+m.group(1)+"}", identifier);
      // String s = m.group(1);
      // s now contains "BAR"
    }
    String id = "" + jsonObject.at("id").getValue();
    rdfSubject = rdfSubject.replace("{id}", id);
    String ttl = "<" + rdfSubject + "> a <" + rdfType + ">";
    for (Map.Entry<String, String[]> entry : predicates.entrySet()) {
      // ook hier {identifiers} vervangen
      String key = entry.getKey();
      String value = entry.getValue()[0];
      String node = entry.getValue()[1];
      String temp = "";
      m = MY_PATTERN.matcher(node);
      if (node.contains("{.}")) {
        temp += "<" + node.replace("{.}",value) + ">";
        value = "";
      }
      while (m.find()) {
        try {
          String identifier = "" + jsonObject.at(m.group(1)).getValue();
          key = node.replace("{" + m.group(1) + "}", identifier) + "/"+ value;
        } catch (NullPointerException npe) {
        // do nothing
        }
      }
      if (value!="") {
        value = "\"" + value + "\"";
      }
      ttl += ";\n  <" + key + "> " + temp + value;
    }
    ttl += ".";
    return ttl;
  }

  public static Document readSchema() {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = null;

    try {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      // get schema_raa.xml from config
      //jar:/schema_raa.xml  dan JsonToTtl.class.getClassLoader().getResourceAsStream("schema_raa.xml")
      // geen jar: prefix dan je oude code
      // new File(filename)
      doc = db.parse(JsonToTtl.class.getClassLoader().getResourceAsStream("schema_raa.xml"));
      NodeList list = doc.getElementsByTagName("persoon");
      Node node = list.item(0);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        rdfSubject = element.getAttribute("rdf:subject");
        rdfType = element.getAttribute("rdf:type");
        NodeList children = node.getChildNodes();
        for (int temp = 0; temp < children.getLength(); temp++) {
          if (children.item(temp).getNodeType() == Node.ELEMENT_NODE) {
            Element child = (Element) children.item(temp);
            String nodePredicate = child.getAttribute("rdf:predicate");
            String nodeObject = child.getAttribute("rdf:object");
            if (nodeObject==null) {
              nodeObject = "";
            }
            if (nodePredicate != "") {
              String nodeName = child.getNodeName();
              try {
                String[] temp_2 =  {"" + jsonObject.at(nodeName).getValue(), nodeObject};
                predicates.put(nodePredicate, temp_2);
              } catch (NullPointerException npe) {
                // predicates.put(nodePredicate, "" + nodeName);
              }
            }
          }
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    return doc;
  }
}
