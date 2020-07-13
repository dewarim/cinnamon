package cinnamon.references

import cinnamon.Acl
import cinnamon.Folder
import cinnamon.LocalRepository
import cinnamon.ObjectSystemData
import cinnamon.UserAccount
import cinnamon.Validator
import cinnamon.exceptions.CinnamonConfigurationException
import cinnamon.exceptions.CinnamonException
import cinnamon.global.PermissionName
import cinnamon.index.IndexAction
import cinnamon.utils.ParamParser
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("GroovyAssignabilityCheck")
class LinkService {

    private transient Logger log = LoggerFactory.getLogger(this.getClass());

    Link createLink(ObjectSystemData osd, Folder parent, Acl acl, UserAccount owner, LinkResolver resolver) {
        def link = Link.findByOsdAndParent(osd, parent)
        if (link == null) {
            link = new Link(LinkType.OBJECT, resolver, owner, parent, null, osd, acl);
            link.save()
            LocalRepository.addIndexable(osd, IndexAction.UPDATE)
        }
        return link
    }

    Link createLink(Folder folder, Folder parent, Acl acl, UserAccount owner, LinkResolver resolver) {
        def link = Link.findByFolderAndParent(folder, parent)
        if (link == null) {
            link = new Link(LinkType.FOLDER, resolver, owner, parent, folder, null, acl);
            link.save()
            LocalRepository.addIndexable(folder, IndexAction.UPDATE)
        }
        return link
    }

    Link updateLink(Link link, params) {
        if (params.containsKey("acl_id")) {
            Acl acl = Acl.get(ParamParser.parseLong(params.get("acl_id"), "error.param.acl_id"));
            link.acl = acl;
        }
        if (params.containsKey("parent_id")) {
            Folder parent = Folder.get(ParamParser.parseLong(params.get("parent_id"), "error.param.parent_id"));
            link.parent = parent;
        }
        if (params.containsKey("owner_id")) {
            UserAccount owner = UserAccount.get(ParamParser.parseLong(params.get("owner_id"), "error.param.owner_id"));
            link.owner = owner;
        }
        if (params.containsKey("resolver")) {
            LinkResolver resolver = LinkResolver.valueOf(params.get("resolver"));
            link.resolver = resolver;
        }
        if (params.containsKey("object_id") && link.type == LinkType.OBJECT) {
            ObjectSystemData newOsd = ObjectSystemData.get(params.get("object_id"));
            if (newOsd == null || newOsd.root != link.osd.root) {
                throw new CinnamonException("error.param.object_id");
            }
            if (link.resolver == LinkResolver.LATEST_HEAD) {
                // we cannot set an object on a link that is dynamically resolved
                // to return the latestHead object.
                throw new CinnamonException("error.cannot.set.latest.head");
            }
            LocalRepository.addIndexable(link.osd, IndexAction.UPDATE)
            LocalRepository.addIndexable(newOsd, IndexAction.UPDATE)
            link.osd = newOsd;
        }
        return link;
    }

    /**
     * Add a "link" element to the given doc which contains a serialized link.
     * @param link the 
     * @param doc
     * @return
     */
    Document renderLinkWithinTarget(Link link, Document doc, Boolean includeSummary) {
        Element root = doc.addElement("link");
        Element linkParent;

        if (link.type == LinkType.FOLDER) {
            link.folder.toXmlElement(root, includeSummary);
            linkParent = (Element) root.selectSingleNode("folder");
        }
        else {
            link.osd.toXmlElement(root, includeSummary);
            linkParent = (Element) root.selectSingleNode("object");
        }
        addLinkToElement(link, linkParent);
        return doc
    }

    Collection<Link> findLinksTo(Folder folder) {
        return null;
    }

    Collection<Link> findLinksTo(ObjectSystemData osd) {
        return null;
    }

    Collection<Link> findLinksIn(Folder parent, LinkType linkType) {
        Collection<Link> links;
        def link
        switch (linkType) {
            case LinkType.OBJECT:
                links = updateObjectLinks(Link.findAllByParentAndOsdIsNotNull(parent));
//                links = Link.findAllByParentAndOsdIsNotNull(parent);
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
            if (link.resolver == LinkResolver.LATEST_HEAD) {
                ObjectSystemData osd = link.osd;
                if (!osd.latestHead) {
                    def latest = ObjectSystemData.findByRootAndLatestHead(osd.root, true)
                    if (latest == null) {
                        log.error("Could not find exactly one latestHead object for #" + osd.id);
                    }
                    // update osd to latestHead:
                    link.osd = latest;
                }
            }
        }
        return links;
    }

    void addLinkToElement(Link link, Element element) {
        element.add(Link.asElement("reference", link));
    }

    Optional<Link> validateLink(Link link, Validator validator, Boolean withMetadata) {
        try {
            switch (link.type) {
                case LinkType.FOLDER:
                    validator.validatePermissionByName(link.acl, PermissionName.BROWSE_FOLDER)
                    validator.validatePermissionByName(link.folder.acl, PermissionName.BROWSE_FOLDER)
                    break
                case LinkType.OBJECT:
                    validator.validatePermissionByName(link.acl, PermissionName.BROWSE_OBJECT);
                    validator.validatePermissionByName(link.osd.acl, PermissionName.BROWSE_OBJECT);
                    if (withMetadata) {
                        val.validatePermissionByName(link.osd.acl, PermissionName.READ_OBJECT_CUSTOM_METADATA)
                    }
                    break
                default:
                    throw new CinnamonConfigurationException("unknown LinkType "+link.type)
            }
            return Optional.of(link)
        }
        catch (Exception e) {
            log.debug("filter unbrowsable link / linked folder:", e);
            return Optional.empty()
        }

    }

}
