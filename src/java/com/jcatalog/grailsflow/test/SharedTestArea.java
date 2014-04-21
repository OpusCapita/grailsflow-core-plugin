package com.jcatalog.grailsflow.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Ivan Baidakou
 */
public class  SharedTestArea {
    public static Map sharedArea = Collections.synchronizedMap(new HashMap<Object, Object>());
}
