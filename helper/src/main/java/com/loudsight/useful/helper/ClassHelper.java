package com.loudsight.useful.helper;


public final class ClassHelper {

    private ClassHelper() {
    }

    /**
     * Unchecked cast of an object to a target type.
     * Useful for situations where you need to cast an object to a generic type.
     * Example usage:
     * `
     * T typedObject = Cast.uncheckedCast(object) // where T is the target type
     * ` *
     *
     * @param <T> - target Class
     * @param entity - Object to cast
     * @return the typed object
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> T uncheckedCast(Object entity) {
        return (T)entity;
    }


    public static String getPackageName(String qualifiedName) {
        var lastIndexOfDot = qualifiedName.lastIndexOf('.');

        return qualifiedName.substring(0, lastIndexOfDot);
    }
}
