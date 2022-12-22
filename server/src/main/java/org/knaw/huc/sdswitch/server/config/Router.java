package org.knaw.huc.sdswitch.server.config;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router {
    private final Set<Switch<?>> switches;

    public Router(Javalin app, String urlPattern, Set<Switch<?>> switches) {
        this.switches = switches;
        app.get(urlPattern, this::withRequest);
        app.post(urlPattern, this::withRequest);
    }

    private void withRequest(Context ctx) {
        Switch<?> matchingSwitch = null;
        if (switches.size() > 1) {
            String accept = ctx.header("accept");
            if (accept != null) {
                Optional<? extends Switch<?>> switchMatchingAccept = Arrays.stream(accept.split(","))
                        .map(MimeTypeQFactor::create)
                        .sorted()
                        .map(mime -> switches.stream().filter(aSwitch ->
                                mime.matches(aSwitch.getAcceptMimeType())).findFirst())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();

                if (switchMatchingAccept.isPresent())
                    matchingSwitch = switchMatchingAccept.get();
            }
        }

        if (matchingSwitch == null) {
            Optional<Switch<?>> firstSwitch = switches.stream().findFirst();
            if (firstSwitch.isPresent())
                matchingSwitch = firstSwitch.get();
            else
                throw new RuntimeException("No configured switch found!");
        }

        matchingSwitch.handle(ctx);
    }

    private record MimeTypeQFactor(String type, String subType, float qFactor) implements Comparable<MimeTypeQFactor> {
        private static final Pattern Q_FACTOR_PATTERN = Pattern.compile("q=(0\\.\\d)");

        public static MimeTypeQFactor create(String acceptMimeType) {
            String[] mimeAndParams = acceptMimeType.split(";", 2);
            Matcher matcher = Q_FACTOR_PATTERN.matcher(mimeAndParams.length == 2 ? mimeAndParams[1] : "");
            float qFactor = matcher.find() && matcher.group(1) != null ? Float.parseFloat(matcher.group(1)) : 1;

            String[] typeAndSubType = mimeAndParams[0].split("/", 2);
            return new MimeTypeQFactor(typeAndSubType[0].trim(), typeAndSubType[1].trim(), qFactor);
        }

        public boolean matches(String mimeType) {
            String[] typeAndSubType = mimeType.split("/", 2);
            boolean typeMatches = type.equals("*") || type.equals(typeAndSubType[0].trim());
            boolean subTypeMatches = subType.equals("*") || subType.equals(typeAndSubType[1].trim());
            return typeMatches && subTypeMatches;
        }

        @Override
        public int compareTo(MimeTypeQFactor other) {
            return -1 * Float.compare(this.qFactor, other.qFactor);
        }
    }
}
