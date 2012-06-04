package cinnamon

/**
 * This class contains a folder type's most common config values.
 * 
 */
class FolderConfig {
    
    String controller = 'folder'
    String action = 'fetchFolderContent'
    String template = '/folder/folderContent'
    
    String toString(){
        return "controllerName: ${controller}, actionName: ${action}, template: ${template}"
    }
}
