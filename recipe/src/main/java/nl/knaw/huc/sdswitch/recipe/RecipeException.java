package nl.knaw.huc.sdswitch.recipe;

public class RecipeException extends Exception {
    private final int httpStatus;

    public RecipeException(String message) {
        this(message, 500);
    }

    public RecipeException(String message, Throwable cause) {
        this(message, cause, 500);
    }

    public RecipeException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public RecipeException(String message, Throwable cause, int httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isInternalServerError() {
        return httpStatus == 500;
    }
}
