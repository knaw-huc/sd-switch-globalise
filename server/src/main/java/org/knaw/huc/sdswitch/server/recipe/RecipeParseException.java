package org.knaw.huc.sdswitch.server.recipe;

public class RecipeParseException extends Exception {
    public RecipeParseException(String message) {
        super(message);
    }

    public RecipeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
