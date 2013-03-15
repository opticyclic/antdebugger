package com.handyedit.ant.util;

import java.util.List;

/**
 * @author Alexei Orischenko
 *         Date: Nov 10, 2009
 */
public class StringUtil {
    
    public static final String QUOTE = "\"";

    public static String quote(String s) {
        return QUOTE + s + QUOTE;
    }

    public static String[] toArray(List<String> names) {
        if (names == null) {
            return new String[0];
        }

        String[] result = new String[names.size()];
        names.toArray(result);

        return result;
    }

    public static int findPropertyNameEnd(String text, int pos, int increment) {
        if (pos < 0 || pos >= text.length()) {
            return -1;
        }

        char c = text.charAt(pos);
        while (isPropertyNameCharacter(c)) {
            pos += increment;
            if (pos < 0 || pos >= text.length()) {
                return pos;
            }
            c = text.charAt(pos);
        }

        return pos;
    }

    private static boolean isPropertyNameCharacter(char c) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_';
    }

    public static String removeLineFeeds(String s) {
        if (s == null) {
            return null;
        }

        s = s.replace("\r", "");
        return s.replace("\n", "");
    }
}
