package cinnamon.config

/**
 * 
 * Basic data transfer configuration container used to hold
 * username, password, port, url etc, individual fields being optional.
 */
class DataTransferConfig {
    
    String username
    String password
    Integer port
    String serverName
    String filename
    
    String remotePath
    String remotePathSeparator = '/'
}
