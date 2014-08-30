package cinnamon

import cinnamon.exceptions.CinnamonConfigurationException
import cinnamon.global.Constants
import cinnamon.lifecycle.LifeCycle
import cinnamon.utils.ParamParser
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class RenderServerController extends BaseController {

    /**
     * Creates a new OSD of the object type render_task in the repository
     * in the specified folder.
     * This task object is read by the render server and updated with status updates by the render
     * process (which are written to custom metadata /meta/metaset=render_output/messages/). It is up
     * to the client to interpret the messages.
     * The overall status of the render task can be determined by looking at the procstate attribute:<br>
     * While the task is waiting for the render server to pick it up, its procstate is set to "waiting".
     * While the task is running, its procstate is set to "running".
     * After the task is finished, the task's procstate attribute is set to "finished".
     * If the task should fail, the task's procstate is set to "failed".
     * <h2>Required permissions</h2>
     * CREATE_OBJECT
     *
     * @param name optional name of the task, defaults to "RenderTask"
     * @param parentid  id of the folder where the task-object will be created
     * @param metadata xml to use as the metadata metaset=render_input field. It must contain
     * at least the element <pre>{@code <renderTaskName>}</pre> which holds the name of the
     * render task that will be performed. It should contain the sourceId element
     * to specify the id of the source content object to be rendered.<br>
     *     Example for metadata content:<br>
     *  <pre>{@code
     *  <metaset type="render_input"><sourceId>542</sourceId><renderTaskName>foo</renderTaskName></metaset>
     *  }</pre>
     * @return a CinnamonException if the object cannot be instantiated for any reason,
     *         or a Response object with the following XML content:
     * <pre>
     * {@code
     *   <startRenderTask>
     *     <taskObjectId>123</taskObjectId>
     *     <success>success.startRenderTask</success>
     *  </startRenderTask>
     * }
     * </pre>
     */
    def createRenderTask(String name, Long parentid, String metadata) {
        try {
            def user = userService.user
            // create object and validate permission
            ObjectSystemData osd = new ObjectSystemData(params, user, false);
            (new Validator(user)).validateCreate(osd.parent)

            String renderInput;
            if (metadata) {
                org.dom4j.Node meta = ParamParser.parseXml(metadata, "error.param.metadata");
                renderInput = meta.asXML();
            }
            else {
                renderInput = "";
            }
            
            String metasetStr = "<meta>"+renderInput+"<metaset type=\"render_output\"></metaset></meta>";
            org.dom4j.Node metaset = ParamParser.parseXml(metasetStr, null);
            
            LifeCycle lc = LifeCycle.findByName(Constants.RENDER_SERVER_LIFECYCLE);
            if(lc == null){
                throw new CinnamonConfigurationException(Constants.RENDER_SERVER_LIFECYCLE+" lifecycle was not found.");
            }
            if(lc.getDefaultState() == null){
                throw new CinnamonConfigurationException(Constants.RENDER_SERVER_LIFECYCLE+" lifecycle is not configured correctly. Needs defaultState.");
            }
            osd.setState(lc.getDefaultState());
            osd.setProcstate(Constants.RENDERSERVER_RENDER_TASK_NEW);

            if(name){
                osd.name = name.trim()
            }
            else{
                osd.name = "RenderTask"
            }

            ObjectType renderTaskType = ObjectType.findByName(Constants.OBJECT_TYPE_RENDER_TASK);
            if (renderTaskType == null) {
                throw new CinnamonConfigurationException("Could not find required render task object type.");
            }
            osd.setType(renderTaskType);
            osd.save(flush:true)
            osd.metadata = metaset.asXML()
            
            // create response
            render(contentType: 'application/xml') {
                startRenderTask{
                    taskObjectId(osd.id.toString())
                    success('success.startRenderTask')
                }
            }

        }
        catch (Exception e) {
            renderExceptionXml("Failed to execute $actionName.", e)
        }
    }
}
