package org.knaw.huc.auth.data;

public class Essential {
    private static final Essential ESSENTIAL_SINGLETON = new Essential();
    private final boolean essential = true;

    public static Essential get() {
        return ESSENTIAL_SINGLETON;
    }
}
