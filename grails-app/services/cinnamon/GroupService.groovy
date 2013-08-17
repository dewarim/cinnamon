package cinnamon

/**
 *
 */
class GroupService {

    void createUserGroup(UserAccount user) {
        String groupName = "_${user.id}_${user.name}"
        def group = new CmnGroup(groupName, true, null)
        group.save(flush: true)
        CmnGroupUser gu = new CmnGroupUser(userAccount:user, cmnGroup:group)
        user.addToGroupUsers(gu)
        group.addToGroupUsers(gu)
        gu.save(flush: true)
    }
}
