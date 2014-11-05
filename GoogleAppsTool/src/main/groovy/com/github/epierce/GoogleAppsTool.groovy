package com.github.epierce

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.oauth2.Oauth2

import com.google.api.services.admin.directory.Directory
import com.google.api.services.admin.directory.model.User
import com.google.api.services.admin.directory.model.Users


class GoogleAppsTool {

    private static final def applicationName = "GoogleAppsTool"
    private static final def dataStoreDir = new File(System.getProperty("user.home"), ".GoogleAppsTool")

    private static FileDataStoreFactory dataStoreFactory
    private static HttpTransport httpTransport
    private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance()
    private static DataStoreCredentialRefreshListener refreshListener
    private static Credential credential

    /** OAuth 2.0 scopes. */
    private static final def SCOPES = [
            "https://www.googleapis.com/auth/admin.directory.group",
            "https://www.googleapis.com/auth/admin.directory.group.member",
            "https://www.googleapis.com/auth/admin.directory.notifications",
            "https://www.googleapis.com/auth/admin.directory.orgunit",
            "https://www.googleapis.com/auth/admin.directory.user",
            "https://www.googleapis.com/auth/admin.directory.user.alias",
            "https://www.googleapis.com/auth/admin.directory.user.security",
            "https://www.googleapis.com/auth/admin.directory.userschema"
    ]

    private static Oauth2 oauth2
    private static GoogleClientSecrets clientSecrets

    public static void main(String[] args) {


        try {

            def opt = getCommandLineOptions(args)

            generateOauthToken()

            def googleAppsClient = new GoogleAppsClient(applicationName, httpTransport, jsonFactory, credential)

            println runAction(googleAppsClient, opt) ?: ' '

        }catch(Exception e) {
             exitOnError e.message
        }

    }

    static void generateOauthToken() {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        dataStoreFactory = new FileDataStoreFactory(dataStoreDir)
        refreshListener = new DataStoreCredentialRefreshListener(applicationName, dataStoreFactory)

        try {
            // load client secrets
            clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(new FileInputStream("/tmp/client_secrets.json")))

            // set up authorization code flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, SCOPES)
                    .setDataStoreFactory(dataStoreFactory)
                    .setApprovalPrompt("force")
                    .setAccessType("offline")
                    .addRefreshListener(refreshListener)
                    .build();

            // authorize
            credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
            oauth2 = new Oauth2.Builder(httpTransport, jsonFactory, credential).setApplicationName(applicationName).build()

        } catch (FileNotFoundException e) {
            println("Enter Client ID and Secret from https://code.google.com/apis/console into /tmp/client_secrets.json")
            System.exit(1)
        }
    }

    /**
     * Parse Command-line options
     **/
    private static getCommandLineOptions(String[] args) {
        def cli = new CliBuilder(
                usage: "GoogleAppsTool [options]",
                header: "\nAvailable options (use -h for help):\n",
                width: 100)

        cli.with {
            h longOpt: 'help', 'usage information', required: false
            u longOpt: 'user', args: 1, argName: 'user', 'GoogleApps user@domain', required: false
            g longOpt: 'givenName', args: 1, argName: 'givenName', 'Given Name', required: false
            f longOpt: 'familyName', args: 1, argName: 'familyName', 'Family Name', required: false
            p longOpt: 'password', args: 1, argName: 'password', 'Password', required: false
            _ longOpt: 'hash', args: 1, argName: 'hash', 'Password Hash Algorithm (MD5 or SHA1)', required: false
            n longOpt: 'newUser', args: 1, argName: 'newUser', 'New user@domain (--rename required)', required: false
            _ longOpt: 'create', 'Create new user', required: false
            _ longOpt: 'rename', 'Change username', required: false
            _ longOpt: 'delete', 'Delete user', required: false
            _ longOpt: 'update', 'Update Given/Family Name', required: false
            _ longOpt: 'lock', 'Lock account', required: false
            _ longOpt: 'unlock', 'Unlock account', required: false
            v longOpt: 'view', 'View user info', required: false

        }

        def options = cli.parse(args)
        if (
        (options.help) ||
                (
                        (!options.create) &&
                        (!options.lock) &&
                        (!options.unlock) &&
                        (!options.update) &&
                        (!options.rename) &&
                        (!options.delete) &&
                        (!options.view)
                )
        ) {
            cli.usage()
            System.exit(0)
        }

        return options
    }

    /**
     * Validate Command-line options
     **/
    private static checkOptions(opt) {
        if ((opt.lock) && (opt.unlock)) throw new IllegalArgumentException('--lock, --unlock are mutually exclusive')

        if ((opt.create) && ((opt.rename) || (opt.delete) || (opt.update))) throw new IllegalArgumentException('--create, --rename, --delete and --update are mutually exclusive')
        if ((opt.rename) && ((opt.create) || (opt.delete) || (opt.update))) throw new IllegalArgumentException('--create, --rename, --delete and --update are mutually exclusive')
        if ((opt.delete) && ((opt.rename) || (opt.create) || (opt.update))) throw new IllegalArgumentException('--create, --rename, --delete and --update are mutually exclusive')
        if ((opt.update) && ((opt.rename) || (opt.delete) || (opt.create))) throw new IllegalArgumentException('--create, --rename, --delete and --update are mutually exclusive')

        if ((opt.rename) && (!opt.newUser)) throw new IllegalArgumentException('--newUser required!')
        if ((opt.delete) && (!opt.user)) throw new IllegalArgumentException('--user required!')
        if ((opt.update) && ((!opt.givenName) || (!opt.familyName))) throw new IllegalArgumentException('--givenName and --familyName required!')
        if ((opt.create) && ((!opt.givenName) || (!opt.familyName) || (!opt.user) || (!opt.password))) throw new IllegalArgumentException('--givenName, --familyName, --user and --password required!')
    }

    private static runAction(googleAppsClient, options) {

        checkOptions(options)

        //View user info
        if(options.view) {
            return googleAppsClient.retrieveUser(options.user).toPrettyString()
            //Create a new user
        } else if (options.create) {
            def result = null

            if (options.hash) {
                result = googleAppsClient.createUser(options.user, options.givenName, options.familyName, options.password, options.hash)
            } else {
                result = googleAppsClient.createUser(options.user, options.givenName, options.familyName, options.password)
            }

            if (result) return "User ${options.user} created."

            //Update a user
        } else if (options.update) {

            def updateMap = [username: options.user, familyName: options.familyName, givenName: options.givenName]

            if (googleAppsClient.updateUser(updateMap)) return "User ${options.user} updated."

            //Rename a user
        } else if (options.rename) {
            def updateMap = [username: options.user, newUsername: options.newUser]

            if (googleAppsClient.updateUser(updateMap)) return "User ${options.user} renamed to ${options.newUser}."

            //Delete a user
        } else if (options.delete) {
            if (googleAppsClient.deleteUser(options.user)) return "User ${options.user} deleted."

            //lock
        } else if (options.lock) {
            if (googleAppsClient.suspendUser(options.user)) return "User ${options.user} suspended."

            //unlock
        } else if (options.unlock) {
            if (googleAppsClient.restoreUser(options.user)) return "User ${options.user} restored."
        }
    }

    private static exitOnError(errorString) {
        println("\nERROR: ${errorString}\n")
        System.exit(1)
    }
}