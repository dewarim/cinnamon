package cinnamon

import grails.plugin.springsecurity.annotation.Secured
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.springframework.dao.DataIntegrityViolationException
import cinnamon.utils.ParamParser

@Secured(["hasRole('_superusers')"])
class MetasetTypeController extends BaseController{

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [metasetTypeInstanceList: MetasetType.list(params), metasetTypeInstanceTotal: MetasetType.count()]
    }

    def create() {
        [metasetTypeInstance: new MetasetType(params)]
    }

    def save(String name, String config) {
        def metasetTypeInstance = new MetasetType(name:name, config: config)

        try{
            ParamParser.parseXmlToDocument(config)
        }
        catch (e){
            flash.message = message(code: 'error.xml.config') //, args:[params.config]
            render(view: "create", model: [metasetTypeInstance: metasetTypeInstance])
            return
        }

        if (!metasetTypeInstance.save(flush: true)) {
            render(view: "create", model: [metasetTypeInstance: metasetTypeInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'metasetType.label', default: 'MetasetType'), metasetTypeInstance.id])
        redirect(action: "show", id: metasetTypeInstance.id)
    }

    def show() {
        def metasetTypeInstance = MetasetType.get(params.id)
        if (!metasetTypeInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'metasetType.label', default: 'MetasetType'), params.id])
            redirect(action: "list")
            return
        }

        [metasetTypeInstance: metasetTypeInstance]
    }

    def edit() {
        def metasetTypeInstance = MetasetType.get(params.id)
        if (!metasetTypeInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'metasetType.label', default: 'MetasetType'), params.id])
            redirect(action: "list")
            return
        }

        [metasetTypeInstance: metasetTypeInstance]
    }

    def update(Long id, String config) {
        def metasetTypeInstance = MetasetType.get(id)
        if (!metasetTypeInstance) {
            log.debug("Could not find MetasetType#${id}")
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'metasetType.label', default: 'MetasetType'), params.id])
            redirect(action: "list")
            return
        }

        if (params.obj_version) {
            def version = params.obj_version.toLong()
            if (metasetTypeInstance.version > version) {
                metasetTypeInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'metasetType.label', default: 'MetasetType')] as Object[],
                          "Another user has updated this MetasetType while you were editing")
                render(view: "edit", model: [metasetTypeInstance: metasetTypeInstance])
                return
            }
        }

        metasetTypeInstance.properties['name', 'config'] = params

        try{
            ParamParser.parseXmlToDocument(config)
        }
        catch (Exception e){
//            log.debug("failed to parse config:",e)
            flash.message = message(code: 'error.xml.config') //, args:[params.config]
            render(view: "edit", model: [metasetTypeInstance: metasetTypeInstance])
            return
        }

        if (!metasetTypeInstance.save(flush: true)) {
            render(view: "edit", model: [metasetTypeInstance: metasetTypeInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'metasetType.label', default: 'MetasetType'), metasetTypeInstance.id])
        redirect(action: "show", id: metasetTypeInstance.id)
    }

    def delete() {
        def metasetTypeInstance = MetasetType.get(params.id)
        if (!metasetTypeInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'metasetType.label', default: 'MetasetType'), params.id])
            redirect(action: "list")
            return
        }

        try {
            metasetTypeInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'metasetType.label', default: 'MetasetType'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'metasetType.label', default: 'MetasetType'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    def updateList () {
        setListParams()
        render(template: 'metasetTypeList', model:[metasetTypeInstanceList: MetasetType.list(params)])
    }

    //---------------------------------------------------
    // Cinnamon XML Server API
    @Secured(["isAuthenticated()"])
    def listXml() {
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("metasetTypes")
        MetasetType.list().each{metasetType ->
            root.add(MetasetType.asElement("metasetType", metasetType))
        }
        render(contentType: 'application/xml', text:doc.asXML())
    }
}
