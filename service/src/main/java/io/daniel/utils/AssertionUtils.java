package io.daniel.utils;

public class AssertionUtils {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private AssertionUtils() {
    }

}
