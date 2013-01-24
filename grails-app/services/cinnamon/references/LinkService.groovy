package cinnamon.references

import cinnamon.Acl
import cinnamon.Folder
import cinnamon.ObjectSystemData
import cinnamon.UserAccount
import cinnamon.exceptions.CinnamonConfigurationException
import cinnamon.exceptions.CinnamonException
import cinnamon.utils.ParamParser
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("GroovyAssignabilityCheck")
public class LinkService {

    private transient Logger log = LoggerFactory.getLogger(this.getClass());

    public Link createLink(ObjectSystemData osd, Folder parent, Acl acl, UserAccount owner, LinkResolver resolver) {
        def link = Link.findByOsdAndParent(osd, parent)
        if (link == null) {
            link = new Link(LinkType.OBJECT, resolver, owner, parent, null, osd, acl);
            link.save()
        }
        return link
    }

    public Link createLink(Folder folder, Folder parent, Acl acl, UserAccount owner, LinkResolver resolver) {
        def link = Link.findByFolderAndParent(folder, parent)
        if (link == null) {
            link = new Link(LinkType.FOLDER, resolver, owner, parent, folder, null, acl);
            link.save()
        }
        return link
    }

    public Link updateLink(Link link, params) {
        if (params.containsKey("acl_id")) {
            Acl acl = Acl.get(ParamParser.parseLong(params.get("acl_id"), "error.param.acl_id"));
            link.setAcl(acl);
        }
        if (params.containsKey("parent_id")) {
            Folder parent = Folder.get(ParamParser.parseLong(params.get("parent_id"), "error.param.parent_id"));
            link.setParent(parent);
        }
        if (params.containsKey("owner_id")) {
            UserAccount owner = UserAccount.get(ParamParser.parseLong(params.get("owner_id"), "error.param.owner_id"));
            link.setOwner(owner);
        }
        if (params.containsKey("resolver")) {
            LinkResolver resolver = LinkResolver.valueOf(params.get("resolver"));
            link.setResolver(resolver);
        }
        if (params.containsKey("object_id") && link.getType() == LinkType.OBJECT){
            ObjectSystemData newOsd = ObjectSystemData.get(params.get("object_id"));
            if(newOsd == null || newOsd.getRoot() != link.getOsd().getRoot()){
                throw new CinnamonException("error.param.object_id");
            }
            if(link.getResolver() == LinkResolver.LATEST_HEAD){
                // we cannot set an object on a link that is dynamically resolved
                // to return the latestHead object.
                throw new CinnamonException("error.cannot.set.latest.head");
            }
            link.setOsd(newOsd);
        }
        return link;
    }

    /**
     * Add a "link" element to the given doc which contains a serialized link.
     * @param link the 
     * @param doc
     * @return
     */
    public Document renderLinkWithinTarget(Link link, Document doc) {
        Element root = doc.addElement("link");
        Element linkParent;

        if (link.getType() == LinkType.FOLDER) {
            link.getFolder().toXmlElement(root);
            linkParent = (Element) root.selectSingleNode("folder");
        }
        else {
            link.getOsd().toXmlElement(root);
            linkParent = (Element) root.selectSingleNode("object");
        }
        addLinkToElement(link, linkParent);
        return doc
    }

    public Collection<Link> findLinksTo(Folder folder) {
        return null;
    }

    public Collection<Link> findLinksTo(ObjectSystemData osd) {
        return null;
    }

    public Collection<Link> findLinksIn(Folder parent, LinkType linkType) {
        Collection<Link> links;
        def link
        switch (linkType) {
            case LinkType.OBJECT:
                links = updateObjectLinks(Link.findAllByParentAndOsdIsNotNull(parent) );
                break;
            case LinkType.FOLDER:
                links = Link.findAllByParentAndFolderIsNotNull(parent)
                break;
            default:
                throw new CinnamonConfigurationException("You tried to query for links of an unknown LinkType.");
        }
        return links;
    }

    Collection<Link> updateObjectLinks(Collection<Link> links) {
        for (Link link : links) {
            if (link.getResolver() == LinkResolver.LATEST_HEAD) {
                ObjectSystemData osd = link.getOsd();
                if (!osd.getLatestHead()) {
                    def latest = ObjectSystemData.findByRootAndLatestHead(osd.getRoot(),true)
                    if (latest == null) {
                        log.error("Could not find exactly one latestHead object for #" + osd.getId());
                    }
                    // update osd to latestHead:
                    link.setOsd(latest);
                }
            }
        }
        return links;
    }

    public void addLinkToElement(Link link, Element element) {
        element.add(Link.asElement("reference", link));
    }

}
