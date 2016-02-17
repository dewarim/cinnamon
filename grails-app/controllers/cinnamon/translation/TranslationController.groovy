package cinnamon.translation

import cinnamon.BaseController
import cinnamon.ObjectSystemData
import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang.exception.ExceptionUtils
import org.dom4j.DocumentHelper
import org.dom4j.Element

@Secured(["isAuthenticated()"])
class TranslationController extends BaseController {

    def translationService

    /**
     * createTranslation - create a copy of an object (and the whole object tree,
     * if necessary) to be used in a translation process.
     * <br/>
     * Definition: an object tree in this context means all versions of a
     * Cinnamon object.
     * </br>
     * <h1>Creating a translation object</h1>
     * <h2>Find root relation</h2>
     * The client specifies a root relation type with the parameter <em>root_translation_type_id</em>,
     * to define a necessary relation between the
     * source object tree and a translated object tree.<br/>
     * One source tree may have several translations. Each translation object tree is connected
     * via (for example) a relation of the type "root_translation".
     * createTranslation searches for those "root_translation"-relations between
     * the root object of the source object tree and other object trees.
     * A root relation exists if a "root_translation" with the correct
     * <em>root_relation_type_id</em> is found which contains an <em>attribute</em>
     * (an XPath-expression returning a node)
     * with the <em>attribute_value</em>.
     * <h3>Creating a new root relation</h3>
     * If no root relation of the given type and attribute/value parameters exists,
     * the whole source object tree will be copied (without its content and metadata).
     * Each object created this way gets a metadata-node which describes the translation:
     * <pre>
     * {@code
     * 	<meta>
     *      <metaset type="translation_extension">
     * 			<target>
     * 				<!-- here 3 is the id of a "translation_relation_type" -->
     * 				<relation_type_id>3</relation_type_id>
     * 				<attribute_value>en_US</attribute_value>
     * 			</target>
     *     </metaset>
     * </meta>
     *}
     * </pre>
     * If you index this field via appropriate IndexItems, you can search for all documents
     * which do lack content and thus need to be translated to en_US.
     * <h2>Find target node in translation object tree</h2>
     * After the server has found or created the translation object tree, it
     * checks if there exists a node which has the same version as the source object.
     * (Strictly speaking, if we just did create the object tree, this is guaranteed.)
     * If the target node is missing, it will be created (without content, with
     * translation metadata-node).
     * <h2>Check object_relation_type</h2>
     * If the target node already has a relation to the source object with a
     * relation type of <em>object_relation_type_id</em>, an error message
     * is returned because the object is already in place. Otherwise, the
     * corresponding relation will be created and the target node receives the content
     * and metadata of the source (the translation metadata is retained).
     *
     * @param attribute XPath expression to find a specific attribute node.
     * @param attribute_value The required value of the attribute node.
     * @param source_id the source object which will be translated.
     * @param object_relation_type_id the type of the object relation which will be created
     *            between the source object and the translated object.
     * @param root_relation_type_id = the type of relation by which the existence of a
     *            translated object tree can be identified.
     * @param target_folder_id optional id of the folder where the translation objects will be created.
     *            <em>This will only be observed if a new target object tree is created!</em>
     * @return A Cinnamon Response object, containing
     * new target node's id as an XML node along with a list of all
     * generated objects (see CmdInterpreter.getObjects):
     * <pre>
     * {@code
     *  <createTranslation><translationId>4</translationId>
     *      <objects><object><id>4</...>
     *  </createTranslation>
     *
     *}
     * </pre>
     */
    def createTranslation(String attribute, String attribute_value, Long source_id,
                          Long object_relation_type_id, Long
                                  root_relation_type_id, Long target_folder_id,
                          Boolean include_summary
    ) {
        try {
            TranslationResult translationResult = translationService.createTranslation(
                    attribute, attribute_value, source_id, object_relation_type_id,
                    root_relation_type_id, target_folder_id
            )
            render(contentType: 'application/xml', text: translationResult
                    .toXml(include_summary))

        } catch (Exception e) {
            log.debug("failed to create translation:", e);
            String fullMessage = ExceptionUtils.getFullStackTrace(e);
            renderExceptionXml(fullMessage)
        }
    }

    /**
     * Check if there already exists a translation for the given source
     * object.
     * The request contains the following parameters:
     * <p/>
     * <pre>
     * {@code
     * <translation>
     * 	<!-- if the translation object tree exists: -->
     * 	<tree_root_id>1234</tree_root_id>
     *
     *  <!-- if a translation object of the requested version already exists: -->
     *
     *  <target_object_id translated="true">543</target_object_id>
     *  <!-- note: Attribute 'translated' is true if the object has a relation
     *  	of the object_relation_type, false otherwise -->
     * </translation>
     *
     *}
     * </pre>
     * @param attribute XPath expression to find a specific attribute node.
     * @param attribute_value The required value of the attribute node.
     * @param source_id the source object which will be translated.
     * @param object_relation_type_id the type of the object relation which will be created
     *            between the source object and the translated object.
     * @param root_relation_type_id = the type of relation by which the existence of a
     *            translated object tree can be identified.
     *
     * @return an XML document which may only contain an empty translation node
     *         or more, depending on whether the target translation object already exists and if
     *         it already has an translation relation to the source object.
     */
    def checkTranslation(String attribute, String attribute_value, Long source_id,
                         Long object_relation_type_id, Long root_relation_type_id, Long target_folder_id
    ) {
        try {
            ObjectSystemData source = translationService.getSource(source_id)
            RelationType objectRelationType = translationService.getObjectRelationType(object_relation_type_id)
            RelationType rootRelationType = translationService.getRootRelationType(root_relation_type_id)

            def doc = DocumentHelper.createDocument()
            Element root = doc.addElement("translation");

            // 1. check if the target object tree already exists.
            ObjectSystemData objectTreeRoot = translationService.checkRootRelation(source, rootRelationType, attribute, attribute_value);

            // 2. Tree already exists, but also the required version?
            // => check version
            if (objectTreeRoot != null) {
                root.addElement("tree_root_id").addText(String.valueOf(objectTreeRoot.getId()));
                ObjectSystemData targetNode = translationService.getNodeFromObjectTree(objectTreeRoot, source);

                if (targetNode != null) {
                    log.debug("targetNode found; id: " + targetNode.getId());
                    Element target = root.addElement("target_object_id");
                    target.addText(String.valueOf(targetNode.getId()));

                    // 3. check if target node has already been translated:
                    if (Relation.findAllByLeftOSDAndRightOSDAndType(source, targetNode, objectRelationType).size() == 0) {
                        // targetNode exists and has no translation relation to source object
                        target.addAttribute("translated", "false");
                    }
                    else {
                        // targetNode exists and has a translation relation to source object
                        target.addAttribute("translated", "true");
                    }
                }
                else {
                    log.debug(String.format("targetNode for %d was not found.", source.getId()));
                }
            }
            else {
                log.debug(String.format("No target object tree for %d was found", source.getId()));
            }
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("checkTranslation failed with:",e)
            String fullMessage = ExceptionUtils.getFullStackTrace(e);
            renderExceptionXml(fullMessage)
        }
    }


}
