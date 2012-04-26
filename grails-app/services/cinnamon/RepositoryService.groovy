package cinnamon

/**
 * NOTE: Refactoring is in progress.
 * <p>The RepositoryService contains methods to access a Cinnamon server.<br>
 * This allows Dandelion to use functions of the server like a normal client would,
 * without the need to implement those features in a Grails style again. It also
 * ensures that users of the web client are subject to the same access restrictions
 * as those who use the desktop client.
 * </p>
 * Note that in case the Cinnamon server goes offline or the session times out
 * the RepositoryService may not always function correctly - in those cases, a
 * logout and login is required.
 */
class RepositoryService {

//    void addClient(Client client) {
//        log.debug("addClient called for client: ${client} (${client.repository})")
//        def repositoryName = client.repository
//        def clientSet = repositoryClients.get(repositoryName)
//        if (clientSet) {
//            clientSet.add(client)
//        }
//        else {
//            clientSet = new HashSet<Client>()
//            clientSet.add(client)
//            repositoryClients.put(repositoryName, clientSet)
//        }
//    }
//
//    Client getClient(String username, String repositoryName) {
//        log.debug("looking up $username at repository $repositoryName")
//        Set<Client> clientSet = repositoryClients.get(repositoryName)
//        if (clientSet) {
//            Client repositoryClient = (Client) clientSet.find {client ->
//                client.username.equals(username)
//            }
//            if (repositoryClient) {
//                if (!repositoryClient.hasTicket()) {
//                    repositoryClient.connect()
//                }
//                return repositoryClient
//            }
//            else {
//                log.debug("no client found for username $username @ $repositoryName")
//                throw new RuntimeException('error.no.client')
//            }
//        }
//        else {
//            log.debug("no clients found for repository $repositoryName")
//            throw new RuntimeException('error.no.client')
//        }
//    }
//
//    /**
//     * Lock an OSD. Does nothing if the OSD is already locked by this user.
//     * Throws a RuntimeException if the OSD is locked by another user.
//     * @param user the user for whom the OSD is to be locked
//     * @param osd the osd to be locked.
//     * @param repositoryName the name of the repository where the OSD resides.
//     */
//    void acquireLock(UserAccount user, ObjectSystemData osd, String repositoryName) {
//        Client client = getClient(user.name, repositoryName)
//        if (osd.locked_by && !osd.locked_by.equals(user)) {
//            // osd is locked by another user
//            throw new RuntimeException('error.locked.already')
//        }
//        if (!osd.locked_by) {
//            try {
//                client.lock(osd.id)
//            }
//            catch (Exception e) {
//                log.debug("Exception while trying to acquire a lock on ${osd.id}", e)
//                throw new RuntimeException('error.acquire.lock')
//            }
//        }
//    }
//
//    /**
//     * Unlock an OSD. Does nothing if the OSD is already unlocked. Throws a
//     * RuntimeException if the OSD is locked by another user.
//     * @param user user whose lock will be removed
//     * @param osd the locked object
//     * @param repositoryName name of the repository where the OSD resides
//     *
//     */
//    void unlockOsd(UserAccount user, ObjectSystemData osd, String repositoryName) {
//        Client client = getClient(user.name, repositoryName)
//        try {
//            if (osd.locked_by && !(osd.locked_by.equals(user) || checkSuperuser(user))) {
//                throw new RuntimeException('error.locked.foreign')
//            }
//            else if (osd.locked_by) {
//                client.unlock(osd.id)
//            }
//        }
//        catch (Exception e) {
//            log.debug("Exception while trying to unlock ${osd.id}", e)
//            throw new RuntimeException("error.unlock.failed")
//        }
//    }
//
//    Boolean checkSuperuser(UserAccount user) {
//        CmnGroup superusers = CmnGroup.findByName(Constants.GROUP_SUPERUSERS)
//        CmnGroupUser gu = CmnGroupUser.findByGroupAndUser(superusers, user)
//        return gu != null
//    }
//
//    /**
//     * Set custom metadata on an OSD. The OSD has to be locked in advance.
//     * @param user user who is responsible for this action
//     * @param id id of the OSD
//     * @param metadata the metadata to be set
//     * @param repositoryName name of the repository where the OSD resides
//     */
//    void setMetadata(UserAccount user, String id, String metadata, String repositoryName) {
//        log.debug("repository: $repositoryName")
//        Long osdId = Long.parseLong(id)
//
//        Client client = getClient(user.name, repositoryName)
//        client.setMeta(osdId, metadata)
//    }
//
//    /**
//     * Retrieve the text content of an OSD (if the content is binary,
//     * you will probably get garbled output).
//     * @param user the user requesting the content
//     * @param osd the osd from which the content will be extracted.
//     * @param repositoryName name of repository where the OSD resides.
//     * @return the string content of the OSD
//     */
//    String getTextContent(UserAccount user, ObjectSystemData osd, String repositoryName) {
//        Client client = getClient(user.name, repositoryName)
//        return client.getContent(osd.id)
//    }
//
//    /**
//     * Find which permissions have been granted to this user by this ACL.
//     * @param user the user whose permissions are retrieved.
//     * @param acl the ACL which is examined.
//     * @param repositoryName repository where the ACL is to be found
//     * @return a set of Strings containing the constant permission names of the
//     * granted permissions.
//     */
//    Set<String> getUserPermissions(UserAccount user, Acl acl, String repositoryName) {
//        Client client = getClient(user.name, repositoryName)
//        HashSet<String> set = new HashSet<String>()
//        def perms = client.getUsersPermissions(user.id, acl.id)
//        set.addAll(new XmlParser().parseText(perms).permission.sysName.collect {it.text()})
//        return set
//    }
//
//    /**
//     * Retrieve the user by client. This may be useful as the CinnamonServer
//     * will also return information about the superuser status.
//     * @param user the user whose details you want
//     * @param repositoryName name of this user's repository
//     * @return xml root node of user
//     */
//    Node getUserDetails(UserAccount user, String repositoryName) {
//        Client client = getClient(user.name, repositoryName)
//        def userDetails = client.getUser(user.id)
//        log.debug("userDetails: $userDetails")
//        return new XmlParser().parseText(userDetails)
//    }
//
//    /**
//     * Returns a set of Ids of the folders inside the specified folder.
//     * @param user the user who requests the information
//     * @param folder the folder whose content are to be examined
//     * @param repositoryName the repository in which the folder resides.
//     * @return a set of ids
//     */
//    Set<Long> getFoldersInside(UserAccount user, Folder parentFolder, String repositoryName) {
//        Client client = getClient(user.name, repositoryName)
//        def subFolders = client.fetchSubfolders(parentFolder.id)
//        def folders = new XmlParser().parseText(subFolders).folder
//        HashSet<Long> hs = new HashSet<Long>()
//        hs.addAll(folders.collect {Long.parseLong(it.id.text())})
//        return hs
//    }
//
//    /**
//     * Check if a user may browse a certain folder.
//     * @param user
//     * @param folder
//     * @param repositoryName
//     * @return
//     */
//    Boolean mayBrowseFolder(UserAccount user, Folder folder, String repositoryName) {
//        Client client = getClient(user.name, repositoryName)
//        try {
//            def folderXml = client.getFolder(folder.id)
//            def node = new XmlParser().parseText(folderXml)
//            return node?.folder?.id?.find { it.text().equals(String.valueOf(folder.id)) } != null
//        }
//        catch (RuntimeException ex) {
//            log.debug("access denied to folder ${folder.id}")
//            return false
//        }
//    }
//
//    /**
//     * Check if a user may browse an object's metadata.
//     * @param user
//     * @param folder
//     * @param repositoryName
//     * @return
//     */
//    Boolean mayBrowseOsd(UserAccount user, ObjectSystemData osd, String repositoryName) {
//        Client client = getClient(user.name, repositoryName)
//        try {
//            def xml = client.getObject(osd.id)
//            def node = new XmlParser().parseText(xml)
//            return node?.object?.id?.find { it.text().equals(String.valueOf(osd.id)) } != null
//        }
//        catch (RuntimeException ex) {
//            log.debug("access denied to folder ${osd.id}")
//            return false
//        }
//    }
//
//    List<ObjectSystemData> getObjects(UserAccount user, Folder parent, String repositoryName, String versions){
//        Client client = getClient(user.name, repositoryName)
//        String versionPred = ObjectSystemData.fetchVersionPredicate(versions);
//        def osdList = ObjectSystemData.findAll("from ObjectSystemData as o where o.parent=:parent " + versionPred + " order by id", [parent: parent])
//        def filteredList = []
//        try{
//            def response = client.getObjects(parent.id, versions)
////            log.debug("response:\n $response")
//            def xml = new XmlSlurper().parseText(response)
//            Set idSet = xml.object.id.collect{Long.parseLong(it.text())}
////            log.debug("idSet: ${idSet}")
//            osdList.each{ osd ->
//                if(idSet.contains(osd.id)){
//                    filteredList.add(osd)
//                }
//            }
//        }
//        catch (RuntimeException e){
//            log.debug("Failed to load objects",e)
//            throw new RuntimeException(e)
//        }
//        return filteredList
//    }
//
//    List<Folder> getFolders(UserAccount user, Folder parent, String repositoryName){
//        def folders = Folder.findAll("from Folder f where f.parent=:parent", [parent: parent])
//        def filteredList = []
//        try{
//            Set idSet = getFoldersInside(user, parent, repositoryName)
//            folders.each{ folder ->
//                if(idSet.contains(folder.id)){
//                    filteredList.add(folder)
//                }
//            }
//        }
//        catch (RuntimeException e){
//            log.debug("Failed to load folders",e)
//            throw new RuntimeException(e)
//        }
//        return filteredList
//    }
//
//    void removeUserFromCache(UserAccount user, String repositoryName) {
//        try {
//            Client client = getClient(user?.name, repositoryName)
//            if (client && client.hasTicket()) {
//                client.disconnect()
//                repositoryClients.get(repositoryName)?.remove(client)
//            }
//        }
//        catch (RuntimeException ex) {
//            log.debug("Failed to disconnect from CinnamonServer", ex)
//        }
//    }
//
//    void updateFolder(String userName, String id, String fieldName, String fieldValue, String repositoryName) {
//        Client client = getClient(userName, repositoryName)
//        Map<String, String> fields = new HashMap<String,String>()
//        fields.put(fieldName, fieldValue)
//        client.updateFolder(id, fields)
//    }
//
//    void updateOsd(String userName, String id, String fieldName, String fieldValue, String repositoryName) {
//        Client client = getClient(userName, repositoryName)
//        Long oid = Long.parseLong(id)
//        client.lock(oid)
//        client.setSysMeta(oid, fieldName, fieldValue)
//        client.unlock(oid)
//    }
//
//    Boolean checkPermission(UserAccount user, Acl acl, String repositoryName, String permissionName){
//        def permissions = getUserPermissions(user, acl, repositoryName)
//        return permissions.contains(permissionName)
//    }

}
