package org.knaw.huc.sdswitch.recipe;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JsonToTtlTest {

  @Test
  public void runTestJsonTtl() throws JSONException {
    String json = "{ \"id\":1 }";
    String expectedResult = "<https://humanities.knaw.nl/raa/person/1> a <https://humanities.knaw.nl/person>.";
    String result = JsonToTtl.jsonToTtl(json);
    Assert.assertEquals(expectedResult, result);
  }

  @Test
  public void runTestSchema() {
    Document doc = JsonToTtl.readSchema();
    System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
    String result = doc.getDocumentElement().getNodeName();
    Assert.assertEquals("s:S", result);
    // NodeList list = doc.getElementsByTagName("persoon");
    // Node node = list.item(0);
    // if (node.getNodeType() == Node.ELEMENT_NODE) {
    //   Element element = (Element) node;
    //   String rdfSubject = element.getAttribute("rdf:subject");
    //   String rdfType = element.getAttribute("rdf:type");
      Assert.assertEquals("https://humanities.knaw.nl/raa/person/{id}", JsonToTtl.rdfSubject);
      Assert.assertEquals("https://humanities.knaw.nl/person", JsonToTtl.rdfType);
    // }
  }
}
