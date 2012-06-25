package cinnamon

import grails.plugins.springsecurity.Secured

@Secured(["hasRole('_superusers')"])
class AclEntryController extends BaseController{

    def index() {
        redirect(action: 'list')
    }

    def list () {
        def showListAll = false
        def entries
        def sortProperty = ''
        def entryCount
        if (params.sort) {
            if (AclEntry.hasProperty(params.sort)) {
                if (params.order?.equals('desc')) {
                    sortProperty = "order by ${params.sort} desc"
                }
                else {
                    sortProperty = "order by ${params.sort} asc"
                }
            }
        }
        if (params.aclId) {
            Acl acl = Acl.get(params.aclId)
            entries = AclEntry.findAll("from AclEntry a where a.acl=:acl $sortProperty", [acl: acl], params)
            entryCount =   AclEntry.findAll("from AclEntry a where a.acl=:acl", [acl: acl]).size()
            flash.message = message(code: "aclEntry.list.onlyAcl", args: [acl.name])
            showListAll = true
        }
        else if (params.groupId) {
            CmnGroup group = CmnGroup.get(params.groupId)
            entries = AclEntry.findAll("from AclEntry a where a.group=:group $sortProperty", [group: group], params)
            entryCount = AclEntry.findAll("from AclEntry a where a.group=:group", [group: group]).size()
            flash.message = message(code: "aclEntry.list.onlyGroup", args: [group.name])
            showListAll = true
        }
        else {
            entries = AclEntry.findAll("from AclEntry a $sortProperty", [:], params)
            entryCount = AclEntry.count()
        }
        return [aclEntries: entries,
                showListAll: showListAll,
                entryCount:entryCount]
    }

    def show () {
        if (!params.id) {
            flash.message = message(code: 'error.object.not.found')
            return redirect(controller: 'aclEntry', action: 'list')
        }
        [aclEntry: AclEntry.get(params.id),
                permissionList: Permission.listOrderByName()]
    }

    def togglePermission () {
        log.debug("params:${params.dump()}")
        Permission perm = Permission.get(params.permissionId)
        AclEntry ae = AclEntry.get(params.id)
        log.debug("ae: ${ae}")
        log.debug("perm: ${perm}")
        if (ae.findPermission(perm)) {
            aclEntryService.removeAclEntryPermission(ae, perm)
        }
        else {
            aclEntryService.addAclEntryPermission(ae, perm)
        }
        redirect(action: 'show', params: [id: params.id])
    }

    def toggleAllPermissions () {
        AclEntry ae = AclEntry.get(params.id)
        def allowAll = params.toggle?.equals('allow') ?: false
        def permList = Permission.list()
        if (allowAll) {
            permList.each {permission ->
                aclEntryService.addAclEntryPermission(ae, permission)
            }
        }
        else {
            permList.each {permission ->
                aclEntryService.removeAclEntryPermission(ae, permission)
            }
        }
        redirect(controller: 'aclEntry', action: 'show', params: [id: params.id])
    }
    
}
