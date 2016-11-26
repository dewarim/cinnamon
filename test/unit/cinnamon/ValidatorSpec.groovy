package cinnamon

import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Specification

/**
 * Unit tests for Validator
 */
@Mock([Acl, AclEntry, UserAccount, CmnGroup, ObjectSystemData, CmnGroupUser])
@TestMixin(DomainClassUnitTestMixin)
class ValidatorSpec extends Specification {

    def validator = new Validator()

    def osd
    def user
    def owner 
    def acl 
    def everyoneGroup 
    def ownerGroup 
    def otherGroup 
    def everyoneAclEntry
    def ownerAclEntry
    def otherGroupAclEntry
    
    def setupSpec(){
        
    }

    def setup() {
        osd = new ObjectSystemData()
        user = new UserAccount("user", "", "", "")
        owner = new UserAccount("owner", "", "", "")
        acl = new Acl()
        everyoneGroup = new CmnGroup(name: CmnGroup.ALIAS_EVERYONE)
        everyoneGroup.save()
        ownerGroup = new CmnGroup(name: CmnGroup.ALIAS_OWNER)
        ownerGroup.save()
        otherGroup = new CmnGroup(name: "authors")
        otherGroup.save()
        everyoneAclEntry = new AclEntry(acl, everyoneGroup)
        everyoneAclEntry.save()
        ownerAclEntry = new AclEntry(acl, ownerGroup)
        ownerAclEntry.save()
        otherGroupAclEntry = new AclEntry(acl, otherGroup)
        otherGroupAclEntry.save()
        osd.acl = acl
        osd.owner = owner
    }


    def 'without an object, return empty list of aliasEntries'() {
        when:
        def aclEntries = validator.findAliasEntries(acl, user, null)

        then:
        aclEntries.empty
    }

    def 'for normal user, aclEntries should contain everyone entry'() {
        when:
        def aclEntries = validator.findAliasEntries(acl, user, osd)

        then:
        aclEntries.find { it.group.name == CmnGroup.ALIAS_EVERYONE }
        !aclEntries.find { it.group.name == CmnGroup.ALIAS_OWNER }
        aclEntries.size() == 1

    }

    def 'for owner user, aclEntries should contain everyone and owner entry'() {
        when:
        def aclEntries = validator.findAliasEntries(acl, owner, osd)

        then:
        aclEntries.find { it.group.name == CmnGroup.ALIAS_EVERYONE }
        aclEntries.find { it.group.name == CmnGroup.ALIAS_OWNER }
        aclEntries.size() == 2

    }

}
