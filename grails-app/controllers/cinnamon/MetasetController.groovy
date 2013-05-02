package cinnamon

import cinnamon.exceptions.CinnamonException
import cinnamon.global.PermissionName
import cinnamon.index.IndexAction
import cinnamon.index.Indexable
import cinnamon.interfaces.IMetasetOwner
import org.dom4j.DocumentHelper
import org.dom4j.Element

class MetasetController extends BaseController{
    
    def metasetService
    
    //----------------------- XML API ---------------------
    
    /**
     * Retrieves an object's metaset.
     * <h2>Needed permissions</h2>
     * READ_OBJECT_CUSTOM_METADATA
     * @param id object id
     * @param class_name one of Folder, OSD, Metaset [Note: Metaset currently disabled so
     * users cannot bypass ACL restrictions] 
     * @param type_name the name of the metaset type
     * @return XML-Response:
     *         The metaset of the specified object or
     *         <pre>
     *             {@code
     *             <metaset id="0" type="$typeName" status="empty"/>
     *             }
     *         </pre>
     */
    def fetchMetaset(Long id, String class_name, String type_name) {
        try {
            def metasetOwner = fetchMetasetOwner(id, class_name, false)
            Metaset metaset = metasetOwner.fetchMetaset(type_name)            
            if (metaset){
                render(contentType: 'application/xml', text: metaset.content)
            }
            else{
                MetasetType type = MetasetType.findByName(type_name)
                if (! type){
                    renderErrorXml("type_name ${type_name} is invalid", 'error.invalid.type_name')
                }
                else{
                render(contentType: 'application/xml'){
                    "metaset"(id:'0', type:type_name, status:'empty')
                }
                }
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to fetchMetaset', e)
        }
    }

    /**
     * Creates or updates a metaset for an object.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_CUSTOM_METADATA or EDIT_FOLDER
     * @param id id of Folder or OSD
     * @param class_name one of (Folder,OSD)
     * @param type_name the name of the metaset type
     * @param content the XML content
     *            <pre>
     *              {@code
     *              <metaset type="$typeName">
     *              ... metaset data ...
     *              </metaset>
     *              }
     *            </pre>
     * @param write_policy optional write policy, allowed values are write|ignore|branch, default is branch.
     *            On write, the content is written regardless of other items linking to this metaset.
     *            On ignore, the content is ignored if there are other references to the metaset.
     *            On branch, if other references exist, a separate metaset for this item is created.
     * @return XML-Response:
     *         The metaset or an XML error message.
     */
    def saveMetaset(Long id, String class_name, String type_name, String content, String write_policy) {
        try {
            def metasetOwner = fetchMetasetOwner(id, class_name, true)
            MetasetType metasetType = MetasetType.findByName(type_name);
            if(metasetType== null){
                throw new CinnamonException("error.param.type_name")
            }

            WritePolicy writePolicy = WritePolicy.BRANCH;
            if(write_policy){
                writePolicy = WritePolicy.valueOf(write_policy)
            }

            Metaset metaset = metasetService.createOrUpdateMetaset(metasetOwner, metasetType, content, writePolicy);
            LocalRepository.addIndexable(metasetOwner, IndexAction.UPDATE)
            def doc = DocumentHelper.createDocument()
            doc.add(Metaset.asElement('meta', metaset))
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            renderExceptionXml('Failed to saveMetaset.', e)
        }
    }

    /**
     * Link a metaset to an object.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_CUSTOM_METADATA or EDIT_FOLDER
     * @param id id of Folder or OSD
     * @param class_name one of (Folder,OSD)
     * @param metaset_id Id of the metaset 
     * @return XML-Response:
     *         <pre>
     *             {@code
     *             <success>success.link.metaset</success>
     *             }
     *         </pre>
     *         or a standard error message.
     */
    def linkMetaset(Long id, Long metaset_id, String class_name) {
        try {
            def metasetOwner = fetchMetasetOwner(id, class_name, true)
            Metaset metaset = Metaset.get(metaset_id)
            if(metaset == null){
                throw new CinnamonException("error.param.metaset_id");
            }
            metasetOwner.addMetaset(metaset);
            render(contentType: 'application/xml') {
                success('success.link.metaset')
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to ', e)
        }
    }

    
    /**
     * Remove the link between a metaset and an item.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_CUSTOM_METADATA or EDIT_FOLDER
     * @param id id of Folder or OSD
     * @param class_name one of (Folder,OSD)
     * @param metaset_id Id of the metaset 
     * @return XML-Response:
     *         <pre>
     *             {@code
     *             <success>success.unlink.metaset</success>
     *             }
     *         </pre>
     *         or a standard error message.
     */
    def unlinkMetaset(Long id, Long metaset_id, String class_name) {
        try {
            def metasetOwner = fetchMetasetOwner(id, class_name, true)
            Metaset metaset = Metaset.get(metaset_id)
            if(metaset == null){
                throw new CinnamonException("error.param.metaset_id")
            }

            metasetService.unlinkMetaset(metasetOwner, metaset)
            luceneService.updateIndex(metasetOwner, repositoryName)

            render(contentType: 'application/xml') {
                success('success.link.metaset')
            }
        }
        catch (Exception e) {
            renderExceptionXml("Failed to execute $actionName.", e)
        }
    }

    /**
     * Fetch a MetasetOwner specified by id and classNname.
     * Validates the write permissions to the object's custom metadata (if 
     * current user is not allowed to update the object's metaset links, throws an
     * exception during validation).
     * @param id id of the object
     * @param className either 'Folder' or 'OSD' (for ObjectSystemData)
     * @param validateWriteAccess if true, verify the user's permission to 
     *        change this object's custom metadata.
     * @return the validated IMetasetOwner (a Folder or OSD object)
     */
    protected IMetasetOwner fetchMetasetOwner(Long id, String className, Boolean validateWriteAccess){
        IMetasetOwner metasetOwner
        def validator = new Validator(userService.user)
        switch(className){
            case 'Folder':
                Folder folder = Folder.get(id)
                if (! folder){
                    throw new CinnamonException('error.folder.not.found')
                }
                if (validateWriteAccess){
                    validator.validatePermissions(folder,
                            [PermissionName.EDIT_FOLDER, PermissionName.READ_OBJECT_CUSTOM_METADATA])
                }
                else{
                    validator.validateGetFolderMeta(folder)
                }

                metasetOwner = folder
                break
            case 'OSD':
                ObjectSystemData osd = ObjectSystemData.get(id)
                if (! osd){
                    throw new CinnamonException('error.object.not.found')
                }
                if (validateWriteAccess){
                    validator.validatePermissions(osd, [PermissionName.WRITE_OBJECT_CUSTOM_METADATA, PermissionName.READ_OBJECT_CUSTOM_METADATA])
                }
                else{
                    validator.validateGetMeta(osd)
                }
                metasetOwner = osd
                break
            default: throw new CinnamonException("error.param.class_name")
        }
        return metasetOwner
    }
}
