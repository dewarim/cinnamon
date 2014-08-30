package cinnamon

import cinnamon.i18n.UiLanguage
import grails.plugin.springsecurity.annotation.Secured
import cinnamon.i18n.Language
import cinnamon.global.Constants
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element

@Secured(["hasRole('_superusers')"])
class LanguageController extends BaseController {

    def create () {}

    def list () {
        setListParams()
        [languageList: Language.list(params)]
    }

    def show () {
        [language: inputValidationService.checkObject(Language.class, params.id)]
    }

    def edit () {
        [language: inputValidationService.checkObject(Language.class, params.id)]
    }

    def update () {
        log.debug("called update with " + params.dump())
        Language lang = (Language) inputValidationService.checkObject(Language.class, params.id)
        try {
            updateFields(lang)
            lang.save()
        }
        catch (Exception e) {
            log.debug("failed to update Language", e)
            flash.message = message(code: e.getLocalizedMessage()).encodeAsHTML()
            return redirect(action: 'edit', params: [id: params.id])
        }
        flash.message = message(code: "language.update.success", args: [lang.isoCode.encodeAsHTML()])
        return redirect(action: 'show', params: [id: lang.id])

    }

    protected void updateFields(Language language) {
        String isoCode = params.isoCode
        if (isoCode?.length() != 0) {
            if (isoCode.getBytes("UTF-8").length > Constants.MAX_ISO_CODE_LENGTH) {
                //noinspection GroovyAssignabilityCheck
                throw new RuntimeException(message(code: "error.param.isoCode.length", args: [Constants.MAX_ISO_CODE_LENGTH]))
            }
            if (!isoCode.equals(language.isoCode)) {
                if (Language.findByIsoCode(isoCode)) {
                    throw new RuntimeException(message(code: "error.object.already.exits", args: ["(?)"]))
                }
                else {
                    language.isoCode = params.isoCode
                }
            }
        }
        else {
            throw new RuntimeException("error.param.isoCode.null")
        }
        language.metadata = params.metadata
    }

    def delete () {
        String isoCode
        try {
            Language lang = (Language) inputValidationService.checkObject(Language.class, params.id)
            isoCode = lang.isoCode
            lang.delete()
            flash.message = message(code: 'language.delete.success', args: [isoCode?.encodeAsHTML()]) // TODO: add name
        }
        catch (Exception e) {
            log.debug("failed to delete language:",e)
            flash.message = message(code: e.getLocalizedMessage())
        }
        return redirect(action: 'list')
    }

    /**
     * Called after the 'save' button in create.gsp is called
     */
    def save () {
        Language language = new Language()
        try {
            updateFields(language)
            language.save(failOnError: true, flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save language:",e)
            flash.message = message(code: e.getLocalizedMessage()).encodeAsHTML()
            return redirect(action: 'create')
        }
        return redirect(action: 'show', params: [id: language.id])
    }

    def updateList () {
        setListParams()
        render(template: 'languageList', model: [languageList: Language.list(params)])
    }

    //---------------------------------------------------
    // Cinnamon XML Server API
    @Secured(["isAuthenticated()"])
    def listXml() {
        Document doc = DocumentHelper.createDocument()
        Element root = doc.addElement("languages");
        Language.list().each {language->
            language.toXmlElement(root)
        }
        render(contentType: 'application/xml', text: doc.asXML())
    }
}
