package org.knaw.huc.sdswitch.server.security;

import io.javalin.core.security.RouteRole;

public enum Role implements RouteRole {
    ANONYMOUS, USER
}
