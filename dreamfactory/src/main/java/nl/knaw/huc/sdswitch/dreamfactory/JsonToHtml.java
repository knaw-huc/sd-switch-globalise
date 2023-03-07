package nl.knaw.huc.sdswitch.dreamfactory;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;
import nl.mpi.tla.util.Saxon;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import static nl.mpi.tla.util.Saxon.getProcessor;

public class JsonToHtml {
    private final XsltTransformer toHtml;

    public JsonToHtml(String xml2HtmlPath) throws SaxonApiException {
        this.toHtml = Saxon.buildTransformer(new File(xml2HtmlPath)).load();
    }

    public String toHtml(String json) throws SaxonApiException {
        XdmNode jsonXML = Saxon.parseJson(json);
        Writer writer = new StringWriter();

        toHtml.setSource(jsonXML.asSource());
        toHtml.setDestination(getProcessor().newSerializer(writer));
        toHtml.transform();

        return writer.toString();
    }
}
