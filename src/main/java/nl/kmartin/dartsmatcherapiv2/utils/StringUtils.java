package nl.kmartin.dartsmatcherapiv2.utils;

public class StringUtils {
    public static String pascalToCamelCase(String input) {
        if (input == null || input.isEmpty()) return input;
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }
}
