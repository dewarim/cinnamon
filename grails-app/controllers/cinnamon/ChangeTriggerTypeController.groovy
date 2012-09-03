package cinnamon

import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import cinnamon.trigger.ChangeTriggerType
import cinnamon.trigger.ITrigger
import cinnamon.trigger.ChangeTrigger
import cinnamon.trigger.impl.RelationChangeTrigger

@Secured(["hasRole('_superusers')"])
class ChangeTriggerTypeController extends BaseController {

    def create () {
//        ChangeTriggerType changeTriggerType = new ChangeTriggerType(params)
        render(template: 'create', model: [changeTriggerType: null,
                 triggers:ConfigurationHolder.config.triggerClasses,
        ])
    }

    def index () {
        setListParams()
        [changeTriggerTypeList: ChangeTriggerType.list(params)]
    }

    def save () {
        ChangeTriggerType changeTriggerType = new ChangeTriggerType()
        try {
            updateFields(changeTriggerType)
            changeTriggerType.save(failOnError: true, flush:true)
        }
        catch (Exception e) {
            log.debug("failed to save changeTriggerType: ", e)
            render(status: 503, template:'create',
                    model:[changeTriggerType:changeTriggerType,
                           triggers:ConfigurationHolder.config.triggerClasses,
                           errorMessage : e.getLocalizedMessage().encodeAsHTML()])
            return
        }
        setListParams()
        render(template: 'list_table', model: [changeTriggerTypeList: ChangeTriggerType.list(params)])
    }

    def list () {
        redirect(action: 'index')
    }

    def edit () {
        render(template: 'edit', model: [changeTriggerType: ChangeTriggerType.get(Long.parseLong(params.id)),
            triggers:ConfigurationHolder.config.triggerClasses,
        ])
    }

    def cancelEdit () {
        render(template: 'row', model: [changeTriggerType: ChangeTriggerType.get(Long.parseLong(params.id))])
    }

    def delete () {
        ChangeTriggerType changeTriggerType = ChangeTriggerType.get(params.id)
        try {
            if(ChangeTrigger.findByTriggerType(changeTriggerType)){
                throw new RuntimeException("error.object.in.use")
            }
            changeTriggerType.delete(flush: true)
        }
        catch (Exception e) {
            log.debug("Failed to delete CTT", e)
            render(status: 503, template:'/shared/showError', model:[infoMessage:message(code: e.getLocalizedMessage())])
            return
        }
        setListParams()
        render(template: 'list_table', model: [changeTriggerTypeList: ChangeTriggerType.list(params)])
    }

    protected void updateFields(changeTriggerType) {
        changeTriggerType.name = inputValidationService.checkAndEncodeName(params.name, changeTriggerType)
        changeTriggerType.description = inputValidationService.checkAndEncodeText(params, 'description', 'changeTriggerType.description')
        try{
            def x = new RelationChangeTrigger()
            // testing if class can be instantiated:
            log.debug("looking for class: ${params.triggerClass}")
            ITrigger iTrigger = (ITrigger) Class.forName(params.triggerClass, true, Thread.currentThread().contextClassLoader ).newInstance()
            changeTriggerType.triggerClass = Class.forName(params.triggerClass, true, Thread.currentThread().contextClassLoader)
        }
        catch (ClassCastException e){
            throw new RuntimeException("error.during.iTrigger.cast")
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException("error.class.not.found")
        }
    }

    def update () {
        ChangeTriggerType changeTriggerType = ChangeTriggerType.get(params.id)
        try {
            updateFields(changeTriggerType)
            changeTriggerType.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save changeTriggerType: " + e.getLocalizedMessage())
            render(template: 'edit',
                    model: [changeTriggerType: changeTriggerType,
                    errorMessage: e.getLocalizedMessage()])
            return
        }
        render(template: 'row', model: [changeTriggerType: changeTriggerType])
    }

    def updateList () {
        setListParams()
        render(template: 'list_table', model:[changeTriggerTypeList:ChangeTriggerType.list(params)])
    }

}
