package com.loudsight.useful.helper

class ClassHelper {
    /**
     * Method to cast a given type to an arbitrary type. The small size of the method means it will likely be in-lined
     * by the JVM compiler The method is may be used to deal with situations where a warning is produced by the compiler
     * stating that a given is an unsafe one i.e. "Type safety: Unchecked cast from O to T" Using this method reduces the
     * number of places where the @SuppressWarnings annotation is required and restricts the suppression of warning to this
     * method only - ensuring that real warnings aren't inadvertently hidden. The method infers the target type from the
     * required return type as illustrated below:<br></br>
     * `
     * // Ex. <br></br>
     * T typedObject = Cast.uncheckedCast(object) // where T is the target type
     * ` *
     *
     * @param object
     * @return the typed object
     */
    companion object {
        fun <T> uncheckedCast(`object`: Any?): T {
            return `object` as T
        }
    }
}