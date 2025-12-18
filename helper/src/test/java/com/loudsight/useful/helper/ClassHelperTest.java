package com.loudsight.useful.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold utilities related to casting of types
 *
 * @author munyengm
 */
public class ClassHelperTest {
    @Test
    public void unsafeCastTest() {
        var list = new ArrayList<>();
        List<String> stringList = ClassHelper.uncheckedCast(list);
        Assertions.assertNotNull(stringList);
    }
}
