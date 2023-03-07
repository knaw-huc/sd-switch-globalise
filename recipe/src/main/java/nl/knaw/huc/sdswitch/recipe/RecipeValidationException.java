package nl.knaw.huc.sdswitch.recipe;

public class RecipeValidationException extends Exception {
    public RecipeValidationException(String message) {
        super(message);
    }

    public RecipeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
