package nl.knaw.huc.sdswitch.server.util;

public interface Server {
    int PORT = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "8080"));
    String DOMAIN = System.getenv().getOrDefault("SERVER_DOMAIN", "http://localhost:" + PORT);
}
