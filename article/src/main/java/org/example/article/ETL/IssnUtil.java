package org.example.article.ETL;


public final class IssnUtil {
    private IssnUtil() {}

    public static String normalize(String raw) {
        if (raw == null) return null;
        String d = raw.replaceAll("[^0-9Xx]", "");
        if (d.length() != 8) return null;
        return d.substring(0,4) + "-" + d.substring(4);
    }
}
