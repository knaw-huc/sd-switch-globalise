package org.knaw.huc.sdswitch.server.config;

public class SwitchException extends Exception {
    public SwitchException(String message) {
        super(message);
    }

    public SwitchException(String message, Throwable cause) {
        super(message, cause);
    }
}
