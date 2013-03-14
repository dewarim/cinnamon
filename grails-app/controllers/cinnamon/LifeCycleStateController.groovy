package cinnamon

import cinnamon.exceptions.CinnamonException
import cinnamon.global.PermissionName
import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import cinnamon.lifecycle.LifeCycleState
import cinnamon.lifecycle.LifeCycle
import cinnamon.lifecycle.IState
import org.dom4j.DocumentHelper
import org.dom4j.Element

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

    protected void updateFields(LifeCycleState lcs) {
        lcs.name = inputValidationService.checkAndEncodeName(params.name, lcs)
        lcs.config = params.config ?: ''
        lcs.lifeCycle = inputValidationService.checkObject(LifeCycle.class, params.lifeCycle, true)
        lcs.lifeCycleStateForCopy = inputValidationService.checkObject(LifeCycleState.class, params.lifeCycleStateForCopy, true)

        try {
            // class is instantiated not for use but to check if it is can be instantiated at all.
            //noinspection GroovyUnusedAssignment
            IState stateClass = (IState) Class.forName(params.stateClass, true, Thread.currentThread().contextClassLoader ).newInstance()
            lcs.stateClass = (Class<? extends IState>) Class.forName(params.stateClass, true, Thread.currentThread().contextClassLoader )
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
    
    //----------------------- XML API --------------------------

    /**
     *
     * Fetch a specific lifecycle state by id from the database as XML.
     * @param id id of LifeCycleState object
     * @return an XML document which
     * contains the requested lifecycle state object (or an error message if it could not be found).<br>
     *     Example:
     * <pre>
     *  {@code
     *          <states>
     *            <lifecycleState>
     *              <id>543</id>
     *              <name>TestState</name>
     *              <sysName>example.test.state</sysName>
     *              <stateClass>server.lifecycle.state.NopState</stateClass>
     *              <parameter>&lt;config /&gt;</parameter> (encoded XML string)
     *              <lifeCycle>44</lifeCycle> (may be empty)
     *              <lifeCycleStateForCopy>7</lifeCycleStateForCopy> (may be empty)
     *            </lifecycleState>
     *          </states>
     *  }
     * </pre>
     */
    def getLifeCycleState(Long id) {
        try {
            LifeCycleState lifeCycleState;
            if(id){
                lifeCycleState = LifeCycleState.get(id)
            }
            else{
                throw new CinnamonException("error.param.lcs.id");
            }

            if(lifeCycleState){
                def doc = DocumentHelper.createDocument()
                Element root = doc.addElement('states')
                lifeCycleState.toXmlElement(root)
                render(contentType: 'application/xml', text: doc.asXML())
            }
            else{
                throw new CinnamonException("error.object.not.found");
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to ', e)
        }

    }

    /**
     * Fetch a list of allowed exit states for a given object with
     * an attached lifecycle.
     * <h2>Needed permissions</h2>
     * READ_OBJECT_SYS_METADATA
     * @param id id of the osd whose state may be changed</li>
     * @return XML response containing the allowed exit states.
     */
    def getNextStates(Long id) {        
        try {
            /*
             * get OSD
             * get lifecycle
             * get lifecycle states
             * call checkEnteringObject
             * Note: checkExit is undefined at the moment.
             */
            def user = userService.user
            ObjectSystemData osd = ObjectSystemData.get(id)
            if(osd){
                new Validator(user).validatePermission(osd.acl, PermissionName.READ_OBJECT_SYS_METADATA)
                LifeCycleState state = osd.state
                if(state == null){
                    throw new CinnamonException("error.no_lifecycle_set")
                }

                Collection<LifeCycleState> states = new HashSet<LifeCycleState>();
                states.addAll(osd.getState().getLifeCycle().getStates());
                states.remove(osd.getState());

                def doc = DocumentHelper.createDocument()
                Element root = doc.addElement('lifecycle-states')
                states.each{LifeCycleState lcs ->
                    if(lcs.openForEntry(osd)){
                        lcs.toXmlElement(root);
                    }
                }                        
                render(contentType: 'application/xml', text: doc.asXML())
            }
            else{
                throw new CinnamonException("error.object.not.found");
            }           
            
        }
        catch (Exception e) {
            renderExceptionXml('Failed to ', e)
        }
    }

    /**
     * Change the lifecycle state of an object by moving to another state.
     * This method checks if the move is allowed and executes the lifecycle state
     * class of the old and new state, calling the exit() and enter() methods
     * as appropriate.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_SYS_METADATA
     * @param id the id of the OSD whose state should be changed
     * @param lifecycle_state_id the id of the target lifecycle state<
     * @param state_name = the optional name of a target lifecycle state of the current lifecycle 
     * (may be used instead of the lifecycle_state_id)
     * @return a CinnamonException on failure or 
     * the following XML content:
     *  <pre>
     *  {@code
     *   <success>success.changed_state</success>
     *  }
     *  </pre>
     */
    def changeState(Long id, String state_name, Long lifecycle_state_id) {
        /*
         * get osd
         * validate permission
         * get target state
         * check target state with current state - is this transition allowed?
         * checkEnteringObject
         * call exit() on old state
         * call enter() on new state
         * set new state on OSD
         */
        try {
            ObjectSystemData osd = ObjectSystemData.get(id)
            if(osd){
                new Validator(userService.user).validateSetSysMeta(osd);

                LifeCycleState lifeCycleState;
                if(state_name){
                    lifeCycleState = LifeCycleState.findByNameAndLifeCycle(state_name, osd.state.lifeCycle);
                    if(! lifeCycleState){
                        throw new CinnamonException("error.param.state_name")
                    }
                }
                else{
                    lifeCycleState = LifeCycleState.get(lifecycle_state_id)
                }

                osd.state.exitState(osd, lifeCycleState)
                lifeCycleState.enterState(osd, lifeCycleState) // if this fails, rollback occurs.
                render(contentType: 'application/xml') {
                    success('success.change_lifecycle')
                }

            }
            else{
                throw new CinnamonException("error.object.not.found");
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to ', e)
        }
    }

    /**
     * Removes all lifecycle state information from an object.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_SYS_METADATA
     * @param id the id of the object
     * @return a CinnamonException on failure or a Response object with
     * the following XML content:
     * {@code
     * <pre>
     *  <success>success.detach_lifecycle</success>
     * </pre>
     * }
     */
    def detachLifeCycle(Long id) {
        try {
            ObjectSystemData osd = ObjectSystemData.get(id)
            if(osd){
                new Validator(userService.user).validateSetSysMeta(osd);
                osd.state.exitState(osd, null);
                osd.state = null
            }
            else{
                throw new CinnamonException("error.object.not.found");
            }
            render(contentType: 'application/xml') {
                succees('success.detach_lifecycle')
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to ', e)
        }
    }

    /**
     * Attach a LifeCycle to an object. The lifecycle will start with the
     * default state or the one specified by the client (the client's choice takes precedence).
     * If the default state of this lifecycle is undefined, the client must specify a valid
     * lifecycle state.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_SYS_METADATA
     * @param id the id of the object to which the lifecycle will be attached
     * @param lifecycle_id the id of the lifecycle     
     * @param lifecycle_state_id optional: the state of the lifecycle. If not set, use the defaultState
     * @return a CinnamonException on failure or a Response object with
     * the following XML content:
     * <pre>
     * {@code
     *  <success>success.attach_lifecycle</success>
     * }
     * </pre>
 
     * @param cmd a Map of HTTP request parameters
     */
    def attachLifeCycle(Long id, Long lifecycle_id, Long lifecycle_state_id) {
        try {
            LifeCycle lifeCycle = LifeCycle.get(lifecycle_id)
            ObjectSystemData osd = ObjectSystemData.get(id)
            if(osd == null){
                throw new CinnamonException("error.object.not.found");
            }
            new Validator(userService.user).validateSetSysMeta(osd);
            LifeCycleState lifeCycleState;
            if(lifecycle_state_id){
                lifeCycleState = LifeCycleState.get(lifecycle_state_id)
            }
            else if(lifeCycle.defaultState){
                lifeCycleState = lifeCycle.defaultState;
            }
            else{
                throw new CinnamonException("error.undefined.lifecycle_state");
            }
            lifeCycleState.enterState(osd, lifeCycleState);
            render(contentType: 'application/xml') {
                success('success.attach_lifecycle')
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to ', e)
        }       
    }
}
