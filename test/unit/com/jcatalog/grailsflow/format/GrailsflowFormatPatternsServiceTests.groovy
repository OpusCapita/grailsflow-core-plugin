package com.jcatalog.grailsflow.format

import spock.lang.Specification

class GrailsflowFormatPatternsServiceTests extends Specification {

    def "default values: datePatterns"() {
        setup:
        def service = new GrailsflowFormatPatternsService()

        when:
        service.grailsApplication = [config: new ConfigObject ()]

        then:
        // no configuration
        service.getDatePatterns() == service.defaultDatePatterns

        when:
        service.grailsApplication = [config: new ConfigSlurper().parse('''
            grailsflow {
            }
        ''')]

        then:
        // still no configuration
        service.getDatePatterns() == service.defaultDatePatterns

        when:
        service.grailsApplication = [config: new ConfigSlurper().parse('''
            grailsflow {
                format {
                }
            }
        ''')]

        then:
        // still no configuration
        service.getDatePatterns() == service.defaultDatePatterns
    }

    def "default values: dateTimePatterns"() {
        setup:
        def service = new GrailsflowFormatPatternsService()

        when:
        service.grailsApplication = [config: new ConfigObject ()]

        then:
        // no configuration
        service.getDateTimePatterns() == service.defaultDateTimePatterns

        when:
        service.grailsApplication = [config: new ConfigSlurper().parse('''
            grailsflow {
            }
        ''')]

        then:
        // still no configuration
        service.getDateTimePatterns() == service.defaultDateTimePatterns

        when:
        service.grailsApplication = [config: new ConfigSlurper().parse('''
            grailsflow {
                format {
                }
            }
        ''')]

        then:
        // still no configuration
        service.getDateTimePatterns() == service.defaultDateTimePatterns
    }

    def "default values: numberPatterns"() {
        setup:
        def service = new GrailsflowFormatPatternsService()

        when:
        service.grailsApplication = [config: new ConfigObject ()]

        then:
        // no configuration
        service.getNumberPatterns() == service.defaultNumberPatterns

        when:
        service.grailsApplication = [config: new ConfigSlurper().parse('''
            grailsflow {
            }
        ''')]

        then:
        // still no configuration
        service.getNumberPatterns() == service.defaultNumberPatterns

        when:
        service.grailsApplication = [config: new ConfigSlurper().parse('''
            grailsflow {
                format {
                }
            }
        ''')]

        then:
        // still no configuration
        service.getNumberPatterns() == service.defaultNumberPatterns
    }

    def "default values: defaultDecimalSeparators"() {
        setup:
        def service = new GrailsflowFormatPatternsService()

        when:
        service.grailsApplication = [config: new ConfigObject ()]

        then:
        // no configuration
        service.getDecimalSeparators() == service.defaultDecimalSeparators

        when:
        service.grailsApplication = [config: new ConfigSlurper().parse('''
            grailsflow {
            }
        ''')]

        then:
        // still no configuration
        service.getDecimalSeparators() == service.defaultDecimalSeparators

        when:
        service.grailsApplication = [config: new ConfigSlurper().parse('''
            grailsflow {
                format {
                }
            }
        ''')]

        then:
        // still no configuration
        service.getDecimalSeparators() == service.defaultDecimalSeparators
    }

    def "configured values: datePatterns"() {
        setup:
        def service = new GrailsflowFormatPatternsService()

        when:
        def datePatterns = ['it': 'dd-mm-yyy']
        def dateTimePatterns = ['es': 'dd-mm-yyy HH:mm:ss']
        def numberPatterns = ['fr': '0.0']
        def decimalSeparators = ['dk': '.']

        def config = new ConfigObject()
        config.putAll([
            grailsflow: [
                format: [
                    'datePatterns': { datePatterns },
                    'dateTimePatterns': { dateTimePatterns },
                    'numberPatterns': { numberPatterns },
                    'decimalSeparators': { decimalSeparators }
                ]
            ]
        ])
        service.grailsApplication = [
            'config': config
        ]

        then:
        service.datePatterns == datePatterns
        service.dateTimePatterns == dateTimePatterns
        service.numberPatterns == numberPatterns
        service.decimalSeparators == decimalSeparators
    }

    def "check applicationContext accessibility"() {
        setup:
        def service = new GrailsflowFormatPatternsService()

        when:
        def applicationContext = [
            datePatterns: ['it': 'dd-mm-yyy'],
            dateTimePatterns: ['es': 'dd-mm-yyy HH:mm:ss'],
            numberPatterns: ['fr': '0.0'],
            decimalSeparators: ['dk': '.']
        ]

        def config = new ConfigObject()
        config.putAll([
            grailsflow: [
                format: [
                    'datePatterns': { applicationContext.datePatterns },
                    'dateTimePatterns': { applicationContext.dateTimePatterns },
                    'numberPatterns': { applicationContext.numberPatterns },
                    'decimalSeparators': { applicationContext.decimalSeparators }
                ]
            ]
        ])
        service.grailsApplication = [
            'mainContext': applicationContext,
            'config': config
        ]

        then:
        service.datePatterns == applicationContext.datePatterns
        service.dateTimePatterns == applicationContext.dateTimePatterns
        service.numberPatterns == applicationContext.numberPatterns
        service.decimalSeparators == applicationContext.decimalSeparators
    }
}