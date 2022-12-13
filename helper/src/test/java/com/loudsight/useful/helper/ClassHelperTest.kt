package com.loudsight.useful.helper

import com.loudsight.utilities.helper.ClassHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.ArrayList

/**
 * Class to hold utilities related to casting of types
 *
 * @author munyengm
 */
class ClassHelperTest {
    @Test
    fun unsafeCastTest() {
        val list: List<*> = ArrayList<Any>()
        val stringList: List<String> = ClassHelper.uncheckedCast(list)
        Assertions.assertNotNull(stringList)
    }
}