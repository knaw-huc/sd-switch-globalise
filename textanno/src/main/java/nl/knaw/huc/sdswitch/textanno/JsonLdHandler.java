package nl.knaw.huc.sdswitch.textanno;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;
import static org.eclipse.rdf4j.rio.RDFFormat.JSONLD;
import static org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
import static org.eclipse.rdf4j.model.util.Statements.statement;

public class JsonLdHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final IRI predicateToMatch;
    private final Supplier<IRI> uriSupplier;
    private final Function<IRI, IRI> typeFunction;
    private final Map<String, String> ids;
    private final RDFWriter writer;

    public JsonLdHandler(OutputStream outputStream, IRI predicateToMatch, Set<? extends Namespace> namespaces,
                         Supplier<IRI> uriSupplier, final Function<IRI, IRI> typeFunction) {
        this.predicateToMatch = predicateToMatch;
        this.uriSupplier = uriSupplier;
        this.typeFunction = typeFunction;

        ids = new HashMap<>();

        writer = Rio.createWriter(TURTLE, outputStream);
        writer.startRDF();

        for (Namespace namespace : namespaces)
            writer.handleNamespace(namespace.getPrefix(), namespace.getName());
    }

    public void parse(Reader reader) throws IOException {
        for (JsonNode node : OBJECT_MAPPER.readTree(reader)) {
            Model model = Rio.parse(new StringReader(node.toString()), JSONLD);
            handleWithModel(model);
        }
    }

    public void end() {
        writer.endRDF();
    }

    public Map<String, String> getIds() {
        return ids;
    }

    private void handleWithModel(Model model) {
        for (Statement statement : model.getStatements(null, predicateToMatch, null)) {
            IRI subject = uriSupplier.get();

            if (statement.getSubject() instanceof IRI subj)
                ids.put(subj.stringValue(), subject.stringValue());

            if (statement.getObject() instanceof Resource resource) {
                for (Statement st : model.getStatements(statement.getSubject(), TYPE, null)) {
                    IRI type = typeFunction.apply((IRI) st.getObject());
                    if (type != null)
                        writer.handleStatement(statement(subject, TYPE, type, null));
                }

                for (Statement st : model.getStatements(resource, null, null)) {
                    Statement stToAdd = statement(subject, st.getPredicate(), st.getObject(), null);
                    addStatement(model, stToAdd);
                }
            }
        }
    }

    private void addStatement(Model model, Statement statement) {
        writer.handleStatement(statement);
        if (statement.getObject() instanceof BNode blankNode)
            for (Statement st : model.getStatements(blankNode, null, null))
                addStatement(model, st);
    }
}
