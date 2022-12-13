package com.loudsight.useful.helper

import com.loudsight.useful.helper.ClassHelper.Companion.uncheckedCast
import kotlin.jvm.kotlin
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Class to hold utilities related to casting of types
 *
 * @author munyengm
 */
object JvmClassHelper {
    private val PRIMITIVES = arrayListOf(
        Boolean::class.java,
        Boolean::class.javaPrimitiveType,
        Int::class.java,
        Int::class.javaPrimitiveType,
        Double::class.java,
        Double::class.javaPrimitiveType,
        Float::class.java,
        Float::class.javaPrimitiveType,
        Long::class.java,
        Long::class.javaPrimitiveType,
        String::class.java
    ).associateBy {
        return@associateBy it!!.name
    }

    @JvmStatic
    fun isPrimitive(entity: Class<*>): Boolean {
        val className = getClassName(entity.name)
        return PRIMITIVES.containsKey(className)
    }

    fun isPrimitive(primitiveName: String): Boolean {
        val className = getClassName(primitiveName)
        return PRIMITIVES.containsKey(className)
    }

    fun <T> getPrimitiveClass(primitiveName: String): Class<T> {
        val className = getClassName(primitiveName)
        return uncheckedCast(PRIMITIVES[className])
    }

    private fun getClassName(primitiveName: String): String {
        var className = primitiveName
        if (className.equals("kotlin.String")) {
            className = "java.lang.String"
        } else if (className.startsWith("kotlin.")) {
            className = className.replace("kotlin.", "")
            className = className.toLowerCase()
        }

        return className
    }

    @JvmStatic
    fun <T> classForName(className: String): Class<T> {
        return if (isPrimitive(className)) {
            getPrimitiveClass(className)
        } else try {
            uncheckedCast<Class<T>>(Class.forName(className))
        } catch (e: Exception) {
            ExceptionHelper.uncheckedThrow<RuntimeException>(e)
            throw RuntimeException("NOT POSSIBLE", e)
        }
    }

    fun enumValueOf(enumClass: Class<Enum<*>>, enumName: String): Enum<*> {

        return java.lang.Enum.valueOf(enumClass as Class<out Enum<*>>, enumName)
    }

    fun <T> typeToClass(type: Type): Class<T> {
        return if (type is Class<*>) {
            type as Class<T>
        } else {
            var typeName = type.typeName
            val index = typeName.indexOf('<')
            if (index != -1) {
                typeName = typeName.substring(0, index)
            }
            classForName(typeName)
        }
    }

    @JvmStatic
    fun <T: Any> toKClass(aClass: Class<T>): KClass<T> {
        return aClass.kotlin
    }

    @JvmStatic
    fun <T: Any> toClass(typeClass: KClass<*>): Class<out T> {
        return uncheckedCast(
            if (typeClass.javaPrimitiveType != null) typeClass.javaPrimitiveType else typeClass.java
        )
    }
}