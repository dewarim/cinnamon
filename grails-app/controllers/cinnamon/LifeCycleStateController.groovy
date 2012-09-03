package cinnamon

import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import cinnamon.lifecycle.LifeCycleState
import cinnamon.lifecycle.LifeCycle
import cinnamon.lifecycle.IState

@Secured(["hasRole('_superusers')"])
class LifeCycleStateController extends BaseController{

    def create () {
        LifeCycleState lcs = new LifeCycleState()
        def copyStates = lifeCycleStateService.fetchCopyStates(null)
        render(template: 'create', model: [lcs: lcs, copyStates: copyStates,
                 stateClasses:ConfigurationHolder.config.lifeCycleStateClasses,
        ])
    }

    def index () {
        setListParams()
        params.sort = params.sort ?: 'id'
        [lcsList: LifeCycleState.list(params)]
    }

    def save () {
        LifeCycleState lcs = new LifeCycleState()
        try {
            updateFields(lcs)
            if (!lcs.lifeCycleStateForCopy) {
                lcs.lifeCycleStateForCopy = lcs
            }
            lcs.save(failOnError: true, flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save lcs: ", e)
            render(status: 503, template: 'create',
                    model: [lcs: lcs,
                            copyStates: lifeCycleStateService.fetchCopyStates(lcs),
                            stateClasses:ConfigurationHolder.config.lifeCycleStateClasses,
                            errorMessage: e.getLocalizedMessage().encodeAsHTML()])
            return
        }
        setListParams()
        render(template: 'list_table', model: [lcsList: LifeCycleState.list(params)])
    }

    def list () {
        redirect(action: 'index')
    }

    def edit () {
        def lcs = LifeCycleState.get(Long.parseLong(params.id))
        render(template: 'edit', model: [lcs: lcs,
                stateClasses:ConfigurationHolder.config.lifeCycleStateClasses,
                copyStates: lifeCycleStateService.fetchCopyStates(lcs)
        ])
    }

    def cancelEdit () {
        render(template: 'row', model: [lcs: LifeCycleState.get(Long.parseLong(params.id))])
    }

    def delete () {
        LifeCycleState lcs = LifeCycleState.get(params.id)
        try {
            if (ObjectSystemData.findByState(lcs)) {
                throw new RuntimeException('error.object.in.use')
            }
            def references = LifeCycleState.findAll(
                    "from LifeCycleState lcs where (lcs != :LCS1) and (lcs.lifeCycleStateForCopy = :LCS2)",
                    [LCS1:lcs, LCS2:lcs])
            if(references.size() > 0){
                throw new RuntimeException('error.object.in.use')
            }
            LifeCycle lifeCycle = lcs.lifeCycle
            if (lifeCycle) {
//                lifeCycle.states.remove(lcs)
                if (lifeCycle.defaultState == lcs) {
                    lcs.lifeCycle.defaultState = null
                }
            }
            lcs.delete(flush: true)
        }
        catch (Exception e) {
            render(status: 503, template: '/shared/showError', model: [infoMessage: message(code: e.getLocalizedMessage())])
            return
        }
        setListParams()
        render(template: 'list_table', model: [lcsList: LifeCycleState.list(params)])
    }

    protected void updateFields(lcs) {
        lcs.name = inputValidationService.checkAndEncodeName(params.name, lcs)
        lcs.config = params.config ?: ''
        lcs.lifeCycle = inputValidationService.checkObject(LifeCycle.class, params.lifeCycle, true)
        lcs.lifeCycleStateForCopy = inputValidationService.checkObject(LifeCycleState.class, params.lifeCycleStateForCopy, true)

        try {
            // class is instantiated not for use but to check if it is can be instantiated at all.
            //noinspection GroovyUnusedAssignment
            IState stateClass = (IState) Class.forName(params.stateClass).newInstance()
            lcs.stateClass = (Class<? extends IState>) Class.forName(params.stateClass)
        }
        catch (ClassCastException e) {
            throw new RuntimeException("error.not.iState.class")
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("error.class.not.found")
        }
        catch (Exception e) {
            throw new RuntimeException("error.not.iState.class")
        }
    }

    def update () {
        LifeCycleState lcs = LifeCycleState.get(params.id)
        try {
            updateFields(lcs)
            lcs.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save lcs: " + e.getLocalizedMessage())
            render(template: 'edit',
                    model: [lcs: lcs,
                            copyStates: lifeCycleStateService.fetchCopyStates(lcs),
                            errorMessage: e.getLocalizedMessage()])
            return
        }
        render(template: 'row', model: [lcs: lcs])
    }

    def updateList () {
        setListParams()
        render(template: 'list_table', model:[lcsList:LifeCycleState.list(params)])
    }

}
