package cinnamon.workflow

import cinnamon.ObjectSystemData
import cinnamon.exceptions.CinnamonException
import cinnamon.global.Constants
import cinnamon.index.LuceneResult
import cinnamon.index.SearchableDomain
import cinnamon.relation.Relation
import groovyx.gpars.actor.DynamicDispatchActor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This actor executes the unfinished transitions and checks workflows and
 * transitions for deadlines.
 */
class TransitionActor extends DynamicDispatchActor{

    Logger log = LoggerFactory.getLogger(this.class)   

    void onMessage(WorkflowCommand command){
        try{
            reply executeTransitions(command, new WorkflowResult())
        }
        catch (Exception e){
            log.warn("TransitionActor failed.",e)
        }
    }
    
    WorkflowResult executeTransitions(WorkflowCommand cmd, WorkflowResult result) {
        try {             
            
            ObjectSystemData.withTransaction {
                def mainContext = new ObjectSystemData().domainClass.grailsApplication.mainContext
                def luceneService = mainContext.getBean('luceneService')
                def itemService = mainContext.getBean('itemService')
                def workflowService = mainContext.getBean('workflowService')
                
                // find tasks with procstate transition_ready.
                List<ObjectSystemData> transitionList = 
                    ObjectSystemData.findAllByTypeAndProcstate(cmd.taskObjectType, Constants.PROCSTATE_TRANSITION_READY);
                log.debug("TransitionReady tasks found: "+transitionList.size());
                tryAndExecuteTransitions(transitionList, null, workflowService);

                // find all tasks with passed deadline:
                // build query

                LuceneResult results = luceneService.searchXml(cmd.queryTaskDeadline, cmd.repositoryName, SearchableDomain.OSD);
                Collection<ObjectSystemData> deadlinedTasks = results.filterResults(itemService)
                log.debug("tasks with deadline: "+deadlinedTasks.size());
                tryAndExecuteTransitions(deadlinedTasks,
                        "/meta/metaset[@type='transition']/transition[name='deadline_transition']", workflowService);

                // find all workflows with passed deadline:
                checkWorkflowDeadlines(cmd, luceneService, itemService, workflowService)
            }
            Thread.sleep(2000)
        }
        catch (Exception e) {
            result.failed = true
            result.messages.add(e.message)
            log.warn("executeTransitions failed:", e)
        }
        result
    }

    void tryAndExecuteTransitions(Collection<ObjectSystemData> tasks, String transitionXpath, workflowService){
        for(ObjectSystemData task : tasks){
            try{
                workflowService.executeTransition(task, transitionXpath)
            }
            catch (Exception e) {
                log.error("Failed to execute Transition",e);
            }
        }
    }

    void checkWorkflowDeadlines(WorkflowCommand cmd, luceneService, itemService, workflowService){
        /* 
         * search for workflows that have reached their deadline
         * get workflow-template
         * get deadline-taskdef for each
         * create deadline-task for workflow
         * set deadline-task to transition_ready if it's an automatic task
         * 	(last is done by wfApi.createTask)
         * The new deadline-Task should transition automatically to its default transition.
         */
        LuceneResult result = luceneService.searchXml(cmd.queryWorkflowDeadline, cmd.repositoryName, SearchableDomain.OSD);
        Collection<ObjectSystemData> deadlinedWorkflows = result.filterResults(itemService)
        for(ObjectSystemData workflow : deadlinedWorkflows){
            // possible optimization-1: preload the templates.
            // possible optimization-2: put the id of the deadline-taskdef into the workflow instance.
            List<ObjectSystemData> templates = ObjectSystemData.findAllByNameAndType(workflow.getName(), cmd.workflowTemplateType);
            if(templates.size() != 1){
                String message = String.format("Found %d deadline templates - expected: one (with name '%s')!",
                        templates.size(), workflow.getName());
                throw new CinnamonException(message);
            }
            Relation relation = Relation.findByLeftOSDAndType(templates.get(0), cmd.deadlineRelationType);
            if(! relation){
                throw new CinnamonException("Could not find workflow deadline relation.");
            }
            ObjectSystemData deadlineTaskDef = relation.rightOSD;
            workflowService.createTask(deadlineTaskDef, workflow);
        }

    }

}
