package org.knaw.huc.sdswitch.recipe;

import org.json.JSONException;
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
import java.io.InputStream;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;



public class JsonToTtl {

  static String rdfSubject = "";
  static String rdfType = "";
  static Map predicates = Collections.EMPTY_MAP;


  public static String jsonToTtl(String json) throws JSONException {
    Document schema = readSchema();
    JSONObject jsonObject = new JSONObject(json);

    String id = jsonObject.getString("id");
    rdfSubject.replace("id",id);
    String ttl = rdfSubject + " a " + rdfType + ".";
    return ttl;
  }

  public static Document readSchema() {
    String filename = "src/main/resources/schema_raa.xml";
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = null;

    try {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.parse(new File(filename));
      NodeList list = doc.getElementsByTagName("persoon");
      Node node = list.item(0);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        rdfSubject = element.getAttribute("rdf:subject");
        rdfType = element.getAttribute("rdf:type");
        NodeList children = node.getChildNodes();
        for (int temp = 0; temp < children.getLength(); temp++) {
          Element child = (Element) children.item(temp);
          String nodePredicate = child.getAttribute("rdf:predicate");
          String nodeName = child.getNodeName();
          predicates.put(nodeName, nodePredicate);
        }
      }

      list = node.getChildNodes();
      for (int temp = 0; temp < list.getLength(); temp++) {
        Node child = list.item(temp);
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    return doc;
  }
}
