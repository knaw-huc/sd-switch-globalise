package nl.knaw.huc.sdswitch.textanno;

import nl.knaw.huc.sdswitch.recipe.RecipeException;
import nl.knaw.huc.sdswitch.recipe.RecipeTask;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TextAnnoRecipe implements Recipe<TextAnnoRecipe.TextAnnoRecipeConfig> {
    static class ConfigNamespace extends SimpleNamespace {
        public ConfigNamespace(String prefix, String name) {
            super(prefix, name);
        }
    }

    record TextAnnoRecipeConfig(String timbuctooUrl, String userId, String datasetName, String authorization,
                                Set<ConfigNamespace> namespaces, SimpleIRI predicateToMatch,
                                String iriPrefix, String typeSuffix) {
    }

    @Override
    public void validateConfig(TextAnnoRecipeConfig config, Set<String> pathParams) throws RecipeValidationException {
        if (config.timbuctooUrl == null)
            throw new RecipeValidationException("Missing required 'timbuctooUrl'");

        if (config.userId == null)
            throw new RecipeValidationException("Missing required 'userId'");

        if (config.datasetName == null)
            throw new RecipeValidationException("Missing required 'datasetName'");

        if (config.authorization == null)
            throw new RecipeValidationException("Missing required 'authorization'");

        if (config.predicateToMatch == null)
            throw new RecipeValidationException("Missing required 'predicateToMatch'");

        if (config.iriPrefix == null)
            throw new RecipeValidationException("Missing required 'iriPrefix'");

        if (config.typeSuffix == null)
            throw new RecipeValidationException("Missing required 'typeSuffix'");
    }

    @Override
    public RecipeResponse withData(RecipeData<TextAnnoRecipeConfig> data) throws RecipeException {
        try {
            final Path tempFile = Files.createTempFile("textanno", null);
            Files.copy(data.bodyInputStream(), tempFile, REPLACE_EXISTING);

            final RecipeTask task = new TextAnnoRecipeTask(data.config(), tempFile);
            return RecipeResponse.withTask(task, "application/json");
        } catch (IOException ex) {
            throw new RecipeException("", ex);
        }
    }
}
