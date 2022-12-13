package com.loudsight.useful.string;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtils {
    private static final char[] codeChars = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    private static final Random random = new SecureRandom();


    public static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Turns array of bytes into string
     *
     * @param buf
     * Array of bytes to convert to hex string
     * @return Generated hex string
     */
    public static String convertToHex(byte[] buf) {
        StringBuilder strbuf = new StringBuilder(buf.length * 2);
        int i = 0;

        while (i < buf.length) {
            if ((((int)buf[i]) & 0xff) < 0x10) {
                strbuf.append("0");
            }

            strbuf.append(Long.valueOf(buf[i] & 0xff).toString());
            i++;
        }

        return strbuf.toString();
    }

    public static String extractFromHex(String hexString) {
        char[] s = hexString.toCharArray();
        StringBuilder ss = new StringBuilder();
        int i = 0;
        while (i < s.length) {
            char c = (char)(((int) s[i + 1] << 8) + s[i]);
            ss.append(c);
            i += 2;
        }
        return ss.toString();
    }

    public static Collection<String> filterMatching(Collection<String> countries, String query) {
        return countries
                .stream()
                .filter(country -> country.toUpperCase().startsWith(query.toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * http://stackoverflow.com/a/391978/1249664
     *
     * @param str
     * @param n
     *
     * @return the padded string
     */
    public static String  padRight(String str, int n) {
        return String.format("%1$-" + n + "s", str);
    }

    /**
     * http://stackoverflow.com/a/391978/1249664
     *
     * @param str
     * @param n
     *
     * @return the padded string
     */
    public static String padLeft(String str, int n) {
        return String.format("%1$#" + n + "s", str);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String[] splitAtLast(String str, char chr) {
        int position = str.lastIndexOf(chr);
        return Arrays.asList(
                str.substring(0, position),
                str.substring(position + 1)
        ).toArray(new String[0]);
    }


    public static String  camelCaseToUnderscores(String str){
        StringBuilder res = new StringBuilder();
        for (char it: str.toCharArray())  {
            if (Character.isUpperCase(it)) {
                res.append("_");
            }
            res.append(it);
        }
        return res.toString();
    }

    public static String  underscoresToCamelcase(String str) {

        StringBuilder res = new StringBuilder();
        boolean capitalise = false;

        for (char it: str.toLowerCase().toCharArray())  {
            if (it == '_') {
                capitalise = true;
            } else {
                char nextChar = it;
                if (capitalise) {
                    capitalise = false;
                    nextChar = Character.toUpperCase(nextChar);
                }
                res.append(nextChar);
            }
        }
        return res.toString();
    }

    public static String randomString(int length) {
        return IntStream.range(0, length).mapToObj(String::valueOf).reduce("", (a, b) -> {
            int offset = random.nextInt(36);
            char codeChar = codeChars[offset];
            return a + codeChar;
        });

    }

}
