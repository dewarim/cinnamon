package cinnamon

import grails.plugin.springsecurity.annotation.Secured
import cinnamon.index.IndexItem
import cinnamon.index.IndexType
import cinnamon.index.IndexGroup
import org.dom4j.DocumentHelper
import org.dom4j.Element

@Secured(["hasRole('_superusers')"])
class IndexItemController extends BaseController{

	def sessionFactory // inject Hibernate session 

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index () {
        redirect(action: "list", params: params)
    }

    def list () {
        setListParams()
        [indexItemList: IndexItem.list(params)]
    }

    def create () {
//        def indexItemInstance = new IndexItem()
//        indexItemInstance.properties = params
//        return [indexItemInstance: indexItemInstance]
    }

    def save () {
    	if (IndexItem.findByName(params.name)) {
    		flash.error = message(code: 'error.create.duplicate.name', args: [message(code:'indexItem.label'), message(code:'indexItem.name.label'), params.name?.encodeAsHTML()])
    		render(view: "create", model: [:])
    		return
    	}
		def indexItemInstance = new IndexItem()
        // TODO: use inputValidationService
        // TODO: catch Exceptions from bad parameters.
		indexItemInstance.name = params.name;
		indexItemInstance.searchString = params.searchString;
		indexItemInstance.searchCondition = params.searchCondition;
		indexItemInstance.fieldname = params.fieldname;
		indexItemInstance.indexType = IndexType.get(params.indexType.id);
		indexItemInstance.systemic = params.systemic == "on";
		indexItemInstance.indexGroup = IndexGroup.get(params.indexGroup.id);
		indexItemInstance.forContent = params.forContent == "on";
		indexItemInstance.forMetadata = params.forMetadata == "on";
		indexItemInstance.forSysMeta = params.forSysMeta == "on";
		indexItemInstance.multipleResults = params.multipleResults == "on";
		indexItemInstance.vaProviderParams = params.vaProviderParams;

        def session = sessionFactory.currentSession
        try {
        	session.persist(indexItemInstance)
        	session.flush()
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'indexItem.label', default: 'IndexItem'), indexItemInstance.id])}"
            redirect(action: "show", id: indexItemInstance.id)
        } catch (Exception ex) {
        	log.debug ex
            render(view: "create", model: [indexItemInstance: indexItemInstance])
        }
   	}

    def show () {
        def indexItemInstance = IndexItem.get(params.id)
        if (!indexItemInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'indexItem.label', default: 'IndexItem'), params.id])}"
            redirect(action: "list")
        }
        else {
            [indexItemInstance: indexItemInstance]
        }
    }

    def edit () {
        def indexItemInstance = IndexItem.get(params.id)
        if (!indexItemInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'indexItem.label', default: 'IndexItem'), params.id])}"
            return redirect(action: "list")
        }
        else {
            return [indexItemInstance: indexItemInstance]
        }
    }

    def update () {
        def indexItemInstance = IndexItem.get(params.id)
        if (indexItemInstance) {
            if (params.obj_version) {
                def obj_version = params.obj_version.toLong()
                if (indexItemInstance.version > obj_version) {
                    
                    indexItemInstance.errors.rejectValue("obj_version", "default.optimistic.locking.failure", [message(code: 'indexItem.label', default: 'IndexItem')] as Object[], "Another user has updated this IndexItem while you were editing")
                    render(view: "edit", model: [indexItemInstance: indexItemInstance])
                    return
                }
            }
            // indexItemInstance.properties = params
            // TODO: use inputValidationService
            // TODO: catch Exceptions from bad parameters.
            indexItemInstance.name = params.name;
    		indexItemInstance.searchString = params.searchString;
    		indexItemInstance.searchCondition = params.searchCondition;
    		indexItemInstance.fieldname = params.fieldname;
    		indexItemInstance.indexType = IndexType.get(params.indexType.id);
    		indexItemInstance.systemic = params.systemic == "on";
    		indexItemInstance.indexGroup = IndexGroup.get(params.indexGroup.id);
    		indexItemInstance.forContent = params.forContent == "on";
    		indexItemInstance.forMetadata = params.forMetadata == "on";
    		indexItemInstance.forSysMeta = params.forSysMeta == "on";
    		indexItemInstance.multipleResults = params.multipleResults == "on";
    		indexItemInstance.vaProviderParams = params.vaProviderParams;

            def session = sessionFactory.currentSession
            try {
	            if (!indexItemInstance.hasErrors()) {
	        		session.persist(indexItemInstance)
	        		session.flush()
	        		
	                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'indexItem.label', default: 'IndexItem'), indexItemInstance.id])}"
	                redirect(action: "show", id: indexItemInstance.id)
	            }
            } catch(Exception ex) {
            	log.debug ex
            	flash.error = ex.getMessage()
                render(view: "edit", model: [indexItemInstance: indexItemInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'indexItem.label', default: 'IndexItem'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete () {
        def indexItemInstance = IndexItem.get(params.id)
        if (indexItemInstance) {
            try {
                indexItemInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'indexItem.label', default: 'IndexItem'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'indexItem.label', default: 'IndexItem'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'indexItem.label', default: 'IndexItem'), params.id])}"
            redirect(action: "list")
        }
    }

    def updateList () {
        setListParams()
        render(template: 'indexItemList', model:[indexItemList:IndexItem.list(params)])
    }
    
    //--------------------- XML API ------------------------
    @Secured(["isAuthenticated()"])
    def listXml() {
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("indexItems");
        IndexItem.list().each { item ->
            item.toXmlElement(root);
        }
        render(contentType: 'application/xml', text: doc.asXML())
    }

}
