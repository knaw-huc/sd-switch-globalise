package org.knaw.huc.sdswitch.recipe;

public class RecipeParseException extends Exception {
    public RecipeParseException(String message) {
        super(message);
    }

    public RecipeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
