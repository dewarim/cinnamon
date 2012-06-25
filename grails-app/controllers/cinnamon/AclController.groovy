package cinnamon

import org.dom4j.Element
import org.dom4j.DocumentHelper
import grails.plugins.springsecurity.Secured
import cinnamon.global.Constants

@Secured(["isAuthenticated()"])
class AclController extends BaseController {

    def listXml(){
        List<Acl> results = new ArrayList<Acl>();
        if (params.id) {
            results.add(Acl.get(params.id))
        } else {
            results = Acl.list();
        }
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("acls");
        for (Acl acl : results) {
            acl.toXmlElement(root);
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }
    
    // --------------------------------------------------------------
    // Dandelion code:
    def showAclsByGroup () {

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

    def index () {
        return redirect(action: 'list', controller: 'acl')
    }

    def create () {

    }

    def list() {
        setListParams()
        [aclList: Acl.list(params)]
    }

    def show () {
        [acl: Acl.get(params.id)]
    }

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

    def save() {
        try {
            // TODO: bind params
            Acl acl = new Acl(params)
            acl.save(flush: true)
            redirect(action: 'show', params: [id: acl.id.toString()])
        }
        catch (Exception e) {
            log.debug("save acl failed: ",e)
            flash.message = e.getLocalizedMessage()
            redirect(action: 'create')
        }
    }

    def update() {
        Acl acl = Acl.get(params.id)
        // TODO: check for acl == null
        acl.properties = params
        if (acl.save(flush: true)) {
            flash.message = message(code: "acl.update.success")
            redirect(action: 'show', params: [id: acl.id])
        }
        else {
            flash.message = message(code: "group.update.fail", args: [acl.errors])
            redirect(action: 'edit', params: [id: acl.id])
        }
    }

    def delete () {
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

    def addAclEntry () {
        try {
            def acl = Acl.get(params.id)
            def group = CmnGroup.get(params.groupId)
            if (!acl || !group) {
                throw new RuntimeException('error.invalid.object')
            }
            if (acl.aclEntries.find {it.group == group}) {
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

    def updateList () {
        setListParams()
        render(template: 'aclList', model: [aclList: Acl.list(params)])
    }
    
}
