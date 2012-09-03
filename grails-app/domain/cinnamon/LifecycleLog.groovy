package cinnamon

/**
 * Optional LifecycleLog
 */
class LifecycleLog {

    static constraints = {
        name size: 1..255
        folderPath size: 1..8191
        newStateName size: 1..255
        lifecycleName size: 1..255
        oldStateName size: 1..255
        newStateName size: 1..255
        userName size: 1..255
    }
    
    static mapping = {
        datasource 'logging'
        version false
    }
    
    String repository
    Long hibernateId
    String userName
    Long userId
    String lifecycleName
    String lifecycleId
    Date dateCreated
    String oldStateName
    String newStateName
    Long oldStateId
    Long newStateId
    String folderPath
    String name
    
}
