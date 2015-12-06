package cinnamon

import cinnamon.global.PermissionName
import cinnamon.references.Link
import cinnamon.references.LinkResolver
import cinnamon.references.LinkService
import cinnamon.references.LinkType
import cinnamon.utils.ParamParser
import grails.plugin.springsecurity.annotation.Secured
import org.dom4j.DocumentHelper

@Secured(["hasRole('_users')"])
class LinkController extends BaseController {

    //------------------- XML API for Cinnamon Desktop Client -------------

    /**
     * <h2>Parameters in HTTP Request</h2>
     * <ul>
     * <li>command=createlink</li>
     * <li>id= the id of the target object or folder which to which the link will point</li>
     * <li>acl_id= the acl of the link</li>
     * <li>owner_id= the owner of the link</li>
     * <li>type= one of FOLDER or OBJECT, to determine the type of linked object.</li>
     * <li>[resolver]= how the link should be resolved: defaults to FIXED, may be LATEST_HEAD for type=OBJECT</li>
     * <li>parent_id= the id of the folder with which to associate the new link object</li>
     * <li>ticket=session ticket</li>
     * </ul>
     * <p/>
     * <h2>Needed permissions</h2>
     * <ul>
     * <li>BROWSE_OBJECT (or: BROWSE_FOLDER)</li>
     * </ul>
     *
     * Renders an XML response with either an error message or
     *         the following XML content:
     *         <pre>
     * {@code
     *          <link>
     *              <object>
     *                  ... (object data)
     *                  <reference>
     *                      <linkId>id of the link object</linkId>
     *                      <id>link target id</id>
     *                      <parentId></parentId>
     *                      <aclId></aclId>
     *                      <ownerId></ownerId>
     *                      <resolver>FIXED</resolver>
     *                      <type>OBJECT</type>
     *                  </reference>
     *              </object>
     *
     *              or, in case of type=FOLDER:
     *              <folder>
     *                  ... (folder data)
     *                  <reference>
     *                      <linkId>id of the link object</linkId>
     *                      <id>link target id</id>
     *                      <parentId></parentId>
     *                      <aclId></aclId>
     *                      <ownerId></ownerId>
     *                      <resolver>FIXED</resolver>
     *                      <type>FOLDER</type>
     *                  </reference>
     *              </folder>
     *          </link>
     *}
     *         </pre>
     */
    def createLink(Boolean include_summary) {
        try {

            if (params.id == null) {
                throw new RuntimeException("error.param.id")
            }

            Acl acl = Acl.get(ParamParser.parseLong(params.acl_id, "error.param.acl_id"))
            Folder parent = Folder.get(ParamParser.parseLong(params.parent_id, "error.param.parent_id"))
            UserAccount owner = UserAccount.get(ParamParser.parseLong(params.owner_id, "error.param.owner_id"))
            ObjectSystemData osd
            Folder folder
            String typeName = params.type
            Validator validator = new Validator(userService.user)
            LinkResolver resolver
            if (params.containsKey("resolver")) {
                resolver = LinkResolver.valueOf(params.resolver)
            } else {
                resolver = LinkResolver.FIXED
            }

            Link link;
            LinkType linkType = LinkType.valueOf(typeName);
            if (linkType == LinkType.FOLDER) {
                folder = Folder.get(params.id);
                if (folder == null) {
                    throw new RuntimeException("error.param.id");
                }
                validator.validatePermission(folder.getAcl(), PermissionName.BROWSE_FOLDER);
                link = linkService.createLink(folder, parent, acl, owner, resolver);
            } else {
                osd = ObjectSystemData.get(params.id);
                if (osd == null) {
                    throw new RuntimeException("error.param.id");
                }
                validator.validatePermission(osd.getAcl(), PermissionName.BROWSE_OBJECT);
                link = linkService.createLink(osd, parent, acl, owner, resolver);
            }

            def doc = DocumentHelper.createDocument()
            doc = linkService.renderLinkWithinTarget(link, doc, include_summary);
            log.debug("result of createLink:\n" + doc.asXML());
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch objects: ", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * <h2>Parameters in HTTP Request</h2>
     * Most parameters are optional [...], but you must include at least one of them,
     * otherwise this method will do nothing.
     * <ul>
     * <li>command=updatelink (mapped via CinnamonController to /link/updateLink)</li>
     * <li>link_id= if of the link object </li>
     * <li>[acl_id]= new acl id for the link</li>
     * <li>[owner_id]= new owner for the link</li>
     * <li>[resolver]= how the link should be resolved: defaults to FIXED, may be LATEST_HEAD for type=OBJECT</li>
     * <li>[parent_id]= the id of the folder with which to associate the new link object</li>
     * <li>[object_id]= the id of another version of the linked object (gives error if object.root is different).</li>
     * <li>ticket=session ticket</li>
     * </ul>
     * <h2>Needed permissions</h2>
     * <ul>
     *   <li>BROWSE_OBJECT (or: BROWSE_FOLDER)</li>
     *   <li>WRITE_OBJECT_SYS_METADATA</li>
     *   <li>SET_ACL for changes to the ACL.</li>
     * </ul>
     * Renders an XML response containing an error message or 
     *         the following XML content:
     *         <pre>
     * {@code
     *          <link>
     *              <object>
     *                  ... (object data)
     *                  <reference>
     *                      <linkId>id of the link object</linkId>
     *                      <id>link target id</id>
     *                      <parentId></parentId>
     *                      <aclId></aclId>
     *                      <ownerId></ownerId>
     *                      <resolver>FIXED</resolver>
     *                      <type>FOLDER</type>
     *                  </reference>
     *              </object>
     *
     *              or, in case of type=FOLDER:
     *              <folder>
     *                  ... (folder data)
     *                  <reference>
     *                      <linkId>id of the link object</linkId>
     *                      <id>link target id</id>
     *                      <id></id>
     *                      <parentId></parentId>
     *                      <aclId></aclId>
     *                      <ownerId></ownerId>
     *                      <resolver>FIXED</resolver>
     *                      <type>FOLDER</type>
     *                  </reference>
     *              </folder>
     *          </link>
     *}
     *         </pre>
     *         <p/>
     */
    def updateLink(Boolean include_summary) {
        try {
            Long linkId = ParamParser.parseLong(params.link_id, "error.param.link_id");
            Link link = Link.get(linkId)
            if (link == null) {
                throw new RuntimeException("error.object.not.found");
            }

            Validator validator = new Validator(userService.user);
            validator.validatePermission(link.getAcl(), PermissionName.WRITE_OBJECT_SYS_METADATA);
            if (params.containsKey("acl_id")) {
                validator.validatePermission(link.getAcl(), PermissionName.SET_ACL);
            }

            link = linkService.updateLink(link, params);

            def doc = DocumentHelper.createDocument()
            doc = linkService.renderLinkWithinTarget(link, doc, include_summary)
            log.debug("result of updateLink:\n" + doc.asXML());
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch objects: ", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * <h2>Parameters in HTTP Request</h2>
     * Most parameters are optional [...], but you must include at least one of them,
     * otherwise this method will do nothing.
     * <ul>
     * <li>command=getlink (mapped to /link/getLink by CinnamonController)</li>
     * <li>link_id= id of the link object </li>
     * <li>ticket=session ticket</li>
     * </ul>
     * <h2>Needed permissions</h2>
     * <ul>
     *   <li>BROWSE_OBJECT (or: BROWSE_FOLDER)</li>
     * </ul>
     * Renders an XML exception message on failure or 
     *         the following XML content:
     *         <pre>
     * {@code
     *          <link>
     *              <object>
     *                  ... (object data)
     *                  <reference>
     *                      <linkId>id of the link object</linkId>
     *                      <id>link target id</id>
     *                      <parentId></parentId>
     *                      <aclId></aclId>
     *                      <ownerId></ownerId>
     *                      <resolver>FIXED</resolver>
     *                      <type>FOLDER</type>
     *                  </reference>
     *              </object>
     *
     *              or, in case of type=FOLDER:
     *              <folder>
     *                  ... (folder data)
     *                  <reference>
     *                      <linkId>id of the link object</linkId>
     *                      <id>link target id</id>
     *                      <id></id>
     *                      <parentId></parentId>
     *                      <aclId></aclId>
     *                      <ownerId></ownerId>
     *                      <resolver>FIXED</resolver>
     *                      <type>FOLDER</type>
     *                  </reference>
     *              </folder>
     *          </link>
     *}
     *         </pre>
     *         <p/>
     */
    def getLink(Boolean include_summary) {
        try {
            Long linkId = ParamParser.parseLong(params.link_id, "error.param.link_id")
            Link link = Link.get(linkId);
            if (link == null) {
                throw new RuntimeException("error.object.not.found")
            }

            Validator validator = new Validator(userService.user)
            if (link.getType() == LinkType.FOLDER) {
                validator.validatePermission(link.getAcl(), PermissionName.BROWSE_FOLDER);
                validator.validatePermission(link.getFolder().getAcl(), PermissionName.BROWSE_FOLDER);
            } else {
                validator.validatePermission(link.getAcl(), PermissionName.BROWSE_OBJECT);
                validator.validatePermission(link.getOsd().getAcl(), PermissionName.BROWSE_OBJECT);
            }

            def doc = DocumentHelper.createDocument()
            doc = linkService.renderLinkWithinTarget(link, doc, include_summary)
            log.debug("result of updateLink:\n" + doc.asXML())
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch objects: ", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * <h2>Parameters in HTTP Request</h2>
     * <ul>
     * <li>command=deletelink (mapped by CinnamonController to /link/deleteLink)</li>
     * <li>link_id= id of the link object </li>
     * <li>ticket=session ticket</li>
     * </ul>
     * <h2>Needed permissions</h2>
     * <ul>
     * <li>DELETE_FOLDER or DELETE_OBJECT on the link's acl.</li>
     * </ul>
     *
     * Renders an XML exception on failure or 
     *         the following XML content:
     *         <pre>
     * {@code
     *         <success>success.delete.link</success>
     *}
     *         </pre>
     */
    def deleteLink() {
        try {
            Long linkId = ParamParser.parseLong(params.link_id, "error.param.link_id")
            Link link = Link.get(linkId)
            if (link == null) {
                throw new RuntimeException("error.object.not.found");
            }

            Validator validator = new Validator(userService.user)
            if (link.getType().equals(LinkType.FOLDER)) {
                validator.validatePermission(link.getAcl(), PermissionName.DELETE_FOLDER);
            } else {
                validator.validatePermission(link.getAcl(), PermissionName.DELETE_OBJECT);
            }

            link.delete()
            def doc = DocumentHelper.createDocument()
            doc.addElement("success").addText("success.delete.link");

            log.debug("result of deleteLink:\n" + doc.asXML());
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to delete link: ", e)
            renderExceptionXml(e.message)
        }

    }

}
