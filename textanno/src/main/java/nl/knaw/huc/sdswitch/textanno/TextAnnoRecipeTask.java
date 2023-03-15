package nl.knaw.huc.sdswitch.textanno;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huc.sdswitch.recipe.RecipeException;
import nl.knaw.huc.sdswitch.recipe.RecipeTask;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class TextAnnoRecipeTask implements RecipeTask {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TextAnnoRecipe.TextAnnoRecipeConfig config;
    private final Path dataFile;

    public TextAnnoRecipeTask(TextAnnoRecipe.TextAnnoRecipeConfig config, Path dataFile) {
        super();
        this.config = config;
        this.dataFile = dataFile;
    }

    @Override
    public void run(Path resultFile) throws RecipeException {
        try (Reader in = Files.newBufferedReader(dataFile);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final JsonLdHandler jsonLdHandler = new JsonLdHandler(
                    out, config.predicateToMatch(), config.namespaces(),
                    () -> subjectSupplier(config.iriPrefix()),
                    iri -> typeFunction(config.iriPrefix(), config.typeSuffix(), iri)
            );

            jsonLdHandler.parse(in);
            jsonLdHandler.end();

            final TimbuctooUpload timbuctooUpload = new TimbuctooUpload(config.timbuctooUrl());
            timbuctooUpload.uploadRDF(config.userId(), config.datasetName(),
                    config.authorization(), out.toByteArray(), "text/turtle");

            Files.write(resultFile, OBJECT_MAPPER.writeValueAsBytes(jsonLdHandler.getIds()));
        } catch (IOException | TimbuctooUpload.TimbuctooUploadException | URISyntaxException ex) {
            throw new RecipeException(ex.getMessage(), ex);
        }
    }

    private static IRI subjectSupplier(String iriPrefix) {
        return Values.iri(iriPrefix + UUID.randomUUID());
    }

    private static IRI typeFunction(String iriPrefix, String typeSuffix, IRI iri) {
        return Values.iri(iriPrefix + iri.getLocalName() + typeSuffix);
    }
}
