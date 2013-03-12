package cinnamon

import grails.plugins.springsecurity.Secured
import cinnamon.index.IndexGroup
import org.dom4j.DocumentHelper
import org.dom4j.Element;

@Secured(["hasRole('_superusers')"])
class IndexGroupController extends BaseController{

	def sessionFactory // inject Hibernate session 

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index () {
        redirect(action: "list", params: params)
    }

    def list () {
        setListParams()
        [indexGroupList: IndexGroup.list(params)]
    }

    def create () {
//        def indexGroupInstance = new IndexGroup()
//        indexGroupInstance.properties = params
//        return [indexGroupInstance: indexGroupInstance]
    }

    def save () {
    	if (params.name?.length() == 0) {
    		flash.error = message(code: 'error.create.indexGroup.empty.name')
    		render(view: "create", model: [:])
    		return
    	}
//        IndexGroup.class.declaredMethods.each{
//            log.debug(it)
//        }

    	if (IndexGroup.findByName(params.name)) {
    	//if (! IndexGroup.findAll("from IndexGroup as ig where ig.name=?",params.name)?.isEmpty()) {
    		flash.error = message(code: 'error.create.duplicate.name', args: [message(code:'indexGroup.label'), message(code:'indexGroup.name.label'), params.name?.encodeAsHTML()])
    		render(view: "create", model: [:])
    		return
    	}
		
		def indexGroupInstance = new IndexGroup(params)
		indexGroupInstance.name = params.name
		indexGroupInstance.items = params.items ?: []

		def session = sessionFactory.currentSession
        try {
        	session.persist(indexGroupInstance)
        	session.flush()

			flash.message = "${message(code: 'default.created.message', args: [message(code: 'indexGroup.label', default: 'IndexGroup'), indexGroupInstance.id])}"
			redirect(action: "show", id: indexGroupInstance.id)
		} catch (Exception ex) {
			log.debug ex
		    render(view: "create", model: [indexGroupInstance: indexGroupInstance])
		}
    }

    def show () {
        def indexGroupInstance = IndexGroup.get(params.id)
        if (!indexGroupInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'indexGroup.label', default: 'IndexGroup'), params.id])}"
            redirect(action: "list")
        }
        else {
            [indexGroupInstance: indexGroupInstance]
        }
    }

    def edit () {
        def indexGroupInstance = IndexGroup.get(params.id)
        if (!indexGroupInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'indexGroup.label', default: 'IndexGroup'), params.id])}"
            return redirect(action: "list")
        }
        else {
            return [indexGroupInstance: indexGroupInstance]
        }
    }

    def update () {
        def indexGroupInstance = IndexGroup.get(params.id)
        if (indexGroupInstance) {
            if (params.obj_version) {
                def obj_version = params.obj_version.toLong()
                if (indexGroupInstance.version > obj_version) {
                    
                    indexGroupInstance.errors.rejectValue("obj_version", "default.optimistic.locking.failure", [message(code: 'indexGroup.label', default: 'IndexGroup')] as Object[], "Another user has updated this IndexGroup while you were editing")
                    render(view: "edit", model: [indexGroupInstance: indexGroupInstance])
                    return
                }
            }
            indexGroupInstance.properties = params
            indexGroupInstance.name = params.name

            def session = sessionFactory.currentSession

            try {
	            if (!indexGroupInstance.hasErrors()) {
	            	session.persist(indexGroupInstance)
	            	session.flush()
	            	
	                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'indexGroup.label', default: 'IndexGroup'), indexGroupInstance.id])}"
	                redirect(action: "show", id: indexGroupInstance.id)
	            }
            } catch (Exception ex) {
                log.debug("failed to update IndexGroup:",ex)
            	render(view: "edit", model: [indexGroupInstance: indexGroupInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'indexGroup.label', default: 'IndexGroup'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete () {
        def indexGroupInstance = IndexGroup.get(params.id)
        if (indexGroupInstance) {
            try {
                indexGroupInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'indexGroup.label', default: 'IndexGroup'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'indexGroup.label', default: 'IndexGroup'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'indexGroup.label', default: 'IndexGroup'), params.id])}"
            redirect(action: "list")
        }
    }

    def updateList () {
        setListParams()
        render(template: 'indexGroupList', model:[indexGroupList:IndexGroup.list(params)])
    }

    //--------------------- XML API ------------------------
    @Secured(["isAuthenticated()"])
    def listXml() {
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("indexGroups");
        IndexGroup.list().each { group ->
            group.toXmlElement(root);
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }
    
}
