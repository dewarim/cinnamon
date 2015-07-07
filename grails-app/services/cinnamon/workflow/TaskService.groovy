package cinnamon.workflow

import cinnamon.ConfigEntry
import cinnamon.Folder
import cinnamon.ObjectSystemData
import cinnamon.ObjectType
import cinnamon.UserAccount
import cinnamon.config.DataTransferConfig
import cinnamon.exceptions.CinnamonConfigurationException
import cinnamon.global.Constants
import cinnamon.lifecycle.LifeCycle
import cinnamon.lifecycle.LifeCycleState
import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import cinnamon.workflow.actions.CinnamonNotification
import cinnamon.workflow.actions.WorkflowMail
import groovy.text.SimpleTemplateEngine
import org.slf4j.LoggerFactory
import org.w3c.dom.Document

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Service class for commonly used functions required by tasks and workflow API
 */
class TaskService {
    
    def infoService
    def osdService
    def folderService
    
    def log = LoggerFactory.getLogger(this.class)

    def builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    ObjectSystemData fetchWorkflowForTask(ObjectSystemData task) {
        RelationType taskRelType = RelationType.findByName(Constants.RELATION_TYPE_WORKFLOW_TO_TASK);
        ObjectSystemData workflow = Relation.findByRightOSDAndType(task, taskRelType)?.leftOSD;
        return workflow
    }

    /**
     * Save a list of parameter nodes identified by their name from a given task to 
     * its workflow.   
     * @param task the task from which the task_definition metaset will be fetched.
     * @param paramNames the names of the parameter elements. Each parameter is in the form
     * of <pre>{@code
     *     <param>
     *         <!-- other elements -->
     *         <name>document</name>
     *     </param>
     *}
     *     </pre>
     */
    void saveParamsToWorkflow(ObjectSystemData task, List<String> paramNames) {
        def taskDef = metasetToDocument(task, Constants.METASET_TASK_DEFINITION)
        ObjectSystemData workflow = fetchWorkflowForTask(task)
        saveParamsToWorkflow(taskDef, workflow, paramNames)
    }

    /**
     * Save a list of parameter nodes identified by their name from a given task to 
     * its workflow. This version of the method takes the explicit taskDef string and 
     * workflow objects, so it is possible to do several units of work on the taskDef 
     * and the workflow without having to save / fetch them repeatedly from the database.
     *
     * @param task the task 
     * @param taskDef the content of the task's task_definition metaset as an XML document
     * @param workflow the workflow object connected to the given task
     * @param paramNames the names of the parameter elements. Each parameter is in the form
     * of <pre>{@code
     *     <param>
     *         <!-- other elements -->
     *         <name>document</name>
     *     </param>
     *}
     *     </pre>
     */
    void saveParamsToWorkflow(Document taskDoc, ObjectSystemData workflow, List<String> paramNames) {
        def workflowDef = workflow.fetchMetaset(Constants.METASET_WORKFLOW_TEMPLATE).content
        def workflowDoc = builder.parse(new ByteArrayInputStream(workflowDef.bytes))
        org.w3c.dom.Node targetNode = selectNodes(workflowDoc, '//params').item(0)
        paramNames.each { name ->
            addParamNode(taskDoc, targetNode, name)
        }
        saveMetaset(workflowDoc, workflow, Constants.METASET_WORKFLOW_TEMPLATE)
    }

    /**
     * Take a list of parameter nodes from a task's task_definition metaset and 
     * append them to another task as "fixed" elements. 
     * @param sourceTask
     * @param targetTask
     * @param paramNames
     */
    void saveParamsToTask(ObjectSystemData sourceTask, ObjectSystemData targetTask, String taskDef, List<String> paramNames) {
        if (!taskDef) {
            taskDef = sourceTask.fetchMetaset(Constants.METASET_TASK_DEFINITION).content
        }
        def targetDef = targetTask.fetchMetaset(Constants.METASET_TASK_DEFINITION).content
        def taskDoc = builder.parse(new ByteArrayInputStream(taskDef.bytes))
        def targetDoc = builder.parse(new ByteArrayInputStream(targetDef.bytes))
        org.w3c.dom.Node targetNode = selectNodes(targetDoc, '//input/fixed').item(0)
        paramNames.each { name ->
            addParamNode(taskDoc, targetNode, name)
        }
        saveMetaset(targetDoc, targetTask, Constants.METASET_TASK_DEFINITION)
    }

    void addParamNode(source, org.w3c.dom.Node targetNode, paramName){
        def node = selectFirstNode(source,
                "//input/*[self::required or self::optional or self::fixed]/param[name='$paramName']")
        if (node) {
            def pNode = targetNode.ownerDocument.importNode(node, true)
            targetNode.appendChild(pNode)
        }
        else {
            log.debug("Node for param $paramName was not found.")
        }
    }
    
    /**
     * Create a new version of the given object and copy the existing content.
     * @param osd the OSD to copy
     * @param repositoryName the name of the repository wherein the OSD is stored
     * @return the new version of the given object
     */
    ObjectSystemData createNewVersionCopy(ObjectSystemData pre, ObjectSystemData workflow) {
        ObjectSystemData copy = new ObjectSystemData(pre, workflow.owner);
        copy.predecessor = pre
        copy.cmnVersion = copy.createNewVersionLabel()
        copy.root = pre.root
        // (new Validator(user)).validateVersion(pre);

        copy.save()
        osdService.copyContent(pre,copy)

        // execute the new LifeCycleState if necessary.
        if (copy.state) {
            copy.state.enterState(copy, copy.state)
        }

        pre.fixLatestHeadAndBranch([copy])
        // schedule pre and copy for re-indexing 
        return copy
    }

    org.w3c.dom.NodeList selectNodes(doc, String xpath) {
        def expr = XPathFactory.newInstance().newXPath().compile(xpath)
        return (org.w3c.dom.NodeList) expr.evaluate(doc, XPathConstants.NODESET)
    }

    /**
     * Return the first node returned by executing a given XPath expression 
     * against a document.
     * @param doc the document from which the node will be selected.
     * @param xpath the XPath expression to use for node selection
     * @return the first node found - or null if the node list was empty.
     */
    org.w3c.dom.Node selectFirstNode(doc, xpath) {
        def nodeList = selectNodes(doc, xpath)
        if (nodeList.length) {
            return nodeList.item(0)
        }
        else {
            return null
        }
    } 
    
    /**
     * Return the text content of the first node returned by executing a given XPath expression 
     * against a document.
     * @param doc the document from which the node will be selected.
     * @param xpath the XPath expression to use for node selection
     * @return the first node found - or an empty string if the node list was empty.
     */
    String selectFirstNodeText(doc, xpath) {
        def nodeList = selectNodes(doc, xpath)
        if (nodeList.length) {
            return nodeList.item(0).textContent ?: ''
        }
        else {
            return ''
        }
    }

    /**
     * Take a org.w3c.Document and replace a given OSD's metaset identified by the
     * metasetName parameter.
     * @param doc the Document
     * @param osd the target OSD whose metaset will be replaced 
     * @param metasetName the name of the metaset type
     * @return the document as a string (for logging etc)
     */
    String saveMetaset(Document doc, ObjectSystemData osd, String metasetName) {
        def content = docToString(doc)
        osd.fetchMetaset(metasetName, true).content = content
        return content
    }

    /**
     * Convert a org.w3c.Document object into a string. 
     * @param doc the document to stringify
     * @return the textual representation of the given XML document.
     */
    String docToString(Document doc) {
        Transformer transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        StreamResult result = new StreamResult(new StringWriter())
        DOMSource source = new DOMSource(doc)
        transformer.transform(source, result)
        String xmlString = result.getWriter().toString()
        return xmlString
    }

    Document stringToDoc(String xml) {
        return builder.parse(new ByteArrayInputStream(xml.bytes))
    }

    Document metasetToDocument(ObjectSystemData osd, String metasetName) {
        def content = osd.fetchMetaset(metasetName).content
        def document = builder.parse(new ByteArrayInputStream(content.bytes))
        return document
    }

    LifeCycle loadLifecycleFromDoc(Document doc, String xpath) {
        def name = selectFirstNode(doc, xpath).textContent
        return LifeCycle.findByName(name)
    }

    LifeCycleState loadLifeCycleAndStateFromDoc(Document doc, String lifecycleXpath, String stateXpath) {
        def lifeCycle = loadLifecycleFromDoc(doc, lifecycleXpath)
        def stateName = selectFirstNode(doc, stateXpath).textContent
        return LifeCycleState.findByNameAndLifeCycle(stateName, lifeCycle)
    }

    /**
     * Load a lifecycle state named by a parameter value found in the given XML document. 
     * @param doc an XML document
     * @param stateXpath an XPath expression which returns the node that contains the
     *  lifecycle state's name
     * @param lifeCycle the lifecycle to which the state belongs
     * @return the lifecycle state which is configured in the given XML document.
     */
    LifeCycleState loadLifeCycleStateFromDoc(Document doc, String stateXpath, LifeCycle lifeCycle) {
        def stateName = selectFirstNode(doc, stateXpath).textContent
        return LifeCycleState.findByNameAndLifeCycle(stateName, lifeCycle)
    }

    List<UserAccount> fetchUserListFromParam(doc, String paramName) {
        def nodeList = selectNodes(doc, "//param[name='$paramName' and type='user-list']/values/value")
        def userList = []
        nodeList.each { org.w3c.dom.Node node ->
            def user = UserAccount.get(node.textContent)
            if (user) {
                userList.add(user)
            }
        }
        return userList
    }

    UserAccount fetchUserFromParam(Document doc, String paramName) {
        def userNode = selectFirstNode(doc, "//param[name='$paramName' and type='user']/values/value")
        return UserAccount.get(userNode.textContent)
    }

    ObjectSystemData fetchOsdFromParam(Document doc, String paramName) {
        def id = selectFirstNode(doc, "//param[name='$paramName']/value").textContent?.toLong()
        return ObjectSystemData.get(id)
    }

    ObjectSystemData createRenderTask(ObjectSystemData source, UserAccount user, String renderTaskName) {
        Folder renderTaskFolder = folderService.findByPath("/system/transient/render_tasks");
        if (renderTaskFolder == null) {
            throw new CinnamonConfigurationException(
                    "folder path /system/transient/render_tasks does not exist.");
        }

        Map<String, Object> cmd = [name: "RT_${source.id}", parentid: "${renderTaskFolder.id}"]
        ObjectSystemData renderTask = new ObjectSystemData(cmd, user, false);

        LifeCycle lc = LifeCycle.findByName(Constants.RENDER_SERVER_LIFECYCLE);
        if (lc == null) {
            throw new CinnamonConfigurationException(Constants.RENDER_SERVER_LIFECYCLE +
                    " lifecycle was not found.");
        }
        if (lc.getDefaultState() == null) {
            throw new CinnamonConfigurationException(Constants.RENDER_SERVER_LIFECYCLE +
                    " lifecycle is not configured correctly. Needs defaultState.");
        }
        renderTask.setState(lc.getDefaultState());
        renderTask.setProcstate(Constants.RENDERSERVER_RENDER_TASK_NEW);

        ObjectType renderTaskType = ObjectType.findByName(Constants.OBJECT_TYPE_RENDER_TASK);
        if (renderTaskType == null) {
            throw new CinnamonConfigurationException("Could not find required render task object type.");
        }
        renderTask.setType(renderTaskType);
        renderTask.save()

        String renderInput = """<metaset type="render_input">
                <renderTaskName>calculatrix</renderTaskName>
                <parameter name="keep_debug_data">true</parameter>
                <parameter name="render_target">MS Office to PDF</parameter>
                <data type="explicit"><object id="$source.id></data></metaset>"""
        String renderOutput = """<metaset type="render_output"></metaset>"""

        saveMetaset(stringToDoc(renderInput), renderTask, Constants.METASET_RENDER_INPUT)
        saveMetaset(stringToDoc(renderOutput), renderTask, Constants.METASET_RENDER_OUTPUT)
        return renderTask
    }

    List<ObjectSystemData> findOpenTasks(ObjectSystemData workflow) {
        ObjectType taskObjectType = ObjectType.findByName(Constants.OBJTYPE_TASK)
        List<ObjectSystemData> tasks =
            ObjectSystemData.findAll("""select o from ObjectSystemData o where 
            o.type=:type and 
            o.procstate=:procstate and 
            o.owner=:owner 
            and o.id in (select r1.rightOSD.id from Relation r1 where r1.leftOSD=:leftOSD)""",
            [type:taskObjectType, procstate:Constants.PROCSTATE_TASK_TODO, owner:workflow]
            )
        return tasks
    }

    /**
     * Fetch a list of all parameters of a given group which look like they
     * have values attached to them.
     * @param document the document to examine
     * @param groupName the text content of the param-group element (that is, 
     * the name of the param-group)
     * @return a list of all param-nodes found.
     */
    org.w3c.dom.NodeList fetchParamGroup(Document document, groupName) {
        selectNodes(document, "//param[param-group='$groupName' and ((string-length(value) > 0) or (count(values/value) > 0))]")
    }

    void createNotifications(org.w3c.dom.Node actionNode, model) {
        def users = fetchUserListFromParam(actionNode, 'values/value')
        def actionLabel = selectFirstNode(actionNode, 'label').textContent
        model.put(actionLabel, actionLabel)
        def content = """<metaset>${getNotificationContent(actionNode, 'content', model)}</metaset>"""
        users.each { user ->
            new CinnamonNotification().create(user, actionLabel, content)
        }
    }

    void createWorkflowMail(org.w3c.dom.Node actionNode, model) {
        def users = fetchUserListFromParam(actionNode, 'values/value')
        def actionLabel = selectFirstNode(actionNode, 'label').textContent
        model.put(actionLabel: actionLabel)
        def content = """<metaset>${getNotificationContent(actionNode, 'content', model)}</metaset>"""
        users.each { user ->
            if (user.email) {
                new WorkflowMail().create(user.email, actionLabel, content)
            }
        }
    }

    String getNotificationContent(actionNode, xpath, model) {
        def notificationContent = selectFirstNode(actionNode, xpath).textContent
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate(notificationContent).make(model)
        return template.toString()
    }
    
    void uploadDocument(actionNode, model){
        def transferConfig = new DataTransferConfig()
        def fileService = new FileService()
        def ftpConfigEntry = ConfigEntry.findByName('workflow.ftp.servers')
        def ftpConfig = stringToDoc(ftpConfigEntry.config)
        def entryName = selectFirstNode(actionNode, 'value').textContent
        log.debug("name of server-entry: ${entryName}")
        def ftpNode = selectFirstNode(ftpConfig, "//ftp[name='$entryName']/")
        ObjectSystemData document = model.document
        
        transferConfig.serverName = selectFirstNodeText(ftpNode, 'server')
        transferConfig.username = selectFirstNodeText(ftpNode, 'username')
        transferConfig.password = selectFirstNodeText(ftpNode, 'password')
        transferConfig.port = selectFirstNodeText(ftpNode, 'port')?.toInteger() ?: 21
        transferConfig.remotePath = selectFirstNodeText(model.taskMetasetDoc, 
                "//param[name='success.ftp.remote-path']/value")  ?: selectFirstNodeText(ftpNode, 'remotePath')
        transferConfig.filename = selectFirstNodeText(model.taskMetasetDoc,
                "//param[name='success.ftp.filename']/value")  ?: document.name
        def protocol = selectFirstNodeText(ftpNode, 'protocol')
        def file = new File(document.getFullContentPath())
        switch (protocol){
            case 'ftp': fileService.ftpUpload(file, transferConfig)
                break
            case 'ftps': fileService.ftpsUpload(file, transferConfig)
                break
            default: throw new RuntimeException('error.upload.fail')
        }
    }
    
    ObjectSystemData fetchLatestHead(ObjectSystemData osd){
        return osd.findLatestHead()
    }
}
