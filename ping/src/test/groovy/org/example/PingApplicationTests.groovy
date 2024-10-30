package org.example

import spock.lang.Specification

class PingApplicationTests extends Specification {

    def "should start application and no exception thrown"() {
        when:
        PingApplication.main()
        then:
        noExceptionThrown()
    }
}
