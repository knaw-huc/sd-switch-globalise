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
import org.json.JSONArray;
import org.json.JSONObject;



public class JsonToTtl {

  static String rdfSubject = "";
  static String rdfType = "";

  public static String jsonToTtl(String json) throws JSONException {
    Document schema = readSchema();
    JSONObject jsonObject = new JSONObject(json);

    //getting first and last name
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
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    return doc;
  }
}
