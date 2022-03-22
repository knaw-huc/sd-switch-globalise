package org.knaw.huc.sdswitch.recipe;

import mjson.Json;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import nl.mpi.tla.util.Saxon;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;

import java.io.File;
import java.util.Objects;
import org.apache.commons.io.IOUtils;

public class JsonToHtml {

  private static Json jsonObject;


  public static String jsonToHtml(String json) {
    // to solve:
    // [ERROR] Failed to execute goal on project recipe:
    // Could not resolve dependencies for project org.knaw.huc:recipe:jar:1.0-SNAPSHOT:
    // Could not find artifact nl.knaw.huc:resourcesync:jar:1.0.2 in
    // CLARIN (https://nexus.clarin.eu/content/repositories/Clarin)
    XdmValue jsonXML = null;
    try {
      jsonXML = Saxon.parseJson(json.toString());
    } catch (SaxonApiException e) {
      System.err.println("JSON to XML failed! ");
      e.printStackTrace();
    }
    try {
      XsltTransformer toHtml = Saxon.buildTransformer(new File("src/main/resources/raa_xml2html.xsl")).load();
      toHtml.setSource(IOUtils.toInputStream(jsonXML.toString(), "UTF-8"));
    } catch (SaxonApiException e) {
      e.printStackTrace();
    }

    jsonObject = Json.read(json);
    Document schema = readSchema(json);
    return jsonXML.toString();
  }

  public static Document readSchema(String json) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = null;
    Processor processor = new Processor(true);
    XsltCompiler xsltCompiler = processor.newXsltCompiler();
    try {
      XsltExecutable executable = xsltCompiler.compile(new StreamSource("raa_xml2html.xsl"));
      XsltTransformer transformer = executable.load();
      transformer.setParameter(new QName("json"), XdmValue.makeValue(json));
      transformer.transform();
    } catch (SaxonApiException e) {
      e.printStackTrace();
    }
    return doc;
  }
}
