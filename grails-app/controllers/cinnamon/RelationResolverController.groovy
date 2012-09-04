package cinnamon

import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import cinnamon.relation.RelationResolver
import cinnamon.interfaces.IRelationResolver
import cinnamon.relation.RelationType

@Secured(["hasRole('_superusers')"])
class RelationResolverController extends BaseController {

    def create() {
        render(template: 'create', model: [relationResolver: new RelationResolver(),
                resolvers: ConfigurationHolder.config.relationResolvers
        ])
    }

    def index() {
        setListParams()
        [relationResolverList: RelationResolver.list(params)]
    }

    def save() {
        RelationResolver relationResolver = new RelationResolver()
        try {
            updateFields(relationResolver)
            relationResolver.save(failOnError: true, flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save relationResolver: ", e)
            render(status: 503, template: 'create',
                    model: [relationResolver: relationResolver,
                            resolvers: ConfigurationHolder.config.relationResolvers,
                            errorMessage: e.getLocalizedMessage().encodeAsHTML()])
            return
        }
        setListParams()
        render(template: 'list_table', model: [relationResolverList: RelationResolver.list(params)])
    }

    def list() {
        redirect(action: 'index')
    }

    def edit() {
        def resolver = inputValidationService.checkObject(RelationResolver.class, params.id)
        render(template: 'edit', model: [relationResolver: resolver,
                resolvers: ConfigurationHolder.config.relationResolvers
        ])
    }

    def cancelEdit() {
        render(template: 'row', model: [relationResolver: RelationResolver.get(Long.parseLong(params.id))])
    }

    def delete() {
        RelationResolver relationResolver = RelationResolver.get(params.id)
        try {
            if (RelationType.findByLeftResolverOrRightResolver(relationResolver, relationResolver)) {
                throw new RuntimeException('error.object.in.use')
            }
            relationResolver.delete(flush: true)
        }
        catch (Exception e) {
            render(status: 503, template: '/shared/showError', model: [infoMessage: message(code: e.getLocalizedMessage())])
            return
        }
        setListParams()
        render(template: 'list_table', model: [relationResolverList: RelationResolver.list(params)])
    }

    protected void updateFields(relationResolver) {
        relationResolver.name = inputValidationService.checkAndEncodeName(params.name, relationResolver)
        relationResolver.config = params.config
        try {
            // class is instantiated not for use but to check if it is can be instantiated at all.
            //noinspection GroovyUnusedAssignment
            IRelationResolver testResolver = 
                (IRelationResolver) Class.forName(params.resolverClass, true, Thread.currentThread().contextClassLoader ).newInstance()
            relationResolver.resolverClass = 
                (Class<? extends IRelationResolver>) Class.forName(params.resolverClass, true, Thread.currentThread().contextClassLoader )
        }
        catch (ClassCastException e) {
            throw new RuntimeException("error.not.iRelationResolver.class")
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("error.class.not.found")
        }
        catch (Exception e) {
            throw new RuntimeException("error.not.iRelationResolver.class")
        }
    }

    def update() {
        RelationResolver relationResolver = RelationResolver.get(params.id)
        try {
            updateFields(relationResolver)
            relationResolver.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save relationResolver: " + e.getLocalizedMessage())
            render(template: 'edit',
                    model: [relationResolver: relationResolver,
                            errorMessage: e.getLocalizedMessage()])
            return
        }
        render(template: 'row', model: [relationResolver: relationResolver])
    }

    def updateList() {
        setListParams()
        render(template: 'list_table', model: [relationResolverList: RelationResolver.list(params)])
    }
}
