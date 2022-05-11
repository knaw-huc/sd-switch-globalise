package org.knaw.huc.sdswitch.server.config;

import org.knaw.huc.sdswitch.server.security.Role;

public record SwitchRoute(String urlPattern, Role role) implements Comparable<SwitchRoute> {
    public static SwitchRoute create(String urlPattern, String role) {
        return new SwitchRoute(urlPattern, switch (role.toLowerCase().trim()) {
            case "user" -> Role.USER;
            default -> Role.ANONYMOUS;
        });
    }

    @Override
    public int compareTo(SwitchRoute o) {
        return -1 * Integer.compare(urlPattern.length(), o.urlPattern.length());
    }
}
