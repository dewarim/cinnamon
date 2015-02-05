package cinnamon

import cinnamon.trigger.ChangeTrigger
import cinnamon.trigger.ChangeTriggerType
import cinnamon.trigger.impl.LifecycleStateAuditTrigger
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([ChangeTriggerType, ChangeTrigger])
class ChangeTriggerTypeControllerSpec extends Specification {
    
    def inputValidationService = new InputValidationService()
    
    def "update changeTriggerType"(){
        setup:
        def controller = new ChangeTriggerTypeController()
        controller.inputValidationService = inputValidationService
        def ctt = new ChangeTriggerType('LSAT', LifecycleStateAuditTrigger)
        ctt.save(flush: true)
        
        when:
        controller.update(ctt.id, 'foo', LifecycleStateAuditTrigger.name)
        
        then:        
        controller.response.contentAsString.contains("<td>foo</td>")
        
    }  
    
    def "update changeTriggerType with existing changeTrigger"(){
        setup:
        def controller = new ChangeTriggerTypeController()
        controller.inputValidationService = inputValidationService
        def ctt = new ChangeTriggerType('LSAT', LifecycleStateAuditTrigger)
        ctt.save(flush: true)
        def ct = new ChangeTrigger('foo', 'bar', ctt, 0, true, true, true, '<meta/>')
        ct.save(flush: true)
        
        when:
        controller.update(ctt.id, 'foo', LifecycleStateAuditTrigger.name)
        
        then:        
        controller.response.contentAsString.contains("<td>foo</td>")
        
    }
}
