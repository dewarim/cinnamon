package cinnamon

import grails.plugins.springsecurity.Secured
import cinnamon.relation.RelationResolver
import cinnamon.relation.RelationType

@Secured(["hasRole('_superusers')"])
class RelationTypeController extends BaseController{

    def create() {}

    def index() {
        redirect(action:'list')
    }
    
    def save() {
        log.debug("relationType::${params}")
        Boolean leftprotected = params.leftobjectprotected?.matches(/true|on/) ?: false
        Boolean rightprotected = params.rightobjectprotected?.matches(/true|on/) ?: false
        Boolean cloneOnLeftCopy = params.cloneOnLeftCopy?.matches(/true|on/) ?: false
        Boolean cloneOnRightCopy = params.cloneOnRightCopy?.matches(/true|on/) ?: false      
        RelationResolver leftResolver = RelationResolver.get(params.left_resolver_id)
        RelationResolver rightResolver = RelationResolver.get(params.right_resolver_id)
        def name = params.name
        def description = params.description
        RelationType relationType = new RelationType(name, description,
                leftprotected, rightprotected,
                leftResolver, rightResolver,
                cloneOnLeftCopy, cloneOnRightCopy
        )
    	try{
    		relationType.save(failOnError:true)
    	}
    	catch(Exception e){
            log.debug("failed to save relationType: ",e)
    		flash.message = e.getLocalizedMessage().encodeAsHTML()
    		return redirect(action:'create', controller:'relationType', 
                    model:[relationType:relationType]) //, params:[relationType:relationType])
    	}

    	return redirect(action : 'show', params : [id : relationType?.id])
    }

    def list() {
    		setListParams()
    		[relationTypeList : RelationType.list(params)]
    }

    def show() {
        if(params.id){
            RelationType rt = RelationType.get(params.id)
            if(rt != null){
    		    return [relationType : rt]
            }
        }
        flash.message = message(code:'error.access.failed')
        return redirect(action:'list', controller:'relationType')
    }

    def edit(){
    		[relationType : RelationType.get(params.id)]
    }

    def delete() {
        RelationType rt = RelationType.get(params.id)
		try{
			rt.delete()
		}
		catch(Exception e){
			flash.message = e.getLocalizedMessage()
			return redirect(action:'list')
		}

		flash.message = message(code: 'relationtype.delete.success', args:[rt.name.encodeAsHTML()])
		return redirect(action:'list')
    }

    def update() {
        RelationType rt = RelationType.get(params.id)
        try{
            if(! rt){
                throw new RuntimeException('error.object.not.found')
            }
            if (params.leftobjectprotected){
                rt.leftobjectprotected = params.leftobjectprotected?.matches(/true|on/) ?: false
            }
            if (params.rightobjectprotected){
                rt.rightobjectprotected = params.rightobjectprotected?.matches(/true|on/) ?: false
            }
            if (params.cloneOnLeftCopy){
                rt.cloneOnLeftCopy = params.cloneOnLeftCopy?.matches(/true|on/) ?: false
            }
            if (params.cloneOnRightCopy){
                rt.cloneOnRightCopy = params.cloneOnRightCopy?.matches(/true|on/) ?: false
            }
            rt.leftResolver = RelationResolver.get(params.left_resolver_id);
            rt.rightResolver = RelationResolver.get(params.right_resolver_id);
            rt.save(flush:true)
        }
        catch (Exception e){
           	flash.message = e.getLocalizedMessage()
			return redirect(action:'edit', params:[id:params.id])
        }
        return redirect(action:'show', params:[id:params.id])
    }

    def updateList() {
        setListParams()
        render(template: 'relationTypeList', model:[relationTypeList:RelationType.list(params)])
    }

}
