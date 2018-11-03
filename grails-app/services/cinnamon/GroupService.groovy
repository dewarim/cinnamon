package cinnamon

/**
 *
 */
class GroupService {

    CmnGroup createUserGroup(UserAccount user) {
        String groupName = "_${user.id}_${user.name}"
        def group = new CmnGroup(groupName, true, null)
        group.save(flush: true)
        CmnGroupUser gu = new CmnGroupUser(userAccount:user, cmnGroup:group)
        gu.save(flush: true)
        return group
    }

    void deleteUserGroup(UserAccount user){
        String groupName = "_${user.id}_${user.name}"
        def group = CmnGroup.findByName(groupName)
        group.delete(flush: true)
    }
}
