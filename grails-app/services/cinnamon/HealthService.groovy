package cinnamon

/**
 * The HealthService class contains methods to repair or create essential objects in the database.
 */
class HealthService {

    def groupService

    def fixUserGroups() {
          UserAccount.list().each{user ->
             if(! user.groupUsers.find{it.cmnGroup.groupOfOne}){
                 groupService.createUserGroup(user)
             }
         }
    }

    /**
     * Create a group if it does not already exist, using a default description.
     * @param name
     */
    CmnGroup createGroup(String name){
        CmnGroup group = CmnGroup.findByName(name)
        if(! group){
            group = new CmnGroup(name, name+'.description', false, null)
            group.save()
        }
        return group
    }
}
