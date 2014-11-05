package com.github.epierce

import groovy.json.*
import org.apache.commons.codec.digest.DigestUtils
import groovy.util.logging.Slf4j

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory

import com.google.api.services.admin.directory.Directory
import com.google.api.services.admin.directory.model.User
import com.google.api.services.admin.directory.model.Users
import com.google.api.services.admin.directory.model.UserName

@Slf4j
class GoogleAppsClient {

    private Directory directoryService

   /**
   * Creates a new GoogleAppsClient object
   *
   * @param applicationName THe name used to generate the OAUTH2 token
   * @param httpTransport HttpTransport object used to connect to Google services
   * @param jsonFactory
   * @param credential Google OAUTH2 access token
   */
    def GoogleAppsClient(   String applicationName,
                            HttpTransport httpTransport,
                            JsonFactory jsonFactory,
                            Credential credential) throws Exception {

        directoryService = new Directory.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build()


    }

    private def convertUserEntrytoMap(User userEntry) {
        def userMap = [
                creationTime    : userEntry.getCreationTime(),
                customerId      : userEntry.getCustomerId(),
                id              : userEntry.getId(),
                inGAL           : userEntry.getIncludeInGlobalAddressList(),
                isAdmin         : userEntry.getIsAdmin(),
                isDelegatedAdmin: userEntry.getIsDelegatedAdmin(),
                lastLoginTime   : userEntry.getLastLoginTime(),
                orgPath         : userEntry.getOrgUnitPath(),
                primaryEmail    : userEntry.getPrimaryEmail(),
                suspended       : userEntry.getSuspended(),
                suspendedReason : userEntry.getSuspensionReason(),
                photo           : userEntry.getThumbnailPhotoUrl(),
                familyName      : userEntry.getName().familyName,
                givenName       : userEntry.getName().givenName,
                changePasswordAtNextLogin: userEntry.getChangePasswordAtNextLogin()
        ]
    }
//
//    private def convertNicknameEntrytoMap(nicknameEntry){
//        def userMap = [ username: nicknameEntry.getLogin().userName,
//                        nickname: nicknameEntry.getNickname().name]
//    }

/**
   * Creates a new user with an email account.
   *
   * @param username The username of the new user.
   * @param givenName The given name for the new user.
   * @param familyName the family name for the new user.
   * @param password The password for the new user.
   * @return A User object of the newly created user.
   * service.
   */
  public def createUser(username, givenName, familyName, password) {
    return createUser(username, givenName, familyName, password, null);
  }

  /**
   * Creates a new user with an email account.
   *
   * @param username The username of the new user.
   * @param givenName The given name for the new user.
   * @param familyName the family name for the new user.
   * @param password The password for the new user.
   * @param passwordHashFunction Specifies the hash format of the password parameter
   * @return A User object of the newly created user.
   * service.
   */
    public def createUser(username, givenName, familyName, password, passwordHashFunction) {
        log.debug("Creating user '${username}'  Given Name: '${givenName}'  Family Name: '${familyName}' Hash Function: ${passwordHashFunction ?: 'plain'}")

        def user = new User()

        // populate the required fields only
        def name = new UserName()
        name.familyName = familyName
        name.givenName = givenName
        user.name = name

        if (passwordHashFunction) user.hashFunction = passwordHashFunction
        user.password = password

        user.primaryEmail = username

        directoryService.users().insert(user).execute()
  }

  /**
   * Retrieves a user.
   *
   * @param username The user you wish to retrieve.
   * @return A User object of the retrieved user.
   * service.
   */
    def retrieveUser(String username) {
        log.debug("Retrieving user ${username}.")

        directoryService.users().get(username).execute()
      }


/**
   * Retrieves all users in domain.  This method may be very slow for domains
   * with a large number of users.  Any changes to users, including creations
   * and deletions, which are made after this method is called may or may not be
   * included in the Feed which is returned.
   *
   * @return A list of all of the retrieved users.
   * service.
   */
    def retrieveAllUsers() {
        log.debug("Retrieving all users.")

        List<User> allUsers = new ArrayList<User>();
        Directory.Users.List request = directoryService.users().list().setCustomer("my_customer");

        // Get all users
        while (true) {
            try {
                Users currentPage = request.execute()
                allUsers.addAll(currentPage.users)
                request.pageToken = currentPage.nextPageToken
                log.error("Received ${currentPage.users.size()} user entries.")
            } catch (IOException e) {
                System.out.println("An error occurred: " + e)
                request.pageToken = null
            }
            if (request.pageToken == null) break
        }

        return allUsers
    }

    /**
    * Updates a user.
    *
    * @param userDetails Map of the user's account debug.
    * @return A Map of the details about the newly updated user.

    * service.
    */
    def updateUser(userDetails) {
        log.debug("Updating user ${userDetails.username}")

        def userEntry = directoryService.users().get(userDetails.username).execute()

        if (userEntry) {
            if (userDetails.newUsername && (userDetails.newUsername != userEntry.primaryEmail)) userEntry.primaryEmail = userDetails.newUsername
            if (userDetails.givenName && (userDetails.givenName != userEntry.name.givenName)) userEntry.name.givenName = userDetails.givenName
            if (userDetails.familyName && (userDetails.familyName != userEntry.name.familyName)) userEntry.name.familyName = userDetails.familyName
            if (userDetails.passwordHashFunction && userDetails.password) {
                userEntry.hashFunction = userDetails.passwordHashFunction
                userEntry.password = userDetails.password
            }

            directoryService.users().update(userDetails.username, userEntry).execute()
        }
    }

    def changePasswordMD5(String username, String password){

        def md5Digest = DigestUtils.md5Hex(password.getBytes("UTF-8"))

        def userDetails = [username: username, passwordHashFunction: 'MD5', password: md5Digest]

        updateUser(userDetails)
    }

    def changePasswordSHA1(String username, String password){

        def sha1Digest = DigestUtils.shaHex(password.getBytes("UTF-8"))

        def userDetails = [username: username, passwordHashFunction: 'SHA-1', password: sha1Digest]

        updateUser(userDetails)
    }
//
//    /**
//    * Deletes a user.
//    *
//    * @param username The user you wish to delete.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    *         service.
//    */
//    def deleteUser(username) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Deleting user ${username}")
//
//        def deleteUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        userService.delete(deleteUrl)
//    }
//
    /**
    * Suspends a user. Note that executing this method for a user who is already suspended has no effect.
    *
    * @param username The user you wish to suspend.
    */
    def suspendUser(username) {
        log.debug("Suspending user ${username}")

        def userEntry = directoryService.users().get(username).execute()
        userEntry.suspended = true

        directoryService.users().update(username, userEntry).execute()

    }

    /**
    * Restores a user. Note that executing this method for a user who is not suspended has no effect.
    *
    * @param username The user you wish to restore.

    */
    def restoreUser(username) {
        log.debug("Restoring user ${username}")

        def userEntry = directoryService.users().get(username).execute()
        userEntry.suspended = false

        directoryService.users().update(username, userEntry).execute()
    }

    /**
    * Set admin privilege for user. Note that executing this method for a user who is already an admin has no effect.
    *
    * @param username The user you wish to make an admin.
    */
    def addAdminPrivilege(username) {
        log.debug("Setting admin privileges for user ${username}")

        User userEntry = directoryService.users().get(username).execute()
        userEntry.setIsAdmin(true)
        directoryService.users().update(username, userEntry).execute()
    }

    /**
    * Remove admin privilege for user. Note that executing this method for a user who is not an admin has no effect.
    *
    * @param username The user you wish to remove admin privileges.
    */
    def removeAdminPrivilege(username) {
        log.debug("Removing admin privileges for user ${username}")

        User userEntry = directoryService.users().get(username).execute()
        userEntry.setIsAdmin(false)
        directoryService.users().update(username, userEntry).execute()
    }
//
//    /**
//    * Require a user to change password at next login. Note that executing this
//    * method for a user who is already required to change password at next login
//    * as no effect.
//    *
//    * @param username The user who must change his or her password.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    *         service.
//    */
//    def forceUserToChangePassword(username) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Requiring ${username} to change password at next login")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def userEntry = userService.getEntry(retrieveUrl, UserEntry.class)
//        userEntry.getLogin().setChangePasswordAtNextLogin(true)
//
//        def updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def result = userService.update(updateUrl, userEntry)
//        convertUserEntrytoMap(result)
//    }
//
//    /**
//    * Creates a nickname for the username.
//    *
//    * @param username The user for which we want to create a nickname.
//    * @param nickname The nickname you wish to create.
//    * @return A NicknameEntry object of the newly created nickname.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    * service.
//    */
//    def createNickname(username, nickname) throws AppsForYourDomainException, ServiceException, IOException {
//
//        log.debug("Creating nickname ${nickname} for user ${username}")
//
//        def entry = new NicknameEntry()
//        def nicknameExtension = new Nickname()
//        nicknameExtension.setName(nickname);
//        entry.addExtension(nicknameExtension)
//
//        def login = new Login()
//        login.setUserName(username)
//        entry.addExtension(login)
//
//        def insertUrl = new URL(domainUrlBase + "nickname/" + SERVICE_VERSION)
//        def result = nicknameService.insert(insertUrl, entry)
//        convertNicknameEntrytoMap(result)
//    }
//
//    /**
//    * Retrieves a nickname.
//    *
//    * @param nickname The nickname you wish to retrieve.
//    * @return A NicknameEntry object of the newly created nickname.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    * service.
//    */
//    def retrieveNickname(nickname) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Retrieving nickname ${nickname}")
//
//        def retrieveUrl = new URL(domainUrlBase + "nickname/" + SERVICE_VERSION + "/" + nickname)
//        def result = nicknameService.getEntry(retrieveUrl, NicknameEntry.class)
//        convertNicknameEntrytoMap(result)
//    }
//
//    /**
//    * Retrieves all nicknames for the given username.
//    *
//    * @param username The user for which you want all nicknames.
//    * @return A NicknameFeed object with all the nicknames for the user.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    *         service.
//    */
//    def retrieveNicknames(username) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Retrieving nicknames for user ${username}")
//
//        def feedUrl = new URL(domainUrlBase + "nickname/" + SERVICE_VERSION)
//        def query = new AppsForYourDomainQuery(feedUrl)
//        query.setUsername(username);
//        def feedResult = nicknameService.query(query, NicknameFeed.class)
//
//        def nicknameList = []
//        feedResult.entries.each { nicknameEntry ->
//            def nickMap = convertNicknameEntrytoMap(nicknameEntry)
//            nicknameList.add(nickMap)
//        }
//
//        return nicknameList
//    }
//
//    /**
//    * Deletes a nickname.
//    *
//    * @param nickname The nickname you wish to delete.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    * service.
//    */
//    def deleteNickname(nickname) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Deleting nickname ${nickname}")
//
//        def deleteUrl = new URL(domainUrlBase + "nickname/" + SERVICE_VERSION + "/" + nickname)
//        nicknameService.delete(deleteUrl)
//    }

}