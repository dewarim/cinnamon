package cinnamon

import cinnamon.exceptions.CinnamonException
import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import grails.plugins.springsecurity.Secured
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
            renderException(e)
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
                luceneService.updateIndex(left, repositoryName)
                luceneService.updateIndex(right, repositoryName)
            }
            forward(controller: 'osd', action: 'listRelations', id: osd)
        }
        catch (Exception e) {
            log.debug("failed: relation.create", e)
            renderException(e)
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
                luceneService.updateIndex(leftOsd, repositoryName)
                luceneService.updateIndex(rightOsd, repositoryName)
            }
            render(status: 200, text: '<!-- delete relation: success -->')
        }
        catch (Exception e) {
            log.debug("failed: relation.create", e)
            renderException(e)
        }
    }

    //---------------------------------------------------
    // Cinnamon XML Server API
    @Secured(["isAuthenticated()"])
    def listXml(String name, Long leftId, Long rightId, Boolean includeMetadata) {
        List<Relation> relations

        def criteria = Relation.createCriteria()
        relations = criteria.list {
            if (name) {
                eq('type.name', name)
            }
        }


        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("relations");
        relations.each { relation ->
            relation.toXmlElement(root)
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }

    /**
     * The createrelation command links the objects specified by leftid and rightid
     * with a relation of the type specified by name. If the relation already exists,
     * it will not create a new one but return the existing relation.
     *
     * @param cmd HTTP request parameter map:
     *            <ul>
     *            <li>command=createrelation</li>
     *            <li>name=name of the relation type</li>
     *            <li>leftid=id of "left object"</li>
     *            <li>rightid=id of "right object"</li>
     *            <li>[metadata]= optional metadata in XML format, defaults to {@code <meta/>}</li>
     *            </ul>
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
            ObjectSystemData left = ObjectSystemData.get(leftid)
            ObjectSystemData right = ObjectSystemData.get(rightid)
            RelationType type = RelationType.findByName(name)
            Relation relation = Relation.findByTypeAndLeftOSDAndRightOSD(type, left, right)
            if (!relation) {
                relation = new Relation(type, left, right, metadata)
                relation.save()
                // update because Indexers may index relations.
                luceneService.updateIndex(left, repositoryName)
                luceneService.updateIndex(right, repositoryName)
            }
            /*
             * Update relations, because it is possible that some
             * other process has changed the OSDs while the user was busy
             * selecting the new relation type or that the client
             * has used the wrong versions.
             */
            relationService.updateRelations(left);
            relationService.updateRelations(right);
            def doc = DocumentHelper.createDocument()
            def root = doc.addElement('relations')
            relation.toXmlElement(root)
            log.debug("Created relation as:" + doc.asXML());
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("Failed to create relation.", e)
            renderExceptionXml(e)
        }
    }

    /**
     * Delete a relation
     *
     * @param cmd HTTP request parameter map:
     *            <ul>
     *            <li>command=deleterelation</li>
     *            <li>id=id of relation to delete</li>
     *            </ul>
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
            ObjectSystemData left = relation.leftOSD
            ObjectSystemData right = relation.rightOSD
            relation.delete()
            // update because Indexers may index relations.
            luceneService.updateIndex(left, repositoryName)
            luceneService.updateIndex(right, repositoryName)
            
            render(contentType: 'application/xml') {
                success('success.delete.relation')
            }
        }
        catch (Exception e) {
            log.debug("Failed to delete relation #$id.", e)
            renderExceptionXml(e)
            
        }
    }
}
