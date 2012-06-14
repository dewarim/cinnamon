package cinnamon

import cinnamon.global.Constants
import org.dom4j.Element
import cinnamon.utils.ParamParser
import org.dom4j.DocumentHelper
import cinnamon.global.ConfThreadLocal
import cinnamon.exceptions.CinnamonException
import cinnamon.global.Conf
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import humulus.EnvironmentHolder
import cinnamon.i18n.UiLanguage

/**
 *
 */
class UserService {

    def springSecurityService
    def healthService

    Boolean isSuperuser(UserAccount user) {
        def auth = springSecurityService.authentication
        def authorities = auth.authorities
        return authorities.find {
            log.debug("authority: ${it.authority}")
            it.authority?.equals(Constants.GROUP_SUPERUSERS)
        } != null
    }

    UserAccount getUser() {
        def principal = springSecurityService.getPrincipal();
//        log.debug("principal: $principal")
        if (principal instanceof GrailsUser) {
            return UserAccount.get(principal.getId())
        }
        return null
    }

    void addUserToUsersGroup(UserAccount user) {
        def usersGroup = CmnGroup.findByName(Constants.GROUP_USERS)
        if (!usersGroup) {
            usersGroup = healthService.createGroup(Constants.GROUP_USERS)
        }
        CmnGroupUser gu = new CmnGroupUser(usersGroup, user)
        user.addToGroupUsers(gu)
        usersGroup.addToGroupUsers(gu)
        gu.save()
    }

    /**
     * Transfer all objects from one user to another.
     * This includes CmnGroupUser, Folder and OSD objects.
     * Sessions of the source user will be simply removed.
     * The goal is to remove everything which prevents the source user account from being deleted.
     * TransferAssets may be incompatible with legal requirements for record keeping. (But not
     * all systems require full accountability.)
     * @param source current owner
     * @param target new owner of assets
     */
    void transferAssets(UserAccount source, UserAccount target) {
        transferGroupMembership(source, target)
        // refreshAclCache for target?
        transferFolderOwnership(source, target)
        transferOsdOwnership(source, target)
        removeSessions(source)
    }

    Boolean transferAssetsAllowed(String repositoryName) {
        Conf conf = ConfThreadLocal.getConf()
        String xpath = 'repositories/repository[name="' + repositoryName + '"]/security/transferAssetsAllowed'
        return conf.getField(xpath, 'false').equals('true')
    }

    Boolean deleteUserAllowed(String repositoryName) {
        Conf conf = ConfThreadLocal.getConf()
        String xpath = 'repositories/repository[name="' + repositoryName + '"]/security/deleteUserAllowed'
        return conf.getField(xpath, 'false').equals('true')
    }

    void transferGroupMembership(UserAccount source, UserAccount target) {
        def groupUsers = CmnGroupUser.findAllByUserAccount(source)
        groupUsers.each {gu ->
            if (CmnGroupUser.findByUserAccountAndCmnGroup(target, gu.cmnGroup)) {
                // simply delete old user's CmnGroupUser object as both
                // old and new are in the same group
                gu.delete()
            }
            else {
                // take over groupUser from other user.
                log.debug("transfer group ${gu.cmnGroup.name} to user ${target.name}")
                gu.userAccount = target
            }
        }
    }

    void transferFolderOwnership(UserAccount source, UserAccount target) {
        Folder.findAllByOwner(source).each {folder ->
            log.debug("transfer folder ownership of ${folder.name} to user ${target.name}")
            folder.owner = target
        }
    }

    Boolean userHasAssets(UserAccount user) {
        if (ObjectSystemData.findByOwner(user) ||
                ObjectSystemData.findByModifier(user) ||
                ObjectSystemData.findByCreator(user) ||
                ObjectSystemData.findByLocker(user)
        ) {
            return true
        }
        if (Folder.findByOwner(user)) {
            return true
        }
        if (CmnGroupUser.findByUserAccount(user)) {
            return true
        }
        return false
    }

    /**
     * Replace all references to a given user with references to a target user.
     * @param source The original owner / modifier / creator / lock owner.
     * @param target The new user who replaces the source user in all cases.
     */
    void transferOsdOwnership(UserAccount source, UserAccount target) {
        ObjectSystemData.findAllByOwner(source).each {osd ->
            log.debug("transfer object ownership of #${osd.id} to user ${target.name}")
            osd.owner = target
        }
        ObjectSystemData.findAllByModifier(source).each {osd ->
            log.debug("transfer modifier label of #${osd.id} to user ${target.name}")
            osd.modifier = target
        }
        ObjectSystemData.findAllByCreator(source).each {osd ->
            log.debug("transfer creator label of #${osd.id} to user ${target.name}")
            osd.creator = target
        }
        ObjectSystemData.findAllByLocker(source).each {osd ->
            log.debug("transfer lock_owner label of #${osd.id} to user ${target.name}")
            osd.locker = target
        }
    }

    void removeSessions(UserAccount source) {
        log.debug("remove sessions of user ${source.name}")
        cinnamon.Session.findAllByUser(source).each {userSession ->
            userSession.delete()
        }
    }

    /**
     * Create a new UUID token and store it in {@link UserAccount#token} for email validation and password reset.
     * @return the new UUID string token 
     * @throws cinnamon.exceptions.CinnamonException if maxTokensPerDay has been reached.
     */
    String createToken(UserAccount user) {
        /*
         * reset tokens_today if token_age > 24 hours
         * get maxTokens a user may create per day
         * throw exception if too many tokens have already been created
         * create a new token
         */

        // just using Date-86400s would also work...
        Calendar today = Calendar.getInstance();
        Calendar tokenCal = Calendar.getInstance();
        tokenCal.setTime(user.tokenAge);
        if (!(today.get(Calendar.DAY_OF_MONTH) == (tokenCal.get(Calendar.DAY_OF_MONTH)))) {
            user.tokensToday = 0;
        }
        else {
            user.tokensToday++;
        }

        String maxTokensPerDay = ConfThreadLocal.getConf().getField("maxTokensPerDay", "3");
        Integer maxTokens = Integer.parseInt(maxTokensPerDay);
        if (user.tokensToday >= maxTokens) { // we start tokensToday with 0, so >= it is.
            throw new CinnamonException("error.too_many_tokens");
        }

        user.token = UUID.randomUUID().toString();
        return user.token;
    }

    /**
     * Sets the token field to a random value.
     */
    void clearToken(user) {
        user.token = Math.random() + "::" + Math.random();
    }

    public List getUsersPermissions(user, acl) {
        if (user.verifySuperuserStatus()) {
            return Permission.list().collect {it.name};
        }

        log.debug("groupUsers for user " + user.getName() + ": " + user.getGroupUsers().size());

        Set<CmnGroup> groups = user.findAllGroups();
        log.debug("number of groups for this user: " + groups.size());

        Set<String> permissions = new HashSet<String>();
        for (CmnGroup group : groups) {
            /*
             * If there are many groups whose AclEntries point to the
             * same Acls, it could be better to first collect the
             * entries before adding their Permissions.
             */
            log.debug("working on group:" + group.getName());
            for (AclEntry ae : group.getAclEntries()) {
                log.debug("working on AclEntry for Acl:" + ae.getAcl().getName());
                Long aclId = ae.getAcl().getId();
                if (aclId.equals(acl.id)) {
                    log.debug("found acl");
                    Set<AclEntryPermission> aepSet = ae.getAePermissions();
                    for (AclEntryPermission aep : aepSet) {
                        permissions.add(aep.getPermission().name);
                    }
                }
            }
        }
        log.debug("number of permissions for this user: " + permissions.size());
        return permissions
    }

    /**
     * Return the current session ticket for a user - or create a new one if he does not yet have one.
     * @param userAccount
     * @return a session ticket string, consisting of UUID.random + @ + repositoryName 
     */
    String fetchTicket(UserAccount userAccount) {
        if (userAccount == null) {
            userAccount = getUser()
        }
        Session session = Session.find("from Session s where s.user=:user order by expires desc", 
                [user: userAccount], [max: 1])
        if (!session || session.expires < new Date()) {
            def uiLanguage = userAccount.language
            if(uiLanguage == null){
                // a user *should* have at least the 'undetermined' language (und), 
                // but it seems like that's not always the case.
                uiLanguage = UiLanguage.findByIsoCode('und')
                userAccount.language = uiLanguage
                userAccount.save()
            }
            session = new Session(EnvironmentHolder.environment.dbName, userAccount, 'localhost', uiLanguage)
            session.save()
        }
        return session.ticket
    }
}
