package cinnamon

import grails.plugins.springsecurity.Secured
import org.springframework.dao.DataIntegrityViolationException
import cinnamon.utils.ParamParser

@Secured(["hasRole('_superusers')"])
class MetasetTypeController {

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

    def save() {
        def metasetTypeInstance = new MetasetType(params)

        try{
            ParamParser.parseXmlToDocument(params.config)
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

    def update() {
        def metasetTypeInstance = MetasetType.get(params.id)
        if (!metasetTypeInstance) {
            log.debug("Could not find MetasetType#${params.id}")
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

        metasetTypeInstance.properties['name','description','config'] = params

        try{
            ParamParser.parseXmlToDocument(params.config)
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
}
