package cinnamon

import cinnamon.exceptions.CinnamonException
import cinnamon.utils.ParamParser
import grails.plugin.springsecurity.annotation.Secured
import org.dom4j.Document

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

    //------------------------------ XML Client API: -----------------------
    /**
     * The getConfigEntry command retrieves the config entry with the given name.
     * Access to config entries is limited to superusers unless the XML config entry
     * contains the element {@code <isPublic>true</isPublic> }
     * @param name: the name of the configuration entry.
     * @return XML-Response: The XML content of the specified configuration entry.
     */
    def getConfigEntryXml(String name) {
        try {
            ConfigEntry configEntry = ConfigEntry.findByName(name);
            if (!configEntry) {
                throw new CinnamonException("error.missing.config");
            }            
            Document doc = ParamParser.parseXmlToDocument(configEntry.config);
            if (userService.isSuperuser(userService.user) || doc.selectSingleNode("//isPublic[text()='true']") != null) {
                render(contentType: 'application/xml', text: configEntry.config)
            }
            else {
                throw new CinnamonException("error.access.denied");
            }
        }
        catch (Exception e) {
            log.debug("Failed to fetch config entry '$name'", e)
        }
    }

    /**
     * The setConfigEntry command sets the config entry with the given name.
     * This command is only available to superusers. To allow non-superusers to
     * view / download config entries, add the element {@code <isPublic>true</isPublic> }
     * to the XML of the config.
     *
     * @param name name of the config entry - if it exists, replace the current content.</li>
     * @param config XML content of the config entry.</li>
     * @return XML-Response:
     *         <pre>{@code
     *         <configEntryId>$configEntryId</configEntryId>
     *         }</pre>
     */
    def setConfigEntryXml(String name, String config) {
        try{
        if(name == null || name.trim().length() == 0){
            throw new CinnamonException("error.param.name");
        }
        if(! userService.isSuperuser(userService.user) ){
            throw new CinnamonException("error.access.denied");
        }

        Document configDoc = ParamParser.parseXmlToDocument(config ?:'<config/>', "error.parse.config");
        ConfigEntry configEntry = ConfigEntry.findByName(name);
        if(configEntry == null){
            configEntry = new ConfigEntry();
            configEntry.setName(name);
            configEntry.setConfig(configDoc.asXML());
            configEntry.save()
        }
        else{
            configEntry.setConfig(configDoc.asXML());
        }
        
        render(contentType: 'application/xml', text: "<configEntryId>"+configEntry.id+"</configEntryId>")
        }
        catch (Exception e){
            renderExceptionXml(e.message)
        }
    }
    
}
