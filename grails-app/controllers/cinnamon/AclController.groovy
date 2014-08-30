package cinnamon

import cinnamon.exceptions.CinnamonException
import org.dom4j.Element
import org.dom4j.DocumentHelper
import grails.plugin.springsecurity.annotation.Secured
import cinnamon.global.Constants

@Secured(["isAuthenticated()"])
class AclController extends BaseController {

    def listXml(Long id) {
        List<Acl> results = new ArrayList<Acl>();
        if (id) {
            results.add(Acl.get(id))
        }
        else {
            results = Acl.list();
        }
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("acls");
        for (Acl acl : results) {
            acl.toXmlElement(root);
        }
        log.debug("acls: ${doc.asXML()}")
        render(contentType: 'application/xml', text: doc.asXML())
    }

    // --------------------------------------------------------------
    // Dandelion code:
    def showAclsByGroup() {

        def group = CmnGroup.get(params.id)
        def aclEntries = AclEntry.findAllByGroup(group)

        // only add those ACLs which are not already in the list.
        def addList = Acl.list().findAll { acl ->
            !aclEntries.find { aclEntry -> aclEntry.acl == acl }
        }

        [aclList: aclEntries.collect { Acl.get(it.acl.id) },
                addList: addList,
                group: group]
    }

    def index() {
        return redirect(action: 'list', controller: 'acl')
    }

    @Secured(["hasRole('_superusers')"])
    def create() {

    }

    def list() {
        setListParams()
        [aclList: Acl.list(params)]
    }

    def show() {
        [acl: Acl.get(params.id)]
    }

    @Secured(["hasRole('_superusers')"])
    def edit() {
        def acl = Acl.get(params.id)
        if (!acl) {
            flash.message = message(code: 'error.invalid.object')
            redirect(action: 'list')
        }
        else {
            [acl: acl,
                    freeGroups: aclEntryService.fetchFreeGroups(acl)]
        }
    }

    @Secured(["hasRole('_superusers')"])
    def save(String name) {
        try {
            Acl acl = new Acl(name: name)
            acl.save(flush: true)
            redirect(action: 'show', params: [id: acl.id.toString()])
        }
        catch (Exception e) {
            log.debug("save acl failed: ", e)
            flash.message = e.getLocalizedMessage()
            redirect(action: 'create')
        }
    }

    @Secured(["hasRole('_superusers')"])
    def update(String name, Long id) {
        Acl acl = Acl.get(id)
        if (!acl) {
            flash.message = message(code: 'error.object.not.found')
        }
        acl.name = name
        if (acl.save(flush: true)) {
            flash.message = message(code: "acl.update.success")
            redirect(action: 'show', params: [id: acl.id])
        }
        else {
            flash.message = message(code: "acl.update.fail", args: [acl.errors])
            redirect(action: 'edit', params: [id: acl.id])
        }
    }

    @Secured(["hasRole('_superusers')"])
    def delete() {
        def acl = Acl.get(params.id)

        if (acl.name == Constants.ACL_DEFAULT) {
            flash.error = message(code: 'error.delete.default.acl')
            redirect(action: 'show', params: [id: params.id])
            return
        }

        // check if the ACL is in use anywhere
        if (AclEntry.findByAcl(acl)) {
            flash.error = message(code: 'error.delete.acl.aclentry')
            redirect(action: 'show', params: [id: params.id])
            return
        }

        def defaultAcl = Acl.findByName(Constants.ACL_DEFAULT)
        if (!defaultAcl) {
            render(view: 'defaultAclError')
            return
        }

        // Set OSDs and folders to default ACL
        def osdList = ObjectSystemData.findAll("from ObjectSystemData as osd where osd.acl=?", [acl])
        log.debug("osdList: ${osdList.size()}")
        osdList.each { it.acl = defaultAcl }
        Folder.findAllByAcl(acl).each { it.acl = defaultAcl }

        flash.message = message(code: 'acl.delete.success', args: [acl.name])
        acl.delete()

        redirect(action: 'list')
    }

    @Secured(["hasRole('_superusers')"])
    def addAclEntry() {
        try {
            def acl = Acl.get(params.id)
            def group = CmnGroup.get(params.groupId)
            if (!acl || !group) {
                throw new RuntimeException('error.invalid.object')
            }
            if (acl.aclEntries.find { it.group == group }) {
                throw new RuntimeException('acl.group.has_entry')
            }
            AclEntry ae = new AclEntry(acl, group);
            ae.save()
            render(template: 'aclEntryManagement', model: [acl: acl, freeGroups: aclEntryService.fetchFreeGroups(acl)])
        }
        catch (Exception e) {
            render(status: 503, text: message(code: e.message))
        }
    }

    @Secured(["hasRole('_superusers')"])
    def removeAclEntry() {
        try {
            Acl acl = Acl.get(params.id)
            AclEntry aclEntry = AclEntry.get(params.aclEntryId)

            if (!acl || !aclEntry) {
                throw new RuntimeException('error.invalid.object')
            }
            aclEntryService.fullDelete(aclEntry)

            render(template: 'aclEntryManagement', model: [acl: acl, freeGroups: aclEntryService.fetchFreeGroups(acl)])
        }
        catch (Exception e) {
            log.debug("Failed to removeAclEntry", e)
            render(status: 503, text: message(code: e.message))
        }
    }

    def updateList() {
        setListParams()
        render(template: 'aclList', model: [aclList: Acl.list(params)])
    }

    //--------------------- Cinnamon XML API -------------------------

    /**
     * Retrieve a list of all ACLs an user is a member of.
     *
     * @param cmd HTTP request parameter map
     *            <ul>
     *            <li>[id] = Id of a User (long)</li>
     *            </ul>
     * @return XML-Response
     */
    def getUsersAcls(Long id) {
        try {
            def user = UserAccount.get(id)
            if (!user) {
                throw new CinnamonException('error.object.not.found')
            }
            def groups = new HashSet<CmnGroup>()
            for (CmnGroupUser cu : user.getGroupUsers()) {
                groups.add(cu.cmnGroup)
                groups.addAll(cu.cmnGroup.findAncestors())
            }
            log.debug("number of groups for this user: " + groups.size())
            Set<Acl> acls = new HashSet<Acl>();
            for (CmnGroup group : groups) {
                /*
                 * If there are many groups whose AclEntries point to the
                 * same Acls, it could be better to first collect the
                 * entries before adding their Acls.
                 * (or get acls / entries by a HQL-Query)
                 */
                for (AclEntry ae : group.getAclEntries()) {
                    acls.add(ae.getAcl());
                }
            }
            log.debug("number of acls for this user: " + acls.size());
            def doc = DocumentHelper.createDocument()
            def root = doc.addElement('acls')
            acls.each { acl ->
                acl.toXmlElement(root)
            }
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            renderExceptionXml("getUsersAcl failed", e)
        }
    }

    /**
     * Retrieve a list of all Permissions applicable for a user on
     * a given Acl. For superusers, it returns all permissions, as those
     * are not restricted by ACLs / permissions.
     *
     * @param cmd Map with Key/Value-Pair<br>
     *            <ul>
     *            <li>userId = Id of a User</li>
     *            <li>aclId = Id of an Acl</li>
     *            </ul>
     * @return XML-Response, for format see listPermissions command.
     */
    def getUsersPermissions(Long userId, Long aclId) {
        try {
            def user = UserAccount.get(userId)
            if (!user) {
                throw new CinnamonException('error.object.not.found')
            }
            if (user.verifySuperuserStatus()) {
                forward(controller: 'permission', action: 'listXml')
                return
            }

            def groups = new HashSet<CmnGroup>()
            for (CmnGroupUser cu : user.getGroupUsers()) {
                groups.add(cu.cmnGroup)
                groups.addAll(cu.cmnGroup.findAncestors())
            }
            log.debug("number of groups for this user: " + groups.size())
            Acl acl = Acl.get(aclId)
            Set<Permission> permissions = new HashSet<Permission>();

            Set<Acl> acls = new HashSet<Acl>();
            for (CmnGroup group : groups) {
                /*
                 * If there are many groups whose AclEntries point to the
                 * same Acls, it could be better to first collect the
                 * entries before adding their Acls.
                 * (or get acls / entries by a HQL-Query)
                 */
                for (AclEntry ae : group.getAclEntries()) {
                    log.debug("working on AclEntry for Acl:" + ae.getAcl().getName());
                    if (ae.acl.equals(acl))
                        log.debug("found acl");
                    Set<AclEntryPermission> aepSet = ae.getAePermissions();
                    for (AclEntryPermission aep : aepSet) {
                        permissions.add(aep.getPermission());
                    }
                }
            }
            log.debug("number of permissions for this user: " + permissions.size());
            def doc = DocumentHelper.createDocument()
            def root = doc.addElement('permissions')
            permissions.each { permission ->
                permission.toXmlElement(root)
            }
            render(contentType: 'application/xml', text: doc.asXML())
        }

        catch (Exception e) {
            renderExceptionXml("getUsersAcl failed", e)
        }
    }

}
