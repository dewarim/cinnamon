package cinnamon.workflow.actions

import cinnamon.ObjectSystemData
import cinnamon.UserAccount
import cinnamon.global.Constants

/**
 * This class generates a notification for a Cinnamon user in the form of an OSD,
 * delivered to his or her inbox folder in the current repository.
 */
class CinnamonNotification {
    
    def folderService
    
    ObjectSystemData create(UserAccount user, String name, String content){
        def folders = folderService.findAllByPath("system/users/${user.name}/inbox", true, null)
        def inbox = folders.last()
        
        def notification = new ObjectSystemData(name, user, inbox)
        notification.save()
        notification.updateIndex()
        def noteMeta = notification.fetchMetaset(Constants.METASET_NOTIFICATION, true)
        noteMeta.content = content
        return notification
    }
    
}
