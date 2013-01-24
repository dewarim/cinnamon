package cinnamon

import grails.plugins.springsecurity.Secured
import cinnamon.global.Constants

@Secured(["hasRole('_superusers')"])
class GroupController extends BaseController {

    /**
     * add a user to a group
     */
    def addUser() {
        // -get selected user and group
        // -add user to group via CmnGroupUser
        // -update view
        def user = UserAccount.get(params?.user_list)
        def group = CmnGroup.get(params.id)
        new CmnGroupUser(group, user).save()

        redirect(controller: 'userAccount', action: 'showUsersByGroup', params: [id: group.id])
    }

    /**
     * Remove a user from a group
     */
    def removeUser() {
        // -get selected user and group
        // -remove user from group via CmnGroupUser
        // -update view
        def user = UserAccount.get(params?.id)
        def group = CmnGroup.get(params.groupId)
        def gu = CmnGroupUser.findByUserAccountAndCmnGroup(user, group)
        gu.delete()

        redirect(controller: 'userAccount', action: 'showUsersByGroup', params: [id: group.id])
    }

    def edit() {
        def group = CmnGroup.get(params.id)
        def parentList = CmnGroup.list()
        [group: group,
                parentList: parentList.each { it.id != group.id },
        ]
    }

    def showGroupsByUser() {
        def user = UserAccount.get(params.id)
        def CmnGroupUsers = CmnGroupUser.findAllByUserAccount(user)

        // nur die User zum Hinzufügen anbieten, die nicht schon in der Liste sind
        // this doesn't work, although it does in UserController.showUsersByGroup. Dunno why
        def addList = CmnGroup.list().findAll { group ->
            !group.find { CmnGroupUser cmnGroupUser -> cmnGroupUser.cmnGroup == group }
        }.findAll {
            !it.name.startsWith("_") && it.name != Constants.GROUP_SUPERUSERS
        }

        [groupList: CmnGroupUsers.collect {it.cmnGroup},
                addList: addList,
                user: user]
    }

    def showSubGroups() {
        def ancestorGroup = CmnGroup.get(params.id)
        Set<CmnGroup> seen = new HashSet<CmnGroup>()

        def recursion = {}
        def fetchSubGroups = {  group ->
            seen.add(group)
            def childGroups =  CmnGroup.findAllWhere(parent: group)
            childGroups.each {childGroup ->
                if (!seen.contains(childGroup)) {
                    recursion(childGroup) // cannot directly reference closure in itself.
                }
            }
        }
        recursion = fetchSubGroups
        fetchSubGroups(ancestorGroup)
        seen.remove(ancestorGroup)

        def groupList = seen.asList()
        if (params.sort && (params.sort =~ /id|name|description/)) {
            def sorter = { a, b ->
                if (params.order?.equals('asc')) {
                    return a."$params.sort" <=> b."$params.sort"
                }
                else {
                    return b."$params.sort" <=> a."$params.sort"
                }
            }
            groupList = groupList.sort(sorter)
        }
        return [
                group: ancestorGroup,
                groupList: groupList
        ]
    }

    /**
     * add an ACL to a group
     */
    def addAcl() {
        // -get selected acl and group
        // -add acl to group via aclentry
        // -update view
        def acl = Acl.get(params?.acl_list)
        def group = CmnGroup.get(params.id)
        new AclEntry(acl: acl, group: group).save()

        redirect(controller: 'acl', action: 'showAclsByGroup', params: [id: group.id])
    }

    /**
     * Remove an ACL from a group
     */
    def removeAcl() {
        // -get selected acl and group
        // -remove acl from group via aclentry
        // -update view
        def acl = Acl.get(params?.id)
        def group = CmnGroup.get(params.groupId)
        def ae = AclEntry.findByAclAndGroup(acl, group)
        ae.delete()

        redirect(controller: 'acl', action: 'showAclsByGroup', params: [id: group.id])
    }

    def list() {
        setListParams()
        def sortBy = ['id': ' g.id ',
                'name': ' g.name ',
                'description': ' g.description ',
        ]
        def searchString = "from CmnGroup as g where g.groupOfOne=false"
        if (params.sort && sortBy.get(params.sort)) {
            searchString = searchString + " order by " + sortBy.get(params.sort) + (params.order?.equals('asc') ? ' asc ' : ' desc ')
        }
        def groupList =  CmnGroup.findAll(searchString, [], [max: params.max, offset: params.offset])
        def groupCount =  CmnGroup.findAll(searchString)
        log.debug("groupList.size: ${groupCount.size()}")
        [groupList: groupList, groupCount: groupCount.size()]
    }

    def showDescendantGroupUsers() {
        def ancestorGroup = CmnGroup.get(params.id)

        Set<CmnGroup> seen = new HashSet<CmnGroup>()
        Set <UserAccount> users = new HashSet <UserAccount>()

        def recursion = {}
        def fetchDeepUsers = {  group ->
            def gus = CmnGroupUser.findAll("from CmnGroupUser as gu where gu.cmnGroup=:group", [group: group])
            users.addAll(gus.collect {it.userAccount})
            seen.add(group)
            def childGroups =  CmnGroup.findAllWhere(parent: group)
            childGroups.each {childGroup ->
                if (!seen.contains(childGroup)) {
                    recursion(childGroup) // cannot directly reference closure in itself
                }

            }
        }
        recursion = fetchDeepUsers
        fetchDeepUsers(ancestorGroup)

        return [
                ancestorGroup: ancestorGroup,
                users: users.asList()
        ]

    }

    def show() {
        def group = CmnGroup.get(params.id)
        def hasChildren = false
        if (CmnGroup.findAllWhere(parent: group)?.size() > 0) {
            hasChildren = true
        }
        [group: group,
                hasChildren: hasChildren]
    }

    def create() {}

    def update() {
        CmnGroup group = CmnGroup.get(params.id)
        // TODO: check for group == null
        group.properties = params
        if (group.save(flush: true)) {
            flash.message = message(code: "group.update.success")
            redirect(action: 'show', params: [id: group.id])
        }
        else {
            flash.message = message(code: "group.update.fail", args: [group.errors])
            redirect(action: 'edit', params: [id: group.id])
        }
    }

    def save() {
        try {
            def group = new CmnGroup(params)
            // is_user is not set by UI, so we
            // set it here to prevent a ConstraintViolation:
            group.groupOfOne = false
            group.save(flush: true)
            redirect(action: 'show', params: [id: group.id])
        }
        catch (Exception e) {
            flash.message = e.getLocalizedMessage()
            redirect(action: 'create')
        }

    }

    def delete () {
        def group = CmnGroup.get(params.id)
        if (CmnGroupUser.findByCmnGroup(group) || AclEntry.findByGroup(group)) {
            flash.error = message(code: 'error.delete.group')
            redirect(action: 'show', params: [id: params.id])
            return
        } else {
            flash.message = message(code: 'group.delete.success', args: [group.name])
            group.delete()
        }
        redirect(action: 'list')
    }

    def updateList () {
        setListParams()
        render(template: 'groupList', model: [groupList: CmnGroup.list(params)])
    }

}
