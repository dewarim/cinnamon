package cinnamon.controller

import cinnamon.CinnamonController
import cinnamon.InfoService
import cinnamon.Session
import cinnamon.UserAccount
import cinnamon.i18n.UiLanguage
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Specification

@TestFor(CinnamonController)
@TestMixin(DomainClassUnitTestMixin)
@Mock([UserAccount, UiLanguage, Session, InfoService])
class CinnamonControllerSpec extends Specification {

    def "connect via connect method"() {
        setup:
        new UserAccount("admin", "the-admin", ".", '.').save()
        new UiLanguage(isoCode: "und").save()
        params.user = 'admin'
        params.repository = 'demo'
        params.pwd = 'the-admin'
        params.machine = 'localhost'

        when:
        controller.connect()

        then:
        response.contentAsString.matches("<connection><ticket>.*</ticket></connection>")
        Session.count() == 1

    }

    def "connect via connect method and wrong password"() {
        setup:
        new UserAccount("admin", "the-admin", ".", '.').save()
        params.user = 'admin'
        params.repository = 'demo'
        params.pwd = 'nope'
        params.machine = 'localhost'

        when:
        controller.connect()

        then:
        response.contentAsString
                .matches("<error><code>error.wrong.password</code><message>error.wrong.password</message></error>")

    }

    def "connect via connect method and wrong user"() {
        setup:
        params.user = 'no-boddy'
        params.repository = 'demo'
        params.pwd = 'nope'
        params.machine = 'localhost'

        when:
        controller.connect()

        then:
        response.contentAsString
                .matches("<error><code>error.user.not.found</code><message>error.user.not.found</message></error>")

    }
}
