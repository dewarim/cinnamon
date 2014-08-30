package cinnamon

import grails.plugin.springsecurity.annotation.Secured
import cinnamon.transformation.Transformer
import cinnamon.transformer.ITransformer

@Secured(["hasRole('_superusers')"])
class TransformerController extends BaseController{

    def create () {
        Transformer transformer = new Transformer()
        render(template: 'create', model: [transformer: transformer,
                transformers:grailsApplication.config.transformers
        ])
    }

    def index () {
        setListParams()
        [transformerList: Transformer.list(params)]
    }

    def save () {
        Transformer transformer = new Transformer()
        try {
            updateFields(transformer)
            transformer.save(failOnError: true, flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save transformer: ", e)
            render(status: 503, template: 'create',
                    model: [transformer: transformer,
                            transformers:grailsApplication.config.transformers,
                            errorMessage: e.getLocalizedMessage().encodeAsHTML()])
            return
        }
        setListParams()
        render(template: 'list_table', model: [transformerList: Transformer.list(params)])
    }

    def list () {
        redirect(action: 'index')
    }

    def edit () {
        try {
            def transformer = Transformer.get(Long.parseLong(params.id))
            if (transformer == null) {
                throw new RuntimeException("error.object.not.found")
            }
            render(template: 'edit', model: [transformer: transformer,
                transformers:grailsApplication.config.transformers
            ])
        }
        catch (Exception e) {
            log.debug("failed to render edit-transformer", e)
            render(status: 503, template: '/shared/showError', model: [infoMessage: message(code: e.getLocalizedMessage())])
        }
    }

    def cancelEdit () {
        render(template: 'row', model: [transformer: Transformer.get(Long.parseLong(params.id))])
    }

    def delete () {
        Transformer transformer = Transformer.get(params.id)
        try {
            transformer.delete(flush: true)
        }
        catch (Exception e) {
            render(status: 503, template: '/shared/showError', model: [infoMessage: message(code: e.getLocalizedMessage())])
            return
        }
        setListParams()
        render(template: 'list_table', model: [transformerList: Transformer.list(params)])
    }

    protected void updateFields(Transformer transformer) {
        transformer.name = inputValidationService.checkAndEncodeName(params.name, transformer)
        transformer.sourceFormat = inputValidationService.checkObject(Format.class, params.sourceFormat)
        transformer.targetFormat = inputValidationService.checkObject(Format.class, params.targetFormat)
        try {
            // class is instantiated not for use but to check if it is can be instantiated at all.
            //noinspection GroovyUnusedAssignment
            ITransformer transformerClass = 
                (ITransformer) Class.forName(params.transformerClass, true, Thread.currentThread().contextClassLoader).newInstance()
            transformer.transformerClass = 
                (Class<? extends ITransformer>) Class.forName(params.transformerClass, true, Thread.currentThread().contextClassLoader)
        }
        catch (ClassCastException e) {
            throw new RuntimeException("error.not.iTransformer.class")
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("error.class.not.found")
        }
        catch (Exception e) {
            throw new RuntimeException("error.not.iTransformer.class")
        }
    }

    def update () {
        Transformer transformer = Transformer.get(params.id)
        try {
            updateFields(transformer)
            transformer.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save transformer: " + e.getLocalizedMessage())
            render(template: 'edit',
                    model: [transformer: transformer,
                            errorMessage: e.getLocalizedMessage()])
            return
        }
        render(template: 'row', model: [transformer: transformer])
    }

    def updateList () {
        setListParams()
        render(template: 'list_table', model:[transformerList:Transformer.list(params)])
    }
}
