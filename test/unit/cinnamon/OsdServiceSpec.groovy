package cinnamon

import grails.plugin.spock.UnitSpec
import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Unroll

/**
 * Unit tests for OsdService
 */
@TestFor(OsdService)
@Mock([Format])
@TestMixin(DomainClassUnitTestMixin)
class OsdServiceSpec extends UnitSpec {

    def setupSpec() {
    }

    @Unroll
    def 'test determine filename'() {
        setup:
        def formats = mockDomain(Format, [
                [name: 'jpeg/jpg format', description: '', extension: 'jpg', contenttype: 'image/jpeg'],
                [name: 'xml format', description: '', extension: 'xml', contenttype: 'application/xml'],
                [name: 'default binary data format', description: '', extension: 'data', contenttype: 'application/octet-stream']
        ])

        expect:
        service.determineFormat(createFile(file)) == Format.findByExtension(extension)

        where:
        file   | extension
        'jpeg' | 'jpg'
        'jpg'  | 'jpg'
        'xml'  | 'xml'
        'foo'  | 'data'
        null   | 'data'
    }

    File createFile(extension) {
        def file
        if (extension == null) {
            file = new File(System.getProperty('java.io.tmpdir'), 'cinnamon-test-no-format')
            file.deleteOnExit()
        }
        else {
            file = File.createTempFile('cinnamon-test', '.' + extension)
            file.deleteOnExit()
        }
        file
    }
}
