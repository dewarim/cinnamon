package cinnamon

import grails.plugins.springsecurity.Secured
import cinnamon.trigger.ChangeTriggerType
import cinnamon.trigger.ITrigger
import cinnamon.trigger.ChangeTrigger

@Secured(["hasRole('_superusers')"])
class ChangeTriggerTypeController extends BaseController {

    def create () {
//        ChangeTriggerType changeTriggerType = new ChangeTriggerType(params)
        render(template: 'create', model: [changeTriggerType: null,
                 triggers:grailsApplication.config.triggerClasses,
        ])
    }

    def index () {
        setListParams()
        [changeTriggerTypeList: ChangeTriggerType.list(params)]
    }

    def save (String name, String triggerClass) {
        ChangeTriggerType changeTriggerType = new ChangeTriggerType()
        try {
            updateFields(changeTriggerType, name, triggerClass)
            changeTriggerType.save(failOnError: true, flush:true)
        }
        catch (Exception e) {
            log.debug("failed to save changeTriggerType: ", e)
            render(status: 503, template:'create',
                    model:[changeTriggerType:changeTriggerType,
                           triggers: grailsApplication.config.triggerClasses,
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
            triggers:grailsApplication.config.triggerClasses,
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

    protected void updateFields(ChangeTriggerType changeTriggerType, String name, String triggerClass) {
        changeTriggerType.name = inputValidationService.checkAndEncodeName(name, changeTriggerType)
        try{
            // testing if class can be instantiated:
            log.debug("looking for class: ${triggerClass}")
            Class iTriggerClass = Class.forName(triggerClass, true, Thread.currentThread().contextClassLoader )
            ITrigger iTrigger = (ITrigger) iTriggerClass.newInstance()
            changeTriggerType.triggerClass = (Class<? extends ITrigger>) iTriggerClass            
        }
        catch (ClassCastException e){
            throw new RuntimeException("error.during.iTrigger.cast")
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException("error.class.not.found")
        }        
    }

    def update (Long id, String name, String triggerClass) {
        ChangeTriggerType changeTriggerType = ChangeTriggerType.get(id)
        try {
            updateFields(changeTriggerType, name, triggerClass)
            log.debug("changeTriggerType: ${changeTriggerType.dump()}")
            changeTriggerType.validate()
            log.debug("${changeTriggerType.errors}")
            changeTriggerType.save()
            render(template: 'row', model: [changeTriggerType: changeTriggerType])
        }
        catch (Exception e) {
            log.debug("failed to save changeTriggerType: ", e )
            render(template: 'edit',
                    model: [changeTriggerType: changeTriggerType,
                    errorMessage: e.getLocalizedMessage()])
        }
    }

    def updateList () {
        setListParams()
        render(template: 'list_table', model:[changeTriggerTypeList:ChangeTriggerType.list(params)])
    }

}
