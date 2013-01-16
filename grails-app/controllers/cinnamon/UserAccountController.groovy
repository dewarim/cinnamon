package cinnamon

import org.dom4j.Element
import org.dom4j.Document
import org.dom4j.DocumentHelper
import grails.plugins.springsecurity.Secured
import cinnamon.global.Constants
import cinnamon.i18n.UiLanguage

@Secured(["hasRole('_users')"])
class UserAccountController extends BaseController {

    def showUsersByGroup() {
        def group = CmnGroup.get(params.id)
        def groupUsers = CmnGroupUser.findAllByCmnGroup(group)

        // only show those users who are not already part of the list:
        def addList = UserAccount.list().findAll { user ->
            !groupUsers.find { groupUser -> groupUser.userAccount == user }
        }

        def hasSubGroups = false
        if (CmnGroup.findAllWhere(parent: group).size() > 0) {
            hasSubGroups = true
            log.debug("group has subgroups")
        } else {
            log.debug("group has no subgroups")
        }

        [userList: groupUsers.collect { it.userAccount },
                addList: addList,
                hasSubGroups: hasSubGroups,
                group: group]
    }

    /**
     * Replace a user in the repositories.
     * Present a form with source and target user.
     * All objects which belong to the source user in some way will be transferred.
     */
    def replaceUser() {
        [userList: UserAccount.list(),
                forbidden: !userService.transferAssetsAllowed(session.repositoryName)
        ]
    }

    def transferAssets() {
        try {
            if (!userService.transferAssetsAllowed(session.repositoryName)) {
                throw new RuntimeException(message(code: 'user.replaceUser.forbidden'))
            }
            if (params.sourceId.equals(params.targetId)) {
                throw new RuntimeException(message(code: 'user.replaceUser.targets.equal'))
            }

            UserAccount source = UserAccount.get(params.sourceId)
            if (!source) {
                throw new RuntimeException(message(code: 'user.replaceUser.source.not_found'))
            }

            UserAccount target = UserAccount.get(params.targetId)
            if (!target) {
                throw new RuntimeException(message(code: 'user.replaceUser.target.not_found'))
            }

            // check that the source user is not an admin
            userService.transferAssets source, target
            flash.message = message(code: 'user.replaceUser.success', args: [source.name, target.name])
        }
        catch (RuntimeException e) {
            flash.message = message(code: 'user.replaceUser.failed', args: [message(code: e.getMessage())])
            return redirect(controller: 'userAccount', action: 'replaceUser')
        }
        return redirect(controller: 'userAccount', action: 'replaceUser')
    }

    def create() {

    }

    def list() {
        setListParams()
        [userList: UserAccount.list(params)]
    }

    def show() {
        [user: UserAccount.get(params.id)]
    }

    def edit() {
        [user: UserAccount.get(params.id)]
    }

    def update() {
        log.debug(params.dump())
        UserAccount user = UserAccount.get(params.id)
        if (!user) {
            flash.message = message(code: 'user.not.found')
            redirect(controller: 'userAccount', action: 'list')
            return
        }

        // TODO: validate username (for example, prevent special chars and trailing whitespace)
        if (params.name?.length() == 0) {
            // do not allow empty username.			
            params.name = user.name
        }

        // HTML form checkboxes: if checked, browser sends sudoer=true, if unchecked, browser sends nothing.
        def checkFields = ['sudoable', 'sudoer', 'activated']
        checkFields.each {field ->
            user."$field" = params.containsKey(field)
        }

        if (user.name.equals('admin')) {
            if (!params.activated?.equals('true')) {
                log.debug("params.activated: ${params.activated}")
                log.debug('Preventing user from deactivating admin account.')
                flash.message = message(code: 'user.update.fail.deactivate')
                redirect(action: 'edit', params: [id: user.id])
                return
            }
            else if (!params.name?.equals(user.name)) {
                log.debug('Preventing user from changing admin\'s name.')
                flash.message = message(code: 'user.update.fail.rename')
                redirect(action: 'edit', params: [id: user.id])
                return
            }
        }

        // if the name was changed, also change the user's personal group name
        if (!user.name.equals(params.name)) {
            CmnGroup group = CmnGroup.findByName("_${user.id}_${user.name}")
            group.name = "_${user.id}_${params.name}"
            group.description = "${params.name}'s personal group"
            group.save()
        }

        bindData(user, params, [include: ['name', 'fullname', 'description', 'email']])

        user.language = UiLanguage.get(params.'language.id')
        if (params.pwd) {
            // set separately to prevent an empty/null pwd from being set.
            user.pwd = params.pwd
        }
        if (user.save(flush: true)) {
            flash.message = message(code: "user.update.success")
            redirect(action: 'show', params: [id: user.id])
        }
        else {
            flash.message = message(code: "user.update.fail", args: [user.errors])
            redirect(action: 'edit', params: [id: user.id])
        }
    }

    def deleteAsk() {
        [userList: UserAccount.list(),
                forbidden: !userService.deleteUserAllowed(session.repositoryName),
                showTransferLink: params.showTransferLink
        ]
    }

    def doDelete() {
        try {
            if (!userService.deleteUserAllowed(session.repositoryName)) {
                throw new RuntimeException(message(code: 'user.delete.forbidden'))
            }
            UserAccount user = UserAccount.get(params.user)
            if (!user) {
                throw new RuntimeException(message(code: 'user.delete.not_found'))
            }
            if (user.equals(userService.getUser())) {
                throw new RuntimeException(message(code: 'user.delete.yourself'))
            }
            if (userService.userHasAssets(user)) {
                throw new RuntimeException(message(code: 'user.has.dependencies'))
            }
            // check that the source user is not an admin
            userService.deleteUserAllowed(session.repositoryName)
            flash.message = message(code: 'user.delete.success', args: [user.name.encodeAsHTML()])
        }
        catch (RuntimeException e) {
//            log.debug("failed to delete user: ",e)
            flash.message = message(code: 'user.delete.failed', args: [message(code: e.getMessage())])
            return redirect(controller: 'userAccount', action: 'deleteAsk', params: [showTransferLink: true])
        }
        return redirect(controller: 'userAccount', action: 'deleteAsk')
    }

    /**
     * add a group to a user
     */
    def addGroup() {
        // -get selected user and group
        // -add group to user via groupuser
        // -update view
        def group = CmnGroup.get(params?.group_list)
        def user = UserAccount.get(params.userId)
        new CmnGroupUser(user, group).save()

        redirect(controller: 'group', action: 'showGroupsByUser', params: [id: user.id])
    }

    /**
     * Remove a group from a user
     */
    def removeGroup() {
        // -get selected user and group
        // -remove group from user via groupuser
        // -update view
        def group = CmnGroup.get(params?.id)
        def user = UserAccount.get(params.userId)
        def gu = CmnGroupUser.findByUserAccountAndCmnGroup(user, group)
        gu.delete()

        redirect(controller: 'group', action: 'showGroupsByUser', params: [id: user.id])
    }

    /**
     * Called after the 'save' button in create.gsp is called
     */
    def save() {
//        setHibernateSessionEm(session)
        def user = null
        try {
            user = new UserAccount(params.name, params.pwd, params.fullname, params.description)
            user.email = params.email
            user.language = UiLanguage.findByIsoCode('und')
            if (params.containsKey('sudoable')) {
                user.sudoable = true
            }
            if (params.containsKey('sudoer')) {
                user.sudoer = true
            }
            user.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save user:", e)
            flash.message = e.getLocalizedMessage()
            return redirect(action: 'create')
        }

        // create home/, searches/, carts/, config/ in .../users/<username>-Folder:
        def folderPath = findAllByPath('/system/users/', true)
        log.debug "folderPath = ${folderPath.dump()}"

        def defaultAcl = Acl.findByName(Constants.ACL_DEFAULT)
        def defaultType = FolderType.findByName(Constants.FOLDER_TYPE_DEFAULT)

        def userFolder = new Folder(user.name, '', defaultAcl, folderPath[-1], findAdminUser(), defaultType)
        userFolder.save(flush: true)
        log.debug "created user folder '${userFolder.dump()}'"

        ['home', 'searches', 'carts', 'config'].each {
            def folder = new Folder(it, '', defaultAcl, userFolder, user, defaultType)
            folder.save()
            log.debug "created folder '${folder.dump()}'"
        }

        // create user-group:
        groupService.createUserGroup(user)
        userService.addUserToUsersGroup(user)

        redirect(action: 'show', params: [id: user.id])
    }

    /**
     * Groovy version of FolderDAOHibernate.findAllByPath()
     */
    protected List<Folder> findAllByPath(String path, Boolean createMissingFolders) {
        def parent = folderService.findRootFolder()

        List<Folder> ret = new ArrayList<Folder>()
        path.split("/").each() { seg ->
            if (seg.length() > 0) {
                def folders = Folder.findAllWhere(parent: parent, name: seg)

                if (folders.size() == 0) { // create missing folders
                    if (createMissingFolders) {
                        Folder f = new Folder(name: seg,
                                owner: findAdminUser(),
                                parent: parent,
                                type: FolderType.findByName(Constants.FOLDER_TYPE_DEFAULT),
                                acl: Acl.findByName(Constants.ACL_DEFAULT))
                        f.save(flush: true)
                        folders = [f]
                    }
                    else {
                        throw new RuntimeException("Invalid path '$path'")
                    }
                }
                Folder folder = folders[0]
                parent = folder
                ret << folder
            }
        }
        return ret
    }

    protected UserAccount findAdminUser() {
        def user = UserAccount.findByName('admin')
        if (user == null) {
            throw new RuntimeException('Dandelion cannot use the Cinnamon Server without a user "admin".')
        }
        return user
    }

    def updateList() {
        setListParams()
        render(template: 'userList', model: [userList: UserAccount.list(params)])
    }

    //---------------------------------------------------
    // Cinnamon XML Server API
    def listXml() {
        Document doc = DocumentHelper.createDocument()
        Element root = doc.addElement("users");
        UserAccount.list().each {user ->
            root.add(UserAccount.asElement("user", user));
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }

}
