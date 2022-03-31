package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;
import nl.mpi.tla.util.Saxon;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static nl.mpi.tla.util.Saxon.getProcessor;

public class JsonToHtml {

  public static String jsonToHtml(String json) {
    XdmValue jsonXML = null;
    try {
      jsonXML = Saxon.parseJson(json);
      Saxon.save(((XdmNode) jsonXML).asSource(), new File("out.xml"));
    } catch (SaxonApiException e) {
      System.err.println("JSON to XML failed! ");
      e.printStackTrace();
    }
    try {
      XsltTransformer toHtml = Saxon.buildTransformer(new File("src/main/resources/raa_xml2html.xsl")).load();
      toHtml.setSource(((XdmNode) jsonXML).asSource());
      toHtml.setDestination(getProcessor().newSerializer(new File("result.html")));
      toHtml.transform();
      Path resultFile = Path.of("result.html");
      String result = Files.readString(resultFile);
      return result;
    } catch (SaxonApiException | IOException e) {
      e.printStackTrace();
    }
    return "";
  }
}
