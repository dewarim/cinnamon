package cinnamon.workflow

import cinnamon.Folder
import cinnamon.Metaset
import cinnamon.ObjectSystemData
import cinnamon.ObjectType
import cinnamon.UserAccount
import cinnamon.Validator
import cinnamon.exceptions.CinnamonConfigurationException
import cinnamon.exceptions.CinnamonException
import cinnamon.global.Constants
import cinnamon.global.PermissionName
import cinnamon.interfaces.Transition
import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import cinnamon.utils.ParamParser
import humulus.Environment
import humulus.EnvironmentHolder
import org.dom4j.Document
import org.dom4j.Element

class WorkflowService {

    def userService
    def folderService
    def luceneService
    def osdService
    def relationService

    Long createWorkflow(Long templateId) {

        /*
         * x fetch the template
         * x fetch the workflow folder
         * x fetch the task definition of the start_task (via relation)
         * x fetch the task folder
         * x fetch object types
         * x copy template to workflow folder (= new workflow object)
         * x copy start_task definiton to task folder (= new task object)
         * x set procstate of start_task to todo
         * x create relation between workflow and task
         * x index new workflow
         * x index new task
         * x return workflow id
         */

        def user = userService.user
        // fetch the template
        ObjectSystemData workflowTemplate = ObjectSystemData.get(templateId)
        if (workflowTemplate == null ||
                !workflowTemplate.getType().getName().equals(Constants.OBJTYPE_WORKFLOW_TEMPLATE)) {
            throw new CinnamonConfigurationException("error.template.not_found")
        }
        new Validator(user).validatePermissions(workflowTemplate, [PermissionName.CREATE_INSTANCE])

        // fetch template folder
        Folder workflowFolder = folderService.findByPath(Constants.WORKFLOW_FOLDER_PATH)

        log.debug("Creating new Workflow from template")

        // copy workflow template to workflow:
        ObjectType workflowType = ObjectType.findByName(Constants.OBJTYPE_WORKFLOW)
        ObjectSystemData workflow = workflowTemplate.createClone()
        workflow.setAcl(workflowFolder.acl) // set ACL to target folder's ACL
        workflow.setParent(workflowFolder);
        workflow.setPredecessor(null);
        workflow.setOwner(user);
        workflow.setType(workflowType);
        workflow.setProcstate(Constants.PROCSTATE_WORKFLOW_STARTED);
        workflow.save()

        log.debug("Creating new Start Task from task_definition");
        RelationType startTaskRelType = RelationType.findByName(Constants.RELATION_TYPE_WORKFLOW_TO_START_TASK);


        def relation = Relation.findByLeftOSDAndType(workflowTemplate, startTaskRelType);
        if (!relation) {
            throw new CinnamonException("error.missing.start_task_relation");
        }
        ObjectSystemData startTaskDef = relation.rightOSD
        ObjectSystemData startTask = createTask(startTaskDef, workflow);

        log.debug("Indexing new workflow and start task");

        // index new workflow and start task:
        luceneService.addToIndex(workflow);
        luceneService.addToIndex(startTask);
        return workflow.id
    }

    ObjectSystemData createTask(ObjectSystemData taskDef, ObjectSystemData workflow) {
        // possible optimization: add the fooDAOs as fields to the class.
        Folder taskFolder = folderService.findByPath(Constants.WORKFLOW_TASK_PATH);
        ObjectType taskType = ObjectType.findByName(Constants.OBJTYPE_TASK);

        ObjectSystemData task = taskDef.createClone();
        task.setAcl(taskFolder.getAcl());
        task.setParent(taskFolder);
        task.setPredecessor(null);
        task.setOwner(userService.user)
        task.setType(taskType);


        Document metaDoc = ParamParser.parseXmlToDocument(task.getMetadata(), null);
        org.dom4j.Node manualNode = metaDoc.selectSingleNode("/meta/metaset[@type='task_definition']/manual[text()='true']");
        if (manualNode == null) {
            /*
             * Tasks which do not require human intervention are set to transition_ready.
             * Then, the WorkflowServer will promptly execute the default transition
             * of this task.
             */
            task.setProcstate(Constants.PROCSTATE_TRANSITION_READY);
        }
        else {
            task.setProcstate(Constants.PROCSTATE_TASK_TODO);
        }

        task.save()
        // copy content of startTask
        osdService.copyContent(taskDef, task);

        // create relation between workflow and start task
        /*
         * Note: this is a normal relation, not a start_task_relation.
         */
        RelationType taskRelType = RelationType.findByName(Constants.RELATION_TYPE_WORKFLOW_TO_TASK);
        relationService.findOrCreateRelation(taskRelType, workflow, task, "");

        return task;
    }

    /**
     * Execute the transition which can be found at the end of the given transitionXpath.
     * @param task the task which holds the transition configuration in its metadata
     * @param transitionXpath the optional transitionXpath param defines the xpath statement which returns the
     *                        transition that should be selected. If null, executeTransition uses the default
     *                        transition.
     */
    public void executeTransition(ObjectSystemData task, String transitionXpath) {
        if (transitionXpath == null) {
            transitionXpath = "/meta/metaset[@type='transition']/transition[name=/meta/metaset[@type='transition']/default]";
        }
        String meta = task.getMetadata();
        log.debug("transitionXPath: " + transitionXpath);
        log.debug("taskId: " + task.getId());
        log.debug("taskName:" + task.getName());
        log.debug("meta: " + task.getMetadata());
        org.dom4j.Node transitionNode = ParamParser.parseXmlToDocument(meta, null).selectSingleNode(transitionXpath);
        if (transitionNode == null) {
            throw new RuntimeException("Could not find transition with xpath: " + transitionXpath);
        }
        org.dom4j.Node transitionClassNode = transitionNode.selectSingleNode("class");
        if (transitionClassNode == null) {
            throw new RuntimeException("Could not find transitionClassNode.");
        }
        String transitionClass = transitionClassNode.getText();
        log.debug("transitionClass: " + transitionClass);

        List<ObjectSystemData> newTasks;
        try {
            Transition transition = (Transition) Class.forName(transitionClass).newInstance();
            newTasks = transition.execute(task, transitionNode, EnvironmentHolder.environment.dbName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new CinnamonConfigurationException(e);
        }

        // create relation between workflow and new tasks
        RelationType taskRelType = RelationType.findByName(Constants.RELATION_TYPE_WORKFLOW_TO_TASK);
        ObjectSystemData workflow = Relation.findByRightOSDAndType(task, taskRelType).leftOSD;
        /*
         *  workflowRelations should only ever have one valid relation as a task
         *  must have a relation to one and only one workflow.
         */
        for (ObjectSystemData aTask : newTasks) {
            relationService.findOrCreateRelation(taskRelType, workflow, aTask, "");
        }

        if (newTasks.isEmpty()) {
            /*
             *  if there are no new tasks, check if finished.
             *  (a workflow is finished if there are no open tasks left)
             */
            ObjectType ot = ObjectType.findByName(Constants.OBJTYPE_TASK);
            List<ObjectSystemData> remainingTasks = findOpenTasksByWorkflow(ot, Constants.PROCSTATE_TASK_TODO, workflow);
            log.debug("remaining tasks: " + remainingTasks.size());
            if (remainingTasks.isEmpty()) {
                log.debug(" => workflow is finished");
                workflow.setProcstate(Constants.PROCSTATE_WORKFLOW_FINISHED);
            }
        }

        // index new tasks with Lucene:
        log.debug("updating Lucene index");
        luceneService.updateIndex(task);
        for (ObjectSystemData aTask : newTasks) {
            luceneService.addToIndex(aTask);
        }
        if (workflow.getProcstate().equals(Constants.PROCSTATE_WORKFLOW_FINISHED)) {
            luceneService.updateIndex(workflow);
        }
    }

    List<ObjectSystemData> findOpenTasksByUserAndWorkflow(
            ObjectType objectType, String processingState, UserAccount owner, ObjectSystemData workflow) {
        final String query = "select o from ObjectSystemData o where o.type=:type and o.procstate=:procstate and o.owner=:owner " +
                "and o.id in (select r1.rightOSD.id from Relation r1 where r1.leftOSD=:leftOSD)"
        return ObjectSystemData.findAll(query, [procstate: processingState, type: objectType, owner: owner, leftOSD: workflow])
    }

    List<ObjectSystemData> findOpenTasksByWorkflow(
            ObjectType objectType, String processingState, ObjectSystemData workflow) {
        final String query = "select o from ObjectSystemData o where o.type=:type and o.procstate=:procstate " +
                "and o.id in (select r1.rightOSD.id from Relation r1 where r1.leftOSD=:leftOSD)"
        return ObjectSystemData.findAll(query, [procstate: processingState, type: objectType, leftOSD: workflow])
    }

    def workflowMasters = [:]

    void initializeWorkflowMasters() {
        def currentEnv = EnvironmentHolder.environment
        Environment.list().each { repo ->
            def name = repo.dbName
            log.debug("create workflow master object for ${name}")
            try {
                EnvironmentHolder.environment = repo
                def master = new WorkflowMaster()
                master.start()
                master.sendAndContinue(new WorkflowCommand(type: WorkflowCommandType.RUN_WORKFLOW,
                        repositoryName: name)) { WorkflowResult workflowResult ->
                    log.debug("Received workflowResult: ${workflowResult}, status: ${ workflowResult.failed ? 'failed' : 'ok'}")
                }
                workflowMasters.put(name, master)
            } catch (Exception e) {
                log.debug("failed to initialize workflow master actor for repository $name", e)
                throw new RuntimeException("Failed to initialize workflow master actor for repository $name.", e);
            }
        }
        EnvironmentHolder.environment = currentEnv
    }

    void stopWorkflowMasters() {
        workflowMasters.each { String name, WorkflowMaster master ->
            WorkflowResult result = master.sendAndWait(new WorkflowCommand(type: WorkflowCommandType.STOP_RUNNING, repositoryName: name))
            if (result.failed) {
                log.warn("Looks like I could not stop the workflowMaster for ${name} properly.\n" +
                        "Messages: ${result.messages}")
            }
            else {
                log.debug("WorkflowMaster for ${name} stopped.")
            }
        }
    }


    void publishStacktrace(ObjectSystemData task, Exception e, String metasetName, String procstate){
        StringBuilder trace = new StringBuilder()
        Throwable cause = e
        while (cause != null) {
            trace.append(cause.toString())
            for (StackTraceElement ste : cause.getStackTrace()) {
                trace.append("\n  ")
                trace.append(ste)
            }
            trace.append('\n')
            cause = cause.getCause()
        }
        Document errorDoc = ParamParser.parseXmlToDocument("<error timestamp='${new Date().toString()}'/>");
        errorDoc.getRootElement().addText(trace.toString());
        Metaset metaset = task.fetchMetaset(metasetName, true);
        String meta = metaset.getContent();
        Document metaDoc = ParamParser.parseXmlToDocument(meta);
        Element metasetElement = metaDoc.getRootElement();
        metasetElement.add(errorDoc.getRootElement().detach());
        metaset.setContent(metaDoc.asXML());
        task.setProcstate(procstate);
    }
}
