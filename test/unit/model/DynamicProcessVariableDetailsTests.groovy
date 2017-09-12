package model

import com.jcatalog.grailsflow.bean.DynamicProcessVariableDetails
import spock.lang.Specification

class DynamicProcessVariableDetailsTests extends Specification {

    void "test failed format of dynamicName property in com.jcatalog.grailsflow.bean.DynamicProcessVariableDetails class"() {
        when:
        new DynamicProcessVariableDetails().getNodeKey()

        then:
        thrown NullPointerException

        when:
        new DynamicProcessVariableDetails(dynamicName: 'name_123sd').getNodeKey()

        then:
        thrown IllegalArgumentException

        when:
        new DynamicProcessVariableDetails(dynamicName: 'name__123').getNodeKey()

        then:
        thrown IllegalArgumentException

        when:
        new DynamicProcessVariableDetails(dynamicName: 'na_me_123').getNodeKey()

        then:
        thrown IllegalArgumentException

        when:
        new DynamicProcessVariableDetails(dynamicName: 'name_123abc').getNodeKey()

        then:
        thrown IllegalArgumentException

        when:
        new DynamicProcessVariableDetails(dynamicName: '_name_123').getNodeKey()

        then:
        thrown IllegalArgumentException

        when:
        new DynamicProcessVariableDetails(dynamicName: 'name_value').getNodeKey()

        then:
        thrown IllegalArgumentException
    }

    void "test normal format of dynamicName property in com.jcatalog.grailsflow.bean.DynamicProcessVariableDetails class"() {
        setup:
        Long nodeKey = null

        when:
        nodeKey = new DynamicProcessVariableDetails(dynamicName: 'name_123').getNodeKey()

        then:
        nodeKey == 123L

        when:
        nodeKey = new DynamicProcessVariableDetails(dynamicName: 'Name_123').getNodeKey()

        then:
        nodeKey == 123L

        when:
        nodeKey = new DynamicProcessVariableDetails(dynamicName: 'MainName$Var_123').getNodeKey()

        then:
        nodeKey == 123L

        when:
        nodeKey = new DynamicProcessVariableDetails(dynamicName: 'name-new_123').getNodeKey()

        then:
        nodeKey == 123L

        when:
        nodeKey = new DynamicProcessVariableDetails(dynamicName: 'name$variable_123').getNodeKey()

        then:
        nodeKey == 123L

        when:
        nodeKey = new DynamicProcessVariableDetails(dynamicName: '$variable_123').getNodeKey()

        then:
        nodeKey == 123L
    }
}