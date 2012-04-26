package cinnamon

import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import cinnamon.exceptions.CinnamonException
import cinnamon.utils.ParamParser
import javax.persistence.EntityManager
import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import humulus.EnvironmentHolder

class OsdService {

    /**
     * Turn a collection of data objects into an XML document. Any exceptions encountered during
     * serialization are turned into error-Elements which contain the exception's message.
     *
     * @param results
     * @return Document
     */
    Document generateQueryObjectResultDocument(Collection<ObjectSystemData> results) {
        return generateQueryObjectResultDocument(results, false);
    }

    /**
     * Turn a collection of data objects into an XML document. Any exceptions encountered during
     * serialization are turned into error-Elements which contain the exception's message.
     *
     * @param results      the source collection of results to be used to generate the XML document.
     * @param withMetadata if true, include object custom metadata in the output (which can get quite large).
     * @return Document
     */
    Document generateQueryObjectResultDocument(Collection<ObjectSystemData> results,
                                                             Boolean withMetadata) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("objects");

        for (ObjectSystemData osd : results) {
            Long id = osd.getId();
            log.debug("working on object: " + id);
            Element data;
            try {
                data = osd.convert2domElement();
                if (withMetadata) {
                    data.add(ParamParser.parseXml(osd.getMetadata(), null));
                }
                root.add(data);
            } catch (CinnamonException ex) {
                /*
                     * Note: any exceptions encountered here are probably serious bugs,
                     * which could be caused by corrupted data or faulty serialization
                     * routines.
                     * So, let's report them as errors instead of debug messages.
                     */
                log.error("Error serializing object: " + id + " - " + ex.getMessage());
                Element error = DocumentHelper.createElement("error").addText(ex.getLocalizedMessage());
                error.addElement("id").addText(id.toString());
                root.add(error);
            }
        }
        return doc;
    }

    void copyContent(ObjectSystemData source, ObjectSystemData copy){
        copyContent(EnvironmentHolder.getEnvironment().dbName, source,copy)
    }

    void copyContent(String repositoryName, ObjectSystemData source, ObjectSystemData copy) {
        String conPath = source.getContentPath();
        if (conPath != null && conPath.length() > 0) {
            String fullContentPath = source.getFullContentPath(repositoryName);

            log.debug("ContentPath: " + fullContentPath +
                    " and Size is: " + source.getContentSize());
            try {
                String targetPath = ContentStore.copyToContentStore(fullContentPath, repositoryName);
                log.debug("targetPath = " + targetPath);
                if (targetPath.length() > 0) {
                    copy.setContentPath(targetPath, repositoryName);
                }
                copy.setFormat(source.getFormat());
            } catch (IOException ex) {
                throw new CinnamonException(ex);
            }
        }
    }


    public List<ObjectSystemData> findAllVersions(ObjectSystemData osd) {
        if(! osd.getVersion().equals("1")){
            osd = osd.root
        }
        return ObjectSystemData.findAll("from ObjectSystemData o where o.root=:root order by o.id desc",
                [root:osd])
    }

    /**
     * Copy relations of an object if the relationType demands it.
     *
     * @param target for which the new relations will be created.
     */
    public void copyRelations(ObjectSystemData source, ObjectSystemData target) {
        List<Relation> relations =
            Relation.findAll("from Relation r where r.leftOSD=:left or r.rightOSD=:right",
                    [left:source, right:source]);
        for (Relation rel : relations) {
            /*
             * The relation will only be copied if the cloneOn{left,right}Copy flag is set on the
             * {left,right} part of the osd which is copied.
             * Example: html to image relation should (normally) have the clone flag set on the
             * left, but not on the right part of the relation. If the image is copied, the copy
             * will not have a relation to the html file. If the html file is copied, the copy
             * should have a relation to the image.
             */
            RelationType relationType = rel.getType();
            if(relationType.getCloneOnLeftCopy() && rel.getLeftOSD().equals(source)){
                Relation relCopy = Relation.findOrSaveWhere(type: rel.getType(),  leftOSD: target, rightOSD: rel.getRightOSD(), rel.getMetadata());
                log.debug("created new Relation: "+relCopy);
            }
            if(relationType.getCloneOnRightCopy() && rel.getRightOSD().equals(source)){
                Relation relCopy =  Relation.findOrSaveWhere(type:  rel.getType(), leftOSD: rel.getLeftOSD(), rightOSD:target, rel.getMetadata());
                log.debug("created new Relation: "+relCopy);
            }
        }
    }

    void delete(ObjectSystemData osd){
        delete(osd, false, false);
    }

    void delete(ObjectSystemData osd, Boolean killDescendants, Boolean removeLeftRelations){
        log.debug("Found osd");
        ObjectSystemData predecessor = osd.getPredecessor();

        log.debug("checking for descendants ");
        boolean hasDescendants = ObjectSystemData.countByPredecessor(osd) > 0;
        if (killDescendants && hasDescendants) {
            def preds = ObjectSystemData.findAll("from ObjectSystemData o where o.predecessor=:pred order by id desc",[pred:osd])
            preds.each{pre ->
                delete(pre, killDescendants, removeLeftRelations);
            }
        }
        else if (hasDescendants){
            throw new CinnamonException("error.delete.has_descendants");
        }


        // check for protected relations
        List<Relation> relations = Relation.findAllByLeftOSDOrRightOSD(osd,osd);
        for(Relation rel : relations){
            RelationType rt = rel.getType();
            /*
             * if an object is protected by the relation type, it
             * must not be deleted.
             */
            if( ( rt.rightobjectprotected && rel.getRightOSD().equals(osd)) ||
                    ( rt.leftobjectprotected && rel.getLeftOSD().equals(osd) && ! removeLeftRelations)
            ){
                throw new CinnamonException("error.protected_relations");
            }
        }

        // delete relations
        for (Relation rel : relations) {
            rel.delete()
        }

        log.debug("object deleted.");

        /*
           * An object is latestBranch, if it has no descendants.
           * You can only delete an object without descendants.
           * So, the predecessor's only child has been deleted and this
           * makes the predecessor latestBranch.
           *
           * An object is latestHead, if it is not of part of a branch and has no
           * descendants. As we already said, this predecessor cannot have any
           * descendants and so we can set latestHead to true if it is part of
           * the main branch (no . in version).
           */
        if(predecessor != null){
            predecessor.setLatestBranch(true);
            if(! predecessor.getVersion().contains(".")){
                predecessor.setLatestHead(true);
            }
        }

        ContentStore.deleteObjectFile(osd);
        osd.delete(flush: true)
    }

    public void delete(Long id) {
        log.debug("before loading osd");
        ObjectSystemData osd = get(id);
        if(osd == null) {
            throw new CinnamonException("error.object.not.found");
        }
        delete(osd);
    }

}
