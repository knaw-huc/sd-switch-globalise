package org.knaw.huc.auth;

public class OpenIDUnauthorizedException extends Exception {
    public OpenIDUnauthorizedException() {
        super("Unauthorized user");
    }
}
