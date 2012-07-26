package cinnamon

import grails.plugins.springsecurity.Secured
import cinnamon.trigger.ChangeTrigger
import cinnamon.trigger.ChangeTriggerType

@Secured(["hasRole('_superusers')"])
class ChangeTriggerController extends BaseController {

    def create() {
        render(template: 'create', model: [changeTrigger: null])
    }

    def index() {
        setListParams()
        [changeTriggerList: ChangeTrigger.list(params)]
    }

    def save() {
        ChangeTrigger changeTrigger = new ChangeTrigger()
        try {
            updateFields(changeTrigger)
            changeTrigger.save(failOnError: true, flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save changeTrigger: ", e)
            render(status: 503, template: 'create',
                    model: [changeTrigger: changeTrigger,
                            errorMessage: message(code: e.getLocalizedMessage()).encodeAsHTML()])
            return
        }
        setListParams()
        render(template: 'list_table', model: [changeTriggerList: ChangeTrigger.list(params)])
    }

    def list() {
        redirect(action: 'index')
    }

    def edit() {
        render(template: 'edit', model: [changeTrigger: ChangeTrigger.get(Long.parseLong(params.id))])
    }

    def cancelEdit() {
        render(template: 'row', model: [changeTrigger: ChangeTrigger.get(Long.parseLong(params.id))])
    }

    def delete() {
        ChangeTrigger changeTrigger = ChangeTrigger.get(params.id)
        try {
            changeTrigger.delete(flush: true)
        }
        catch (Exception e) {
            flash.message = e.getLocalizedMessage()
            render(status: 503, text: message(code: e.getLocalizedMessage()))
            return
        }
        setListParams()
        render(template: 'list_table', model: [changeTriggerList: ChangeTrigger.list(params)])
    }

    protected void updateFields(changeTrigger) {
        changeTrigger.triggerType = inputValidationService.checkObject(ChangeTriggerType.class, params.triggerType)
        changeTrigger.controller = inputValidationService.checkAndEncodeText(params, 'controller', 'changeTrigger.controller')
        changeTrigger.action = inputValidationService.checkAndEncodeText(params, 'action', 'changeTrigger.action')
        changeTrigger.config = params.config
        params.ranking = params.ranking ?: '0'
        changeTrigger.ranking = inputValidationService.checkAndEncodeInteger(params, 'ranking', 'changeTrigger.ranking')
        changeTrigger.preTrigger = params.preTrigger ? true : false
        changeTrigger.postTrigger = params.postTrigger ? true : false
        changeTrigger.active = params.active ? true : false
    }

    def update() {
        ChangeTrigger ct = ChangeTrigger.get(params.id)
        try {
            updateFields(ct)
            ct.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save changeTrigger: ", e)
            render(status: 503,
                    template: 'edit', model: [changeTrigger: ct, errorMessage: e.getLocalizedMessage()])
            return
        }
        render(template: 'row', model: [changeTrigger: ct])
    }

    def updateList() {
        setListParams()
        render(template: 'list_table', model: [changeTriggerList: ChangeTrigger.list(params)])
    }
}
