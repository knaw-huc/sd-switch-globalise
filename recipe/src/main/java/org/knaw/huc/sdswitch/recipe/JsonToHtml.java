package org.knaw.huc.sdswitch.recipe;

import mjson.Json;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;

public class JsonToHtml {

  private static Json jsonObject;


  public static String jsonToHtml(String json) {
    jsonObject = Json.read(json);
    Document schema = readSchema(json);
    return "";
  }

  public static Document readSchema(String json) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = null;
    Processor processor = new Processor(true);
    XsltCompiler xsltCompiler = processor.newXsltCompiler();
    try {
      XsltExecutable executable = xsltCompiler.compile(new StreamSource("raa_xml2html.xsl"));
      XsltTransformer transfomer = executable.load();
      transfomer.setParameter(new QName("json"), XdmValue.makeValue(json));
      transfomer.transform();
    } catch (SaxonApiException e) {
      e.printStackTrace();
    }
    return doc;
  }
}
