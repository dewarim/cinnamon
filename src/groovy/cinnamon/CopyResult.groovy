package cinnamon

import cinnamon.exceptions.CinnamonException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the results of a copy operation - list of copied files and folders,
 * along with any errors encountered.  
 */
public class CopyResult {
    
    /* 
     * moved java code here because Grails 2.1.1 cannot not compile this class (missing symbol getId())
     * when using cinnamon module as a plugin:
     * .grails/2.1.1/.../plugins/cinnamon-0.2.2/src/java/cinnamon/CopyResult.java:80: error: cannot find symbol
     *   getNewObjects().add(osd.getId());
     *                          ^
     *   symbol:   method getId()
     * location: variable osd of type ObjectSystemData
     * 
     */
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    Collection<Long> newFolders;
    Collection<Long> newObjects;
    Map<Long, String> folderFailures; // if necessary, refactor to proper ErrorMessage objects.
    Map<Long, String> objectFailures;


    public CopyResult() {
        newFolders = new HashSet<Long>();
        newObjects = new HashSet<Long>();
        folderFailures = new HashMap<Long, String>();
        objectFailures = new HashMap<Long, String>();
    }

    void addCopyResult(CopyResult cr){
        newFolders.addAll(cr.getNewFolders());
        newObjects.addAll(cr.getNewObjects());
        folderFailures.putAll(cr.getFolderFailures());
        objectFailures.putAll(cr.getObjectFailures());
    }

    public CopyResult addFailure(Folder folder, String failureMessage){
//        if(failureMessage == null){throw new RuntimeException("null failure!");}
        log.debug("addFailure:", failureMessage);
        getFolderFailures().put(folder.getId(), failureMessage);
        return this;
    }

    public CopyResult addFailure(Folder folder, CinnamonException exception){
        log.debug("addFailure:", exception);
        getFolderFailures().put(folder.getId(), exception.getMessage());
        return this;
    }

    public CopyResult addFailure(ObjectSystemData osd, CinnamonException exception){
        log.debug("addFailure:", exception);
        getObjectFailures().put(osd.getId(), exception.getMessage());
        return this;
    }

    public CopyResult addObject(ObjectSystemData osd){
        log.debug("addObject:"+osd.getId());
        getNewObjects().add(osd.getId());
        return this;
    }

    public CopyResult addFolder(Folder folder){
        log.debug("addFolder"+folder.getId());
        getNewFolders().add(folder.getId());
        return this;
    }

    public Boolean foundFailure(){
        return ! (getFolderFailures().isEmpty() && getObjectFailures().isEmpty());
    }

    public Document toXml(){
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("copyResult");
        Element folders = root.addElement("folders");
        for(Long id : newFolders){
            folders.addElement("id").addText(id.toString());
        }
        Element objects = root.addElement("objects");
        for(Long id : newObjects){
            objects.addElement("id").addText(id.toString());
        }

        Element errors = root.addElement("errors");

        Element folderErrors = errors.addElement("folders");
        addErrorSet(folderFailures, folderErrors, "folder");

        Element objectErrors = errors.addElement("objects");
        addErrorSet(objectFailures, objectErrors, "object");

        return doc;
    }

    void addErrorSet(Map<Long, String> errors, Element root, String elementName){
        for(Map.Entry<Long, String> entry : errors.entrySet()){
            Element folder = root.addElement(elementName);
            folder.addElement("id").addText(entry.getKey().toString());
            folder.addElement("message").addText(entry.getValue());
        }
    }

    public Integer newObjectCount(){
        return newObjects.size();
    }

    public Integer newFolderCount(){
        return newFolders.size();
    }
}
