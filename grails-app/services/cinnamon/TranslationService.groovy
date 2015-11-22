package cinnamon

import cinnamon.exceptions.CinnamonException
import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import cinnamon.translation.TranslationResult
import cinnamon.utils.ParamParser
import org.dom4j.Document
import org.dom4j.Element

// TODO: refactor code to be Groovier.

class TranslationService {

    def osdService
    def userService
    def relationService

    TranslationResult createTranslation(String attribute, String attribute_value, Long source_id,
                           Long object_relation_type_id, Long root_relation_Type_id, Long target_folder_id){
        
        
        TranslationResult translationResult = new TranslationResult()
        
        ObjectSystemData source = getSource(source_id);
        RelationType objectRelationType = getObjectRelationType(object_relation_type_id);
        RelationType rootRelationType = getRootRelationType(root_relation_Type_id);

        Folder targetFolder;
        if (target_folder_id != null) {
            targetFolder = Folder.get(target_folder_id);
            if (targetFolder == null) {
                throw new CinnamonException("error.target_folder.not_found");
            }
        } else {
            targetFolder = source.getParent();
        }

        // initialize objectTreeCopier:
        def user = userService.user
        def objectTreeCopier = new ObjectTreeCopier(user, targetFolder);
        setAclForTranslations(objectTreeCopier);
        Set<ObjectSystemData> newObjects = new HashSet<ObjectSystemData>();

        // 1. check if the target object tree already exists.
        ObjectSystemData objectTreeRoot = checkRootRelation(source, rootRelationType,
                attribute, attribute_value);

        String metaNode = String.format("<meta><metaset type='translation_extension'><target><relation_type_id>%d</relation_type_id>"
                + "<attribute_value>%s</attribute_value></target></metaset></meta>",
                objectRelationType.getId(), attribute_value);

        if (objectTreeRoot == null) {
            log.debug("no existing targetObjectTree was found - will grow one.");
            objectTreeRoot = growObjectTree(source, rootRelationType, metaNode, targetFolder, newObjects);
        } else {
            log.debug("Found existing targetObjectTree.");
        }
        log.debug("targetObjectTree (now) exists as: " + objectTreeRoot.getId());

        // 2. Tree already exists, but also the required version?
        // => check version
        ObjectSystemData targetNode = getNodeFromObjectTree(objectTreeRoot, source);
        if (targetNode == null) {
            log.debug("target node does not exist - will create it.");
            targetNode = growTargetNode(objectTreeRoot, source, metaNode, newObjects);
        }
        /*
        *  3. now we got a node, but is it new or do we need to fill it
        *  with to-be-translated-content?
        *  => check for object_relation_type-Relation
        */
        if (Relation.findAllByLeftOSDAndRightOSDAndType(source, targetNode, objectRelationType).size() == 0) {
            log.debug("copy content and metadata for object " + targetNode.getName() + " v" + targetNode.getVersion());
            copyContentAndMetadata(source, targetNode);
            log.debug("Source "+source.getId()+" has metadata: "+source.getMetadata());
            log.debug("Target "+targetNode.getId()+" has metadata: "+targetNode.getMetadata());
            osdService.copyRelations(source, targetNode);
            addTranslationMetadata(targetNode, objectRelationType, attribute_value);
            relationService.findOrCreateRelation(objectRelationType, source, targetNode, "");

            List<ObjectSystemData> children = ObjectSystemData.findAllByPredecessor(targetNode);
            targetNode.fixLatestHeadAndBranch(children);

            log.debug("fixLatestHeadAndBranch:" +
                    String.format("target: %d is latestHead %s / branch: %s", targetNode.getId(),
                            targetNode.getLatestHead().toString(),
                            targetNode.getLatestBranch().toString()));
            ObjectSystemData predecessor = targetNode.getPredecessor();
            if (predecessor != null) {
                log.debug("fixLatestHeadAndBranch:" +
                        String.format("predecessor: %d is latestHead: %s / branch: %s", predecessor.getId(),
                                predecessor.getLatestHead().toString(),
                                predecessor.getLatestBranch().toString()));
            }
        } else {
            throw new CinnamonException("error.translation_exists");
        }
        translationResult.targetNode = targetNode
        translationResult.newObjects = newObjects
        return translationResult
    }
    
    void setAclForTranslations(ObjectTreeCopier otc) {
        try {
            def configEntry = ConfigEntry.findByName("translation.config");
            if (configEntry == null) {
                log.debug("Could not find configEntry 'translation.config'.");

            } else {
                org.dom4j.Node node = configEntry.parseConfig().selectSingleNode("aclForTranslatedObjects");
                if (node != null) {
                    String aclName = node.getText();
                    Acl acl = Acl.findByName(aclName);
                    otc.setAclForCopies(acl);
                    log.debug("setAclForCopies: " + aclName);
                } else {
                    log.debug("node for aclForTranslatedObjects is null");
                }
            }
        } catch (Exception e) {
            log.debug("Failed to setAclForCopies (will use default):", e);
            otc.setAclForCopies(null);
        }
    }

    /**
     * Check all OSDs linked by relations to the source object, if one of them
     * fulfills the conditions defined in the parameters <em>attribute</em> and
     * <em>attribute_value</em>.
     *
     * @param source           the source object
     * @param rootRelationType the relation type by which a translation node may be linked to the source object.
     * @param attribute        the attribute which is used to select the discriminating attribute value
     * @param attribute_value  the value by which the client differentiates the translated objects. For example,
     *                         if your source document is of language en-US and the target object is de-DE, you should set
     *                         the attribute value to de-DE.
     * @return the target object tree or null.
     */
    ObjectSystemData checkRootRelation(ObjectSystemData source,
                                       RelationType rootRelationType, String attribute, String attribute_value) {

        List<Relation> relationList = Relation.findAllByLeftOSDAndType(source.getRoot(), rootRelationType);

        // 1. check root relations for attribute and attribute_value
        ObjectSystemData objectTreeRoot = null;
        for (Relation rootRelation : relationList) {
            log.debug("testing relation: " + rootRelation.getId());
            objectTreeRoot = checkRelation(rootRelation, attribute, attribute_value);
            if (objectTreeRoot != null) {
                log.debug("Found root relation");
                break;
            }
        }
        return objectTreeRoot;
    }

    /**
     * Copy the content and metadata of the source to the target object.
     *
     * @param source the source OSD whose content needs translation
     * @param target the recipient of the copied content and metadata.
     */
    void copyContentAndMetadata(ObjectSystemData source, ObjectSystemData target) {
        osdService.copyContent(source, target);
        // TODO: copyMetasets instead of storeMetadata
        target.storeMetadata(source.metadata);
    }

    /**
     * Add the translation-metadata node to target object. If the node exists,
     * this just adds the new "target"-node.
     *
     * @param target             the source language object
     * @param objectRelationType the RelationType for this type
     * @param attributeValue     the value by which the client differentiates the translated objects. For example,
     *                           if your source document is of language en-US and the target object is de-DE, you should set
     *                           the attribute value to de-DE.
     */
    void addTranslationMetadata(ObjectSystemData target,
                                RelationType objectRelationType, String attributeValue) {
        Document meta = ParamParser.parseXmlToDocument(target.getMetadata(), "error.invalid.metadata");
        org.dom4j.Node translationNode = meta.selectSingleNode("/meta/metaset[@type='translation_extension']");
        Element translation;

        if (translationNode == null) {
            log.debug("no translation node exists - we create one.");
            translation = meta.getRootElement().addElement("metaset");
            translation.addAttribute("type", "translation_extension");
        } else {
            translation = (Element) translationNode;
        }
        Element targetNode = translation.addElement("target");
        targetNode.addElement("relation_type_id").addText(String.valueOf(objectRelationType.getId()));
        targetNode.addElement("attribute_value").addText(attributeValue);
        log.debug("about to set metadata:" + meta.asXML());
        target.storeMetadata(meta.asXML());
    }


    /**
     * Find the target node corresponding to the source. For example, if the client needs
     * a copy of version 4, this code looks if the target object tree already has a translated
     * object with this version. If the object exists, it will be returned. Otherwise, the
     * result is null.
     *
     * @param treeRoot the tree on which the target leaf node may exist.
     * @param source   the source object from which we take the version number that we need
     *                 to determine if there is an already translated leaf node.
     * @return the target OSD node from the object tree, or null if the node was not found
     */
    ObjectSystemData getNodeFromObjectTree(ObjectSystemData treeRoot, ObjectSystemData source) {
        ObjectSystemData treeNode = ObjectSystemData.findByRootAndCmnVersion(treeRoot, source.cmnVersion);
        log.debug(String.format("Result of trying to fetch version %s from tree with rootId %d: %s",
                source.cmnVersion, treeRoot.getId(), treeNode));
        return treeNode;
    }

    /**
     * Create all missing leaves on target object tree and return the requested node.
     *
     * @param treeRoot   the root of the target object tree
     * @param source     the source object which will be copied
     * @param newObjects set in which any new objects will be stored.
     * @param metaNode   the metadata which will be added to all translation objects on this tree.
     * @return the OSD with the same version as the source object
     */
    ObjectSystemData growTargetNode(ObjectSystemData treeRoot, ObjectSystemData source, String metaNode,
                                    Set<ObjectSystemData> newObjects) {
        List<ObjectSystemData> allVersions = ObjectSystemData.findAllByRoot(source);
        // find and store existing copies:
        ObjectTreeCopier otc = new ObjectTreeCopier()
        Map<ObjectSystemData, ObjectSystemData> emptyCopies = otc.getCopyCache();
        for (ObjectSystemData osd : allVersions) {
            ObjectSystemData newLeaf = ObjectSystemData.findByRootAndCmnVersion(treeRoot, osd.cmnVersion);
            log.debug(String.format("OSD: %d / newLeaf: %d",
                    osd.getId(), newLeaf != null ? newLeaf.getId() : null));
            emptyCopies.put(osd, newLeaf);
        }

        // create missing leaves:
        for (ObjectSystemData osd : otc.getCopyCache().keySet()) {
            if (emptyCopies.get(osd) == null) {
                ObjectSystemData leaf = otc.createEmptyCopy(osd);
                log.debug(String.format("EmptyCopy of %d is %d",
                        osd.getId(), leaf != null ? leaf.getId() : null));
                if (leaf == null) {
                    log.warn("An empty leaf node was generated!");
                    throw new CinnamonException("error.translation.internal");
                }
                leaf.storeMetadata(metaNode);
                newObjects.add(leaf);
                emptyCopies.put(osd, leaf);
            }
        }
        return emptyCopies.get(source);
    }

    /**
     * Copy a root object and all of its descendants.
     * Creates a relation between the root of the original and the copy.
     *
     * @param source           the source object
     * @param rootRelationType type of the relation between the root object of the source and the copy.
     * @param metaNode         all copies in the tree get this as metadata.
     * @param targetFolder     the folder in which the copy will be created.
     * @param newObjects       set in which any new objects will be stored.
     * @return the root object of the new objectTree
     */
    ObjectSystemData growObjectTree(ObjectSystemData source,
                                    RelationType rootRelationType,
                                    String metaNode, Folder targetFolder, Set<ObjectSystemData> newObjects) {
        List<ObjectSystemData> allVersions = ObjectSystemData.findAllByRoot(source);
        List<ObjectSystemData> newTree = new ArrayList<ObjectSystemData>();

        // create copies of all versions:
//		clearEmptyCopies();
        def objectTreeCopier = new ObjectTreeCopier(userService.user, targetFolder);
        setAclForTranslations(objectTreeCopier);
        log.debug("create empty copies of all versions");
        for (ObjectSystemData osd : allVersions) {
            log.debug("create empty copy of: " + osd.getId());
            ObjectSystemData emptyCopy = objectTreeCopier.createEmptyCopy(osd);
            log.debug(String.format("Empty copy of %d is %d", osd.getId(), emptyCopy.getId()));
            emptyCopy.storeMetadata(metaNode);
            
            objectTreeCopier.getCopyCache().put(osd, emptyCopy);
            newTree.add(emptyCopy);
            newObjects.add(emptyCopy);
        }
        ObjectSystemData treeRoot = newTree.get(0).getRoot();
        log.debug("treeRoot of objectTree: " + treeRoot.getId());
        log.debug("metadata of treeRoot:" + treeRoot.getMetadata());
        // create root_object_relation:

        log.debug(String.format("create root relation between: %d and %d of type %d",
                source.getRoot().getId(), treeRoot.getId(), rootRelationType.getId()));
        relationService.findOrCreateRelation(rootRelationType, source, treeRoot, '')     
        return treeRoot;
    }


    /**
     * Load the source object for a create/checkTranslation. Throws an Exception if
     * no object can be found.
     *
     * @param cmd Map with parameter "source_id" of an OSD.
     * @return the OSD
     */
    ObjectSystemData getSource(Long sourceId) {
        ObjectSystemData source = ObjectSystemData.get(sourceId);
        if (source == null) {
            throw new CinnamonException("error.object.not.found");
        }
        return source;
    }

    /**
     * Load the RelationType and return it. If no relation type with this
     * id can be found, throw an exception.
     *
     * @param cmd Map with parameter "object_relation_type_id"
     * @return the RelationType for a source-to-translation object relation.
     */
    RelationType getObjectRelationType(Long object_relation_type_id) {
        RelationType objectRelationType = RelationType.get(object_relation_type_id);
        if (objectRelationType == null) {
            throw new CinnamonException("error.param.object_relation_type_id");
        }
        return objectRelationType;
    }

    /**
     * Load the RelationType and return it. If no relation type with this
     * id can be found, throw an exception.
     *
     * @param cmd Map with parameter "root_relation_type_id"
     * @return the RelationType for a root-source-object to root-translation object relation.
     */
    RelationType getRootRelationType(Long root_relation_type_id) {
        RelationType rootRelationType = RelationType.get(root_relation_type_id);
        if (rootRelationType == null) {
            throw new CinnamonException("error.root_relation_type.not_found");
        }
        return rootRelationType;
    }

    /**
     * Given a relation, check if it points to an object which has an XPath element whose value
     * matches the given attribute value.<br/>
     * The XPath expression is tested against the metadata, the system metadata and the content.
     * The three content categories are the same as those of the Lucene index server.
     *
     * @param relation       the Relation to test
     * @param attribute      the attribute on which to test
     * @param attributeValue the required value
     * @return the object linked to by the matching Relation - or null.
     */
    ObjectSystemData checkRelation(Relation relation, String attribute, String attributeValue) {
        ObjectSystemData osd = relation.rightOSD;
        ObjectSystemData objectTreeRoot = null;
        String[] xmlForm = [osd.metadata, osd.getSystemMetadata(true, true), osd.content]
        for (String xml : xmlForm) {
            log.debug("testing: " + attribute + " value: " + attributeValue);
            log.debug("against: " + xml);
            Document doc;
            try{
                doc = ParamParser.parseXmlToDocument(xml, null);
            }
            catch (Exception e){
                log.debug("Failed to parse xml [will ignore non-xml content]: "+e.getMessage());
                continue;
            }
            try {
                org.dom4j.Node node = doc.selectSingleNode(attribute);
                if (node != null && node.getText().equals(attributeValue)) {
                    log.debug("found objectTreeRoot:" + osd.getId());
                    objectTreeRoot = osd;
                    break;
                }
            } catch (Exception e) {
                log.warn(String.format(
                        "Exception occurred during translation checking, testing for attribute %s with value %s against %s.",
                        attribute, attributeValue, xml), e);
            }
        }
        return objectTreeRoot;
    }
}
