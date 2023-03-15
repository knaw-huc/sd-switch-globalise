package nl.knaw.huc.sdswitch.recipe;

public class RecipeMappingException extends Exception {
    public RecipeMappingException(String message) {
        super(message);
    }

    public RecipeMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
