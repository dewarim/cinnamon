package cinnamon

import cinnamon.exceptions.CinnamonException
import cinnamon.index.IndexAction
import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import grails.plugin.springsecurity.annotation.Secured
import org.dom4j.DocumentHelper
import org.dom4j.Element

@Secured(["isAuthenticated()"])
class RelationController extends BaseController {

    def relationService

    // AJAX method
    def create(Long id) {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(id)
            def folders = osd.parent.subfolders.findAll { Folder f ->
                folderService.mayBrowseFolder(f, userService.user)
            }
            def candidates = folderService.getFolderContent(osd.parent, false)
            log.debug("candidates: ${candidates}")
            render(template: 'create', model: [osd: osd, folders: folders, folderType: 'relation', candidates: candidates])
        }
        catch (Exception e) {
            log.debug("failed: relation.create", e)
            renderException(e.message)
        }
    }

    // AJAX method
    def save(Long osd, Long rightOsd, Long leftOsd, Long type) {
        try {
            if (rightOsd != osd && leftOsd != osd) {
                throw new RuntimeException('error.foreign.relations')
            }

            ObjectSystemData right = fetchAndFilterOsd(rightOsd)
            ObjectSystemData left = fetchAndFilterOsd(leftOsd)
            RelationType rt = RelationType.get(type)
            def alreadyExists = Relation.findByTypeAndLeftOSDAndRightOSD(rt, left, right)
            if (!alreadyExists) {
                Relation relation = new Relation(rt, left, right, '<meta/>')
                relation.save()
                LocalRepository.addIndexable(left, IndexAction.UPDATE)
                LocalRepository.addIndexable(right, IndexAction.UPDATE)
            }
            forward(controller: 'osd', action: 'listRelations', id: osd)
        }
        catch (Exception e) {
            log.debug("failed: relation.create", e)
            renderException(e.message)
        }
    }

    // AJAX method
    def delete(Long id) {
        try {
            Relation relation = Relation.get(id)
            if (relation) {
                def leftOsd = relation.leftOSD
                def rightOsd = relation.rightOSD
                relation.delete(flush: true)
                LocalRepository.addIndexable(leftOsd, IndexAction.UPDATE)
                LocalRepository.addIndexable(rightOsd, IndexAction.UPDATE)
            }
            render(status: 200, text: '<!-- delete relation: success -->')
        }
        catch (Exception e) {
            log.debug("failed: relation.create", e)
            renderException(e.message)
        }
    }

    //---------------------------------------------------
    // Cinnamon XML Server API
    
    /* The getrelations command retrieves a list of relations.
     * Without the "name", leftid and rightid parameters, it lists all relations.
     * If one or both of the ids and \/ or the name are specified, the results will be filtered accordingly.
     *
     * @param name relation type (optional)
     * @param leftid id of "left object" (optional)
     * @param rightid id of "right object" (optional)
     * @param include_metadata optional parameter whether to include or exclude metadata
     *            from the XML response, defaults to 'true'
     * @return XML response: the serialized relation objects.
     */
    @Secured(["isAuthenticated()"])
    def listXml(String name, Long leftid, Long rightid, Boolean include_metadata) {
        List<Relation> relations

        def criteria = Relation.createCriteria()
        relations = criteria.list {
            if (name) {
                type{
                    eq('name', name)
                }
            }
            if(leftid){
                leftOSD{
                    eq('id', leftid)
                }
            }
            if(rightid){
                rightOSD{
                    eq('id', rightid)
                }
            }
        }


        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("relations");
        relations.each { relation ->
            relation.toXmlElement(root, include_metadata != null ? include_metadata : true)
        }
        render(contentType: 'application/xml', text: doc.asXML())
    }

    /**
     * The createrelation command links the objects specified by leftid and rightid
     * with a relation of the type specified by name. If the relation already exists,
     * it will not create a new one but return the existing relation.
     *
     * @param leftid id of "left object"
     * @param rightid id of "right object"
     * @param name of relation type
     * @param metadata optional metadata in XML format, defaults to {@code <meta/>}
     * @return XML response with format:
     *         <pre>
     * {@code
     *                            <relations>
     *                          <relation>
     *                              <id>123</id>
     *                              <leftId>true</leftId>
     *                              <rightId>false</rightId>
     *                              <metadata>....</metadata>
     *                              <type>name of type</type>
     *                         </relations>
     *}
     *                         </pre>
     */
    @Secured(["isAuthenticated()"])
    def createXml(String name, Long leftid, Long rightid, String metadata) {
        try {
            def user = userService.user
            ObjectSystemData left = ObjectSystemData.get(leftid)
            ObjectSystemData right = ObjectSystemData.get(rightid)
            RelationType type = RelationType.findByName(name)
            Relation relation = Relation.findByTypeAndLeftOSDAndRightOSD(type, left, right)
            if (!relation) {
                relation = new Relation(type, left, right, metadata)
                new Validator(user).validateAddRelation(relation)
                relation.save()
                // update because Indexers may index relations.
                LocalRepository.addIndexable(left, IndexAction.UPDATE)
                LocalRepository.addIndexable(right, IndexAction.UPDATE)
                /*
                 * Update relations, because it is possible that some
                 * other process has changed the OSDs while the user was busy
                 * selecting the new relation type or that the client
                 * has used the wrong versions.
                 *
                 * Note: this is probably obsolete as we do not use LatestHeadResolver any longer.
                 */
//                relationService.updateRelations(left);
//                relationService.updateRelations(right);
            }
            def doc = DocumentHelper.createDocument()
            def root = doc.addElement('relations')
            relation.toXmlElement(root, true)
            log.debug("Created relation as:" + doc.asXML());
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("Failed to create relation.", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * Delete a relation
     *
     * @param id the relation's id
     * @return XML-Response
     *         {@code
     *         <success>success.delete.relation</success>
     *         }
     */
    @Secured(["isAuthenticated()"])
    def deleteXml(Long id) {
        try {
            Relation relation = Relation.get(id)
            if (! relation){
                throw new CinnamonException('error.object.not.found')
            }
            def user = userService.user
            new Validator(user).validateDeleteRelation(relation)
            ObjectSystemData left = relation.leftOSD
            ObjectSystemData right = relation.rightOSD
            relation.delete()
            // update because Indexers may index relations.
            LocalRepository.addIndexable(left, IndexAction.UPDATE)
            LocalRepository.addIndexable(right, IndexAction.UPDATE)
            
            render(contentType: 'application/xml') {
                success('success.delete.relation')
            }
        }
        catch (Exception e) {
            log.debug("Failed to delete relation #$id.", e)
            renderExceptionXml(e.message)
            
        }
    }
}
