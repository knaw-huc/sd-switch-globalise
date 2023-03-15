package nl.knaw.huc.sdswitch.server.routing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record MimeTypeQFactor(String type, String subType, float qFactor) implements Comparable<MimeTypeQFactor> {
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
