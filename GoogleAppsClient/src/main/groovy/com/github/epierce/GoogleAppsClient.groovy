package com.github.epierce

import groovy.json.*
import java.security.MessageDigest
import groovy.util.logging.Slf4j

@Slf4j
class GoogleAppsClient {


    private static final String APPS_FEEDS_URL_BASE =  "https://apps-apis.google.com/a/feeds/"

    protected static final String SERVICE_VERSION = "2.0"

    protected def domainUrlBase
    protected def userService
    protected def domain
    protected def nicknameService

    def GoogleAppsClient(adminEmail, adminPassword, adminDomain) throws Exception {
        domain = adminDomain
        domainUrlBase = APPS_FEEDS_URL_BASE + adminDomain + "/"

    //    userService = new UserService("gdata-sample-AppsForYourDomain-UserService")
    //    userService.setUserCredentials(adminEmail, adminPassword)

    //    nicknameService = new NicknameService("gdata-sample-AppsForYourDomain-NicknameService")
    //    nicknameService.setUserCredentials(adminEmail, adminPassword)

    }
//
//    private def convertUserEntrytoMap(userEntry){
//        def userMap = [ username: userEntry.getLogin().userName,
//                        familyName: userEntry.getName().familyName,
//                        givenName: userEntry.getName().givenName,
//                        quota: userEntry.getQuota().limit,
//                        suspended: userEntry.getLogin().suspended,
//                        admin: userEntry.getLogin().admin,
//                        changePasswordAtNextLogin: userEntry.getLogin().changePasswordAtNextLogin ]
//    }
//
//    private def convertNicknameEntrytoMap(nicknameEntry){
//        def userMap = [ username: nicknameEntry.getLogin().userName,
//                        nickname: nicknameEntry.getNickname().name]
//    }
///**
//   * Creates a new user with an email account.
//   *
//   * @param username The username of the new user.
//   * @param givenName The given name for the new user.
//   * @param familyName the family name for the new user.
//   * @param password The password for the new user.
//   * @return A UserEntry object of the newly created user.
//   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//   * @throws ServiceException If a generic GData framework error occurs.
//   * @throws IOException If an error occurs communicating with the GData
//   * service.
//   */
//  public def createUser(username, givenName, familyName, password) throws AppsForYourDomainException, ServiceException, IOException {
//    return createUser(username, givenName, familyName, password, null, null);
//  }
//
//  /**
//   * Creates a new user with an email account.
//   *
//   * @param username The username of the new user.
//   * @param givenName The given name for the new user.
//   * @param familyName the family name for the new user.
//   * @param password The password for the new user.
//   * @param passwordHashFunction The name of the hash function to hash the
//   * password
//   * @return A UserEntry object of the newly created user.
//   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//   * @throws ServiceException If a generic GData framework error occurs.
//   * @throws IOException If an error occurs communicating with the GData
//   * service.
//   */
//  public def createUser(username, givenName, familyName, password, passwordHashFunction) throws AppsForYourDomainException, ServiceException, IOException {
//    return createUser(username, givenName, familyName, password, passwordHashFunction, null);
//  }
//
//  /**
//   * Creates a new user with an email account.
//   *
//   * @param username The username of the new user.
//   * @param givenName The given name for the new user.
//   * @param familyName the family name for the new user.
//   * @param password The password for the new user.
//   * @param passwordHashFunction Specifies the hash format of the password
//   * parameter
//   * @param quotaLimitInMb User's quota limit in megabytes.  This field is only
//   * used for domains with custom quota limits.
//   * @return A UserEntry object of the newly created user.
//   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//   * @throws ServiceException If a generic GData framework error occurs.
//   * @throws IOException If an error occurs communicating with the GData
//   * service.
//   */
//    public def createUser(username, givenName, familyName, password, passwordHashFunction, quotaLimitInMb) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Creating user '${username}'  Given Name: '${givenName}'  Family Name: '${familyName}' Hash Function: ${passwordHashFunction ?: 'plain'} Quota Limit: ${quotaLimitInMb ?: 25600}")
//
//        def entry = new UserEntry()
//        def login = new Login()
//        login.userName = username
//        login.password = password
//        if (passwordHashFunction) login.hashFunctionName = passwordHashFunction
//
//        entry.addExtension(login)
//
//        def name = new Name()
//        name.givenName = givenName
//        name.familyName = familyName
//        entry.addExtension(name)
//
//        if (quotaLimitInMb) {
//          def quota = new Quota()
//          quota.limit = quotaLimitInMb
//          entry.addExtension(quota)
//        }
//
//        def insertUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION )
//        def userEntry = userService.insert(insertUrl, entry)
//
//        convertUserEntrytoMap(userEntry)
//  }
//
//  /**
//   * Retrieves a user.
//   *
//   * @param username The user you wish to retrieve.
//   * @return A UserEntry object of the retrieved user.
//   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//   * @throws ServiceException If a generic GData framework error occurs.
//   * @throws IOException If an error occurs communicating with the GData
//   * service.
//   */
//    def retrieveUser(username) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Retrieving user ${username}.")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//
//        def userEntry = userService.getEntry(retrieveUrl, UserEntry.class)
//
//        convertUserEntrytoMap(userEntry)
//      }
//
//    def exists(username) {
//
//    }
///**
//   * Retrieves all users in domain.  This method may be very slow for domains
//   * with a large number of users.  Any changes to users, including creations
//   * and deletions, which are made after this method is called may or may not be
//   * included in the Feed which is returned.
//   *
//   * @return A UserFeed object of the retrieved users.
//   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//   * @throws ServiceException If a generic GData framework error occurs.
//   * @throws IOException If an error occurs communicating with the GData
//   * service.
//   */
//    def retrieveAllUsers() throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Retrieving all users.")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/")
//        def allUsers = new UserFeed()
//        def currentPage
//        def nextLink
//
//        while(true) {
//            currentPage = userService.getFeed(retrieveUrl, UserFeed.class)
//            allUsers.getEntries().addAll(currentPage.getEntries())
//            nextLink = currentPage.getLink(ILink.Rel.NEXT, ILink.Type.ATOM)
//            if (nextLink) {
//                retrieveUrl = new URL(nextLink.getHref())
//            } else {
//                break
//            }
//        }
//
//        def userList = []
//        allUsers.entries.each { userEntry ->
//            def userMap = convertUserEntrytoMap(userEntry)
//            userList.add(userMap)
//        }
//
//        return userList
//    }
//
//    /**
//    * Retrieves one page (100) of users in domain.  Any changes to users,
//    * including creations and deletions, which are made after this method is
//    * called may or may not be included in the Feed which is returned.  If the
//    * optional startUsername parameter is specified, one page of users is
//    * returned which have usernames at or after the startUsername as per ASCII
//    * value ordering with case-insensitivity.  A value of null or empty string
//    * indicates you want results from the beginning of the list.
//    *
//    * @param startUsername The starting point of the page (optional).
//    * @return A UserFeed object of the retrieved users.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    * service.
//    */
//    def retrievePageOfUsers(startUsername) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Retrieving one page of users starting at ${startUsername}")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/")
//        def query = new AppsForYourDomainQuery(retrieveUrl)
//        query.startUsername = startUsername
//        def userFeed = userService.query(query, UserFeed.class)
//
//        def userList = []
//        userFeed.entries.each { userEntry ->
//            def userMap = convertUserEntrytoMap(userEntry)
//            userList.add(userMap)
//        }
//
//        return userList
//    }
//
//    /**
//    * Updates a user.
//    *
//    * @param userDetails Map of the user's account debug.
//    * @return A Map of the deatils about the newly updated user.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    * service.
//    */
//    def updateUser(userDetails) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Updating user ${username}")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + userDetails.username)
//
//        def userEntry = userService.getEntry(retrieveUrl, UserEntry.class)
//
//        if (userDetails.newUsername && (userDetails.newUsername != userEntry.getLogin().userName)) userEntry.getLogin().userName = userDetails.newUsername
//        if (userDetails.givenName && (userDetails.givenName != userEntry.getName().givenName)) userEntry.getName().givenName = userDetails.givenName
//        if (userDetails.familyName && (userDetails.familyName != userEntry.getName().familyName)) userEntry.getName().familyName = userDetails.familyName
//        if (userDetails.passwordHashFunction && userDetails.password) {
//            userEntry.getLogin().hashFunctionName = userDetails.passwordHashFunction
//            userEntry.getLogin().password = userDetails.password
//        }
//        if (userDetails.quotaLimitInMb) currentAcct.getQuota().limit = userDetails.quotaLimitInMb
//
//        def result = userService.update(retrieveUrl, userEntry)
//
//        convertUserEntrytoMap(result)
//    }
//
//    def changePasswordMD5(username, password) throws AppsForYourDomainException, ServiceException, IOException {
//
//        def md5 = MessageDigest.getInstance("MD5")
//        md5.update(password.getBytes())
//        def hash = new BigInteger(1, md5.digest())
//        def hashedPassword = hash.toString(16)
//
//        def userDetails = [username: username, passwordHashFunction: "MD5", password: hashedPassword]
//
//        updateUser(userDetails)
//    }
//
//    def changePasswordSHA1(username, password) throws AppsForYourDomainException, ServiceException, IOException {
//
//        def sha1 = MessageDigest.getInstance("SHA1")
//        sha1.update(password.getBytes())
//        def hash = new BigInteger(1, sha1.digest())
//        def hashedPassword = hash.toString(16)
//
//        def userDetails = [username: username, passwordHashFunction: "SHA-1", password: hashedPassword]
//
//        updateUser(userDetails)
//    }
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
//    /**
//    * Suspends a user. Note that executing this method for a user who is already
//    * suspended has no effect.
//    *
//    * @param username The user you wish to suspend.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    *         service.
//    */
//    def suspendUser(username) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Suspending user ${username}")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def userEntry = userService.getEntry(retrieveUrl, UserEntry.class)
//        userEntry.getLogin().setSuspended(true);
//
//        def updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def result = userService.update(updateUrl, userEntry)
//        convertUserEntrytoMap(result)
//    }
//
//
//    /**
//    * Restores a user. Note that executing this method for a user who is not
//    * suspended has no effect.
//    *
//    * @param username The user you wish to restore.
//    * @throws AppsForYourDomainException If a Provisioning API specific occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    *         service.
//    */
//    def restoreUser(username) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Restoring user ${username}")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def userEntry = userService.getEntry(retrieveUrl, UserEntry.class)
//        userEntry.getLogin().setSuspended(false)
//
//        def updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def result = userService.update(updateUrl, userEntry)
//        convertUserEntrytoMap(result)
//    }
//
//    /**
//    * Set admin privilege for user. Note that executing this method for a user
//    * who is already an admin has no effect.
//    *
//    * @param username The user you wish to make an admin.
//    * @throws AppsForYourDomainException If a Provisioning API specific error
//    *         occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    *         service.
//    */
//    def addAdminPrivilege(username) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Setting admin privileges for user ${username}")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def userEntry = userService.getEntry(retrieveUrl, UserEntry.class)
//        userEntry.getLogin().setAdmin(true)
//
//        def updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def result = userService.update(updateUrl, userEntry)
//        convertUserEntrytoMap(result)
//    }
//
//    /**
//    * Remove admin privilege for user. Note that executing this method for a user
//    * who is not an admin has no effect.
//    *
//    * @param username The user you wish to remove admin privileges.
//    * @throws AppsForYourDomainException If a Provisioning API specific error
//    *         occurs.
//    * @throws ServiceException If a generic GData framework error occurs.
//    * @throws IOException If an error occurs communicating with the GData
//    *         service.
//    */
//    def removeAdminPrivilege(username) throws AppsForYourDomainException, ServiceException, IOException {
//        log.debug("Removing admin privileges for user ${username}")
//
//        def retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def userEntry = userService.getEntry(retrieveUrl, UserEntry.class)
//        userEntry.getLogin().setAdmin(false)
//
//        def updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username)
//        def result = userService.update(updateUrl, userEntry)
//        convertUserEntrytoMap(result)
//    }
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