package cinnamon

import grails.plugins.springsecurity.Secured

@Secured(["hasRole('_superusers')"])
class ConfigEntryController extends BaseController{

    def create () {
        render(template: 'create', model: [configEntry: null])
    }

    def index () {
        setListParams()
        [configEntryList: ConfigEntry.list(params)]
    }

    def save () {
        ConfigEntry configEntry = new ConfigEntry()
        try {
            updateFields(configEntry)
            configEntry.save(failOnError: true, flush:true)
        }
        catch (Exception e) {
            log.debug("failed to save configEntry: ", e)
            render(status: 503, template:'create',
                    model:[configEntry:configEntry,
                            errorMessage : message(code:e.getLocalizedMessage()).encodeAsHTML()])
            return
        }
        setListParams()
        render(template: 'list_table', model: [configEntryList: ConfigEntry.list(params)])
    }

    def list () {
        redirect(action: 'index')
    }

    def edit () {
        render(template: 'edit', model: [configEntry: inputValidationService.checkObject(ConfigEntry.class, params.id)])
    }

    def cancelEdit () {
        render(template: 'row', model: [configEntry:  inputValidationService.checkObject(ConfigEntry.class, params.id)])
    }

    def delete () {
        ConfigEntry configEntry =  (ConfigEntry) inputValidationService.checkObject(ConfigEntry.class, params.id)
        try {
            configEntry.delete(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to delete ConfigEntry:",e)
            render(status: 503, text: message(code: e.getLocalizedMessage()))
            return
        }
        setListParams()
        render(template: 'list_table', model: [configEntryList: ConfigEntry.list(params), resetMessage:true])
    }

    protected void updateFields(configEntry) {
        configEntry.name = inputValidationService.checkAndEncodeText(params, 'name', 'configEntry.name')
        configEntry.config = params.config
    }

    def update () {
        ConfigEntry ce = (ConfigEntry) inputValidationService.checkObject(ConfigEntry.class, params.id)
        try {
            updateFields(ce)
            ce.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save configEntry: ", e)
            render(template: 'edit', model: [configEntry: ce, errorMessage: e.getLocalizedMessage()])
            return
        }
        render(template: 'row', model: [configEntry: ce])
    }

    def updateList () {
        setListParams()
        render(template: 'list_table', model:[configEntryList:ConfigEntry.list(params)])
    }


}
