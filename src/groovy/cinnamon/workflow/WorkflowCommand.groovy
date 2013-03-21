package cinnamon.workflow

import cinnamon.LuceneService
import cinnamon.ObjectType
import cinnamon.global.Constants
import cinnamon.relation.RelationType

/**
 * 
 */
class WorkflowCommand {
    
    WorkflowCommandType type
    String repositoryName
    ObjectType taskObjectType
    ObjectType workflowObjectType
    ObjectType workflowTemplateType
    RelationType deadlineRelationType
    String queryTaskDeadline
    String queryWorkflowDeadline
    
    WorkflowCommand() {
    }
    
    WorkflowCommand(WorkflowCommand old){
        type = old.type
        repositoryName = old.repositoryName
        taskObjectType = old.taskObjectType
        workflowObjectType = old.taskObjectType
        workflowTemplateType = old.workflowObjectType
        deadlineRelationType = old.deadlineRelationType
        initializeQueryStrings()
    }

    /**
     * Build a query for 'workflow_deadline' index depending on objecttype and procstate.
     * For example, a deadline on a task object is irrelevant, if the procstate is "done".
     * @param ot the objecttype of the items to find
     * @param procstate the forbidden procstate
     * @return an XML query string for use with LuceneBridge.search()
     */
    static String buildDeadlineQuery(ObjectType ot, String procstate){
        String deadline = LuceneService.pad(new Date().getTime());
        return String.format(deadlineQueryFormatString, deadline, LuceneService.pad(ot.getId()), procstate);
    }

    static final String deadlineQueryFormatString = "<FilteredQuery>" +
            "<Filter>" +
            "<RangeFilter fieldName='workflow_deadline' lowerTerm='00000000000000000000' upperTerm='%s'></RangeFilter>" +
            "</Filter>" +
            "<Query><BooleanQuery>" +
            "<Clause occurs='must'><TermQuery fieldName='objecttype'>%s</TermQuery></Clause>" +
            "<Clause occurs='must'><TermQuery fieldName='procstate'>%s</TermQuery></Clause>" +
            "</BooleanQuery></Query>" +
            "</FilteredQuery>";

    void initializeQueryStrings(){
        queryTaskDeadline = buildDeadlineQuery(taskObjectType, Constants.PROCSTATE_TASK_TODO)
        queryWorkflowDeadline = buildDeadlineQuery(workflowObjectType, Constants.PROCSTATE_WORKFLOW_STARTED)
    }
}
