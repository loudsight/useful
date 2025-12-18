package com.loudsight.useful.helper;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to hold utilities related to casting of types
 *
 * @author munyengm
 */
public class JvmClassHelper {
    private static final Map<String, Class<?>> PRIMITIVES = Stream.of(
            Boolean.class,
            boolean.class,
            int.class,
            Integer.class,
            Double.class,
            double.class,
            Float.class,
            Float.class,
            Long.class,
            Long.class,
            String.class
    ).collect(
            Collectors.toMap(Class::getName, it -> it));

    public static boolean isPrimitive(Class<?> entity) {
        var className = getClassName(entity.getName());
        return PRIMITIVES.containsKey(className);
    }

    public static boolean isPrimitive(String primitiveName) {
        var className = getClassName(primitiveName);
        return PRIMITIVES.containsKey(className);
    }

    public static <T> Class<T> getPrimitiveClass(String primitiveName)  {
        var className = getClassName(primitiveName);

        return ClassHelper.uncheckedCast(PRIMITIVES.get(className));
    }

    private static String getClassName(String primitiveName) {
        var className = primitiveName;
        if ("kotlin.String".equals(className)) {
            className = "java.lang.String";
        } else if (className.startsWith("kotlin.")) {
            className = className.replace("kotlin.", "");
            className = className.toLowerCase();
        }

        return className;
    }

    public static <T> Class<T> classForName(String className) {
        return classForName(className, ClassLoader.getSystemClassLoader());
    }

    public static <T> Class<T> classForName(String className, ClassLoader classLoader) {
        if (isPrimitive(className)) {
            return getPrimitiveClass(className);
        } else try {
            return ClassHelper.uncheckedCast(Class.forName(className, true, classLoader));
        } catch (Exception e) {
            throw new RuntimeException("Instantiation error", e);
        }
    }

//    fun enumValueOf(enumClass: Class<Enum<*>>, enumName: String): Enum<*> {
//
//        return java.lang.Enum.valueOf(enumClass as Class<out Enum<*>>, enumName)
//    }
//
//    fun <T> typeToClass(type: Type): Class<T> {
//        return if (type is Class<*>) {
//            type as Class<T>
//        } else {
//            var typeName = type.typeName
//            var index = typeName.indexOf('<')
//            if (index != -1) {
//                typeName = typeName.substring(0, index)
//            }
//            classForName(typeName)
//        }
//    }

//    @JvmStatic
//    fun <T: Any> toKClass(aClass: Class<T>): KClass<T> {
//        return aClass.kotlin
//    }
//
//    public static <T> Class<T> toClass(Class<?> typeClass) {
//        if (typeClass.isPrimitive())
//            return typeClass.javaPrimitiveType as Class<T> else typeClass.java as Class<T>
//
//    }
}
