package nl.knaw.huc.sdswitch.recipe;

import java.nio.file.Path;

@FunctionalInterface
public interface RecipeTask {
    void run(Path resultFile) throws RecipeException;
}
