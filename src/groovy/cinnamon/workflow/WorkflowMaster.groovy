package cinnamon.workflow

import cinnamon.ObjectType
import cinnamon.global.Constants
import cinnamon.relation.RelationType
import groovyx.gpars.actor.DefaultActor
import humulus.Environment
import humulus.EnvironmentHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Actor which starts the TransitionActor for the repository configured in given the
 * WorkflowCommand instance.
 */
class WorkflowMaster extends DefaultActor {

    Logger log = LoggerFactory.getLogger(this.class)
    Boolean running = true
    TransitionActor transitionActor
    Long sleeping = 2000

    protected void act() {
        loop {
            react { WorkflowCommand command ->
                WorkflowResult result = new WorkflowResult()
                try {
                    switch (command.type) {
                        case WorkflowCommandType.RUN_WORKFLOW: startTransitions(command); break
                        case WorkflowCommandType.STOP_RUNNING: stopTransitions(); break
                    }
                    reply result
                }
                catch (Exception e) {
                    log.warn("Failed to act on command: ${command?.dump()}", e)
                    result.failed = true
                    result.messages.add(e.message)
                    reply result
                }
            }
        }
    }

    def startTransitions(WorkflowCommand command) {
        try {
            running = true
            if (!transitionActor) {
                transitionActor = new TransitionActor()
                transitionActor.start()
            }
            Map currentEnvironment = EnvironmentHolder.environment
            EnvironmentHolder.environment = Environment.list().find{it.dbName == command.repositoryName}
            WorkflowCommand cmd = new WorkflowCommand(type: WorkflowCommandType.DO_TRANSITION,
                    repositoryName: command.repositoryName,
                    taskObjectType: ObjectType.findByName(Constants.OBJTYPE_TASK),
                    workflowObjectType: ObjectType.findByName(Constants.OBJTYPE_WORKFLOW),
                    workflowTemplateType: ObjectType.findByName(Constants.OBJTYPE_WORKFLOW_TEMPLATE),
                    deadlineRelationType: RelationType.findByName(Constants.RELATION_TYPE_WORKFLOW_TO_DEADLINE_TASK)
            )
            cmd.initializeQueryStrings()
            runTransitions(cmd)
            EnvironmentHolder.environment = currentEnvironment
        }
        catch (Exception e) {
            log.warn("startTicking failed.", e)
        }
    }

    def runTransitions(WorkflowCommand cmd) {
        if (running) {
            log.debug("calling transitionActor for ${cmd.repositoryName}")
            WorkflowCommand wc = new WorkflowCommand(cmd)
            transitionActor.sendAndContinue(wc) { WorkflowResult result ->
                if (result.failed) {
                    log.warn("TransitionActor failed: ${result.messages}")
                }
                else {
                    Thread.sleep(sleeping)
                    runTransitions(wc)
                }
            }
        }
    }

    def stopTransitions() {
        running = false
    }
    
}
