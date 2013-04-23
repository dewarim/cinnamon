package cinnamon.workflow

import cinnamon.Acl
import cinnamon.BaseController
import cinnamon.ObjectSystemData
import cinnamon.ObjectType
import cinnamon.UserAccount
import cinnamon.Validator
import cinnamon.exceptions.CinnamonException
import cinnamon.global.Constants
import cinnamon.global.PermissionName
import grails.plugins.springsecurity.Secured
import org.dom4j.Document
import org.dom4j.Element

@Secured(["isAuthenticated()"])
class WorkflowController extends BaseController {

    def workflowService

    /**
     * Create a new Workflow. User must have CREATE_INSTANCE-Permission.
     * @param template_id id of workflow-template
     * @return
     * {@code
     * 	<workflow>$id of workflow OSD</workflow>
     *}
     */
    def createWorkflow(Long template_id) {
        try {
            def id = workflowService.createWorkflow(template_id)
            render(contentType: 'application/xml') {
                workflowId(id)
            }
        }
        catch (Exception e) {
            renderExceptionXml("Failed to execute $actionName.", e)
        }
    }

    /**
     * Find open tasks of one or all workflows and for one or all users.
     * @param workflow_id optional workflow id to filter tasks
     * @param user_id optional user id to filter tasks
     * @return
     * {@code
     *  <!-- identical to getObjects -->
     * 	<objects>
     * 	 <object>(serialized object)</object>
     *  </objects>
     *}
     */
    def findOpenTasks(Long workflow_id, Long user_id) {
        try {
            ObjectSystemData workflow;
            ObjectType taskObjectType = ObjectType.findByName(Constants.OBJTYPE_TASK);
            UserAccount owner;
            List<ObjectSystemData> tasks;

            /*
             * 4 cases:
             * 1. workflow defined, user defined = return this user's tasks in this workflow.
             * 2. workflow defined, no user = return all tasks of this workflow.
             * 3. no workflow given, but user define = return this user's tasks in all workflows.
             * 4. no workflow given, no user defined: return all open tasks in all workflows.
             */
            if (workflow_id) {
                workflow = ObjectSystemData.get(workflow_id)
                if (workflow == null) {
                    throw new CinnamonException("error.workflow.not_found");
                }
                log.debug("found workflow");
                if (user_id) {
                    // case 1
                    owner = UserAccount.get(user_id);
                    if (owner == null) {
                        throw new CinnamonException("error.user.not_found");
                    }
                    tasks = workflowService.findOpenTasksByUserAndWorkflow(taskObjectType,
                            Constants.PROCSTATE_TASK_TODO, owner, workflow);
                }
                else {
                    // case 2
                    tasks = workflowService.findOpenTasksByWorkflow(taskObjectType,
                            Constants.PROCSTATE_TASK_TODO, workflow);
                }
            }
            else if (user_id) {
                // case 3
                owner = UserAccount.get(user_id)
                if (owner == null) {
                    throw new CinnamonException("error.user.not_found");
                }
                tasks = ObjectSystemData.findAllByTypeAndProcstateAndOwner(taskObjectType,
                        Constants.PROCSTATE_TASK_TODO, owner);
            }
            else {
                log.debug("return all open tasks");
                // case 4
                tasks = ObjectSystemData.findAllByTypeAndProcstate(taskObjectType, Constants.PROCSTATE_TASK_TODO);
            }

            Document doc = osdService.generateQueryObjectResultDocument(tasks);
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            renderExceptionXml("Failed to execute $actionName.", e)
        }

    }

    /**
     * Execute a transition from one task to another (or the end point of a workflow).
     * The required parameters must be set beforehand in the task.
     * @return
     * {@code
     * 	<success>transition.successful</success>
     * }
     * @param id id of task object
     * @param transition_name = name of the selected transition
     *
     */
    def doTransition(Long id, String transition_name) {
        try {
            ObjectSystemData task = ObjectSystemData.get(id)
            if (task == null) {
                throw new CinnamonException("error.object.not.found")
            }
            Validator val = new Validator(userService.user)
            val.validatePermissions(task, [PermissionName.READ_OBJECT_CUSTOM_METADATA, PermissionName.WRITE_OBJECT_CUSTOM_METADATA, PermissionName.WRITE_OBJECT_SYS_METADATA]);

            String transitionXpath = String.format("/meta/metaset[@type='transition']/transition[name='%s']", transition_name)
            workflowService.executeTransition(task, transitionXpath)

            render(contentType: 'application/xml') {
                success('transition.successful')
            }

        }
        catch (Exception e) {
            renderExceptionXml("Failed to execute $actionName.", e)
        }
    }


}
