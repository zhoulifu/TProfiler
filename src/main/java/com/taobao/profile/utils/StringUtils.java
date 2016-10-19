package com.taobao.profile.utils;

public class StringUtils {
    private StringUtils() {
    }

    /**
     * Transform first character of source string to uppercase iff it is lowercase
     *
     * @param source source string
     * @return
     */
    public static String upperCaseFirstLetter(String source) {
        if (isLowerLetter(source.charAt(0))) {
            char[] chars = source.toCharArray();
            chars[0] &= 0xdf;
            return new String(chars);
        } else {
            return source;
        }
    }

    /**
     * Determines if the specified character (Unicode code point) is a
     * lowercase character.
     *
     * @param ch the character to be tested
     * @return {@code true} if the character is lowercase
     */
    private static boolean isLowerLetter(char ch) {
        return Character.getType(ch) == Character.LOWERCASE_LETTER;
    }
}
