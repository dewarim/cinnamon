package cinnamon

import cinnamon.authentication.LoginType
import org.dom4j.Element
import org.dom4j.Document
import org.dom4j.DocumentHelper
import grails.plugin.springsecurity.annotation.Secured
import cinnamon.global.Constants
import cinnamon.i18n.UiLanguage
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

@Secured(["isAuthenticated()"])
class UserAccountController extends BaseController {

    @Secured(["hasRole('_superusers')"])
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
        }
        else {
            log.debug("group has no subgroups")
        }

        [userList    : groupUsers.collect { it.userAccount },
         addList     : addList,
         hasSubGroups: hasSubGroups,
         group       : group]
    }

    /**
     * Replace a user in the repositories.
     * Present a form with source and target user.
     * All objects which belong to the source user in some way will be transferred.
     */
    @Secured(["hasRole('_superusers')"])
    def replaceUser() {
        [userList : UserAccount.list(),
         forbidden: !userService.transferAssetsAllowed(repositoryName)
        ]
    }

    @Secured(["hasRole('_superusers')"])
    def transferAssets() {
        try {
            if (!userService.transferAssetsAllowed(repositoryName)) {
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

    @Secured(["hasRole('_superusers')"])
    def create() {
        [user: flash.user ?: new UserAccount()]
    }

    @Secured(["hasRole('_superusers')"])
    def list() {
        setListParams()
        [userList: UserAccount.list(params)]
    }

    @Secured(["hasRole('_superusers')"])
    def show() {
        [user: UserAccount.get(params.id)]
    }

    @Secured(["hasRole('_superusers')"])
    def edit() {
        if (flash.user) {
            [user: flash.user]
        }
        else {
            [user: UserAccount.get(params.id)]
        }
    }

    @Secured(["hasRole('_superusers')"])
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
        def checkFields = ['sudoable', 'sudoer', 'activated', 'changeTracking']
        checkFields.each { field ->
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
            String groupName = "_${user.id}_${user.name}"
            CmnGroup group = CmnGroup.findByName(groupName)
            if (group == null) {
                // create user-group:
                group = groupService.createUserGroup(user)
                userService.addUserToUsersGroup(user)

            }
            group.name = "_${user.id}_${params.name}"
            group.save()
        }

        bindData(user, params, [include: ['name', 'fullname', 'description', 'email', 'pwd']])
        if (user.description == null) {
            user.description = '';
        }
        user.language = UiLanguage.get(params.'language.id')
        if (user.validate() && user.save(flush: true)) {
            flash.message = message(code: "user.update.success")
            redirect(action: 'show', params: [id: user.id])
        }
        else {
            flash.user = user
            redirect(action: 'edit', params: [id: user.id])
        }
    }

    @Secured(["hasRole('_superusers')"])
    def deleteAsk() {
        [userList        : UserAccount.list(),
         forbidden       : !userService.deleteUserAllowed(repositoryName),
         showTransferLink: params.showTransferLink
        ]
    }

    @Secured(["hasRole('_superusers')"])
    def doDelete() {
        try {
            if (!userService.deleteUserAllowed(repositoryName)) {
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
            if (userService.deleteUserAllowed(repositoryName)) {
                user.delete()
            }
            flash.message = message(code: 'user.delete.success', args: [user.name.encodeAsHTML()])
        }
        catch (RuntimeException e) {
            flash.message = message(code: 'user.delete.failed', args: [message(code: e.getMessage())])
            return redirect(controller: 'userAccount', action: 'deleteAsk', params: [showTransferLink: true])
        }
        return redirect(controller: 'userAccount', action: 'deleteAsk')
    }

    /**
     * add a group to a user
     */
    @Secured(["hasRole('_superusers')"])
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
    @Secured(["hasRole('_superusers')"])
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
    @Secured(["hasRole('_superusers')"])
    def save() {
        UserAccount user = null
        try {
            user = new UserAccount(params.name, params.pwd, params.fullname, params.description ?: '')
            user.email = params.email
            user.language = UiLanguage.findByIsoCode('und')
            user.sudoable = params.containsKey('sudoable')
            user.sudoer = params.containsKey('sudoer')
            user.changeTracking = params.containsKey('changeTracking')
            if (!user.validate()) {
                flash.user = user
                return redirect(action: 'create')
            }
            user.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to save user:", e)
            flash.user = user
            return redirect(action: 'create')
        }

        // create folders for user:
        folderService.createHomeFolders(user)

        // create user-group:
        groupService.createUserGroup(user)
        userService.addUserToUsersGroup(user)

        redirect(action: 'show', params: [id: user.id])
    }

    @Secured(["hasRole('_superusers')"])
    def updateList() {
        setListParams()
        render(template: 'userList', model: [userList: UserAccount.list(params)])
    }

    //---------------------------------------------------
    // Cinnamon XML Server API
    def listXml() {
        Document doc = DocumentHelper.createDocument()
        Element root = doc.addElement("users");
        UserAccount.list().each { user ->
            root.add(UserAccount.asElement("user", user));
        }
        render(contentType: 'application/xml', text: doc.asXML())
    }

    def getUserByName(String name) {
        Document doc = DocumentHelper.createDocument()
        Element root = doc.addElement("users");
        def user = UserAccount.findByName(name)
        root.add(UserAccount.asElement("user", user));
        render(contentType: 'application/xml', text: doc.asXML())
    }

    def changePassword(String password) {
        def user = userService.user
        if (!user) {
            renderErrorXml("invalid user")
            return
        }
        if(user.loginType != LoginType.CINNAMON){
            renderErrorXml("LDAP- and other non-Cinnamon accounts cannot change their password via this method.")
            return
        }
        if (password?.length() < Constants.MINIMUM_PASSWORD_LENGTH) {
            renderErrorXml('error.password.too.short')
            return
        }
        user.pwd = password
        render(contentType: 'application/xml') {
            success('success.set.password')
        }
    }

}
