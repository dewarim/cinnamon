package cinnamon.tika

import cinnamon.ConfigEntry
import cinnamon.Metaset
import cinnamon.ObjectSystemData
import cinnamon.utils.ParamParser
import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata
import org.apache.tools.ant.util.StringUtils
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element

import java.nio.file.Path

class CinnamonTikaService {

    def tikaService
    
    void parse(ObjectSystemData osd){
        if(osd == null){
            log.debug("received null osd.");
            return;
        }
        if(osd.format == null || osd.format.extension == null){
            log.debug("object #${osd.id} has no defined format - will be ignored by Tika.");
            return;
        }
        if (! osd.contentSize){
            log.debug("Object #${osd.id} has no content - will skip Tika parsing.")
            return
        }
        String tikaBlacklist = getTikaBlacklist();
        String extension = osd.format.extension.toLowerCase();
        if (extension.matches(tikaBlacklist)){
            log.debug("Object format "+extension+" is not suitable for tika - will be ignored.");
            return;
        }
        def metaset = osd.fetchMetaset('tika', true)
        String xhtml = ""
        try {
            metaset = parseFile(new File(osd.fullContentPath), metaset)
            metaset.save()
        }
        catch (Exception e) {
            log.warn("Failed to extract data with tika.", e);
            def errorDoc = DocumentHelper.createDocument()
            Element tikaMetaset = errorDoc.addElement("metaset");
            tikaMetaset.addAttribute("type","tika");
            tikaMetaset.addElement("error").addText(StringUtils.getStackTrace(e));
            tikaMetaset.addElement("tika-output").addText(xhtml)
            metaset.content = errorDoc.asXML()
        }
    }
    
    Metaset parseFile(File content, Metaset metaset){
        TikaConfig tikaConfig = new TikaConfig()
        Metadata tikaMeta = new Metadata()

        String xhtml = tikaService.parseFile(content, tikaConfig, tikaMeta)
        log.debug("xhtml from tika:\n"+xhtml)
        xhtml = xhtml.replaceAll("xmlns=\"http://www\\.w3\\.org/1999/xhtml\"", "")
        org.dom4j.Node resultNode = ParamParser.parseXml(xhtml, "Failed to parse tika-generated xml")

        Document meta = ParamParser.parseXmlToDocument(metaset.content)
        // remove previous content:
        meta.selectSingleNode("/metaset/html")?.detach()
        Element tikaMetaset = meta.rootElement
        tikaMetaset.addAttribute("type","tika");
        tikaMetaset.add(resultNode)
        tikaMetaset.selectSingleNode('/metaset/@status')?.detach()
        metaset.content = meta.asXML()
        return metaset
    }

    /**
     * You can add a config entry to define formats that should not be parsed by Tika.
     * For example, DITA files are already XML, so you can index / handle them without any
     * further Tika-parsing. (And adding them to the metadata may cause problems due to xml-namespaces)
     * The default blacklist is: "xml|dita|ditamap"
     * @return a String that may be used as a regex to filter for invalid format extensions<br/>
     * Example: may return "dita|xml|foo"
     */
    String getTikaBlacklist(){
        ConfigEntry blacklist = ConfigEntry.findByName("tika.blacklist");
        if(blacklist == null){
            log.debug("Did not find tika.blacklist config entry, returning defaultBlacklist.");
            return defaultBlacklist;
        }
        org.dom4j.Node blackNode = ParamParser.parseXmlToDocument(blacklist.getConfig()).selectSingleNode("//blacklist");
        if(blackNode == null){
            log.debug("Did not find blacklist node in tika.blacklist config entry, returning defaultBlacklist.");
            return defaultBlacklist;
        }
        log.debug("Found blacklist: "+blackNode.getText());
        return blackNode.getText();
    }

    public final String defaultBlacklist = "xml|dita|ditamap";
}
