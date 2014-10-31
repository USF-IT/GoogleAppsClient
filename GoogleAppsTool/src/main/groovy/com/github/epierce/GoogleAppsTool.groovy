package com.github.epierce

import groovy.json.*
import groovy.util.CliBuilder
import org.apache.commons.cli.Option

import com.github.epierce.GoogleAppsClient

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.DataStoreFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.oauth2.Oauth2
import com.google.api.services.oauth2.model.Tokeninfo
import com.google.api.services.oauth2.model.Userinfoplus

class GoogleAppsTool {

    private static final def APPLICATION_NAME = "GoogleAppsTool"
    private static final def DATA_STORE_DIR = new File(System.getProperty("user.home"), ".GoogleAppsTool")

    private static FileDataStoreFactory dataStoreFactory
    private static HttpTransport httpTransport
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance()

    /** OAuth 2.0 scopes. */
    private static final def SCOPES = [
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"]

    private static Oauth2 oauth2
    private static GoogleClientSecrets clientSecrets

    public static void main(String[] args) {


        //   try {

        def opt = getCommandLineOptions(args)
        def config = getConfigSettings(opt)

        if (opt.oauth) {

            generateOauthToken()

        } else {

            def googleAppsClient = createGoogleAppsClient(config)

            println runAction(googleAppsClient, opt) ?: ' '
        }
        /*
         }catch(AppsForYourDomainException e) {
            exitOnError e.errorCode
         }catch(Exception e) {
             exitOnError e.message
         }
         */
    }

    static void generateOauthToken() {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR)

  //      try {
            // load client secrets
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream("/tmp/client_secrets.json")))

            // set up authorization code flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(
                    dataStoreFactory).build();
            // authorize
            def credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")

            oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build()

            def tokeninfo = oauth2.tokeninfo().setAccessToken(credential.getAccessToken()).execute()
            println(tokeninfo.toPrettyString())

        Userinfoplus userinfo = oauth2.userinfo().get().execute();
        System.out.println(userinfo.toPrettyString());


        /*} catch (FileNotFoundException) {
            println("Enter Client ID and Secret from https://code.google.com/apis/console/ into /tmp/client_secrets.json")
            System.exit(1)
        }
*/
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
            u longOpt: 'username', args: 1, argName: 'userName', 'GoogleApps userName', required: false
            g longOpt: 'givenName', args: 1, argName: 'givenName', 'Given Name', required: false
            f longOpt: 'familyName', args: 1, argName: 'familyName', 'Family Name', required: false
            p longOpt: 'password', args: 1, argName: 'password', 'Password', required: false
            _ longOpt: 'hash', args: 1, argName: 'hash', 'Password Hash Algorithm (MD5 or SHA1)', required: false
            n longOpt: 'newUsername', args: 1, argName: 'newUsername', 'New username (--rename required)', required: false
            _ longOpt: 'create', 'Create new user', required: false
            _ longOpt: 'rename', 'Change username', required: false
            _ longOpt: 'delete', 'Delete user', required: false
            _ longOpt: 'update', 'Update Given/Family Name', required: false
            _ longOpt: 'lock', 'Lock account', required: false
            _ longOpt: 'unlock', 'Unlock account', required: false
            c longOpt: 'config', args: 1, argName: 'configFileName', 'groovy config file (default: ~/.GoogleAppsTool.conf)', required: false
            _ longOpt: 'oauth', 'Generate an Oauth login Token (interactive)', required: false

        }

        def options = cli.parse(args)
        if (
        (options.help) ||
                ((!options.create) &&
                        (!options.lock) &&
                        (!options.unlock) &&
                        (!options.update) &&
                        (!options.rename) &&
                        (!options.delete)&&
                        (!options.oauth))
        ) {
            cli.usage()
            System.exit(0)
        }

        return options
    }

    /**
     * Read external config files
     **/
    private static getConfigSettings(options) {
        def config = new ConfigObject()

        config.admin = ""
        config.password = ""
        config.domain = ""

        /** Defaut configuration values can be set in $HOME/.GoogleAppsTool.conf **/
        def defaultConfigFile = new File(System.getProperty("user.home") + '/.GoogleAppsTool.conf')

        if (defaultConfigFile.exists() && defaultConfigFile.canRead()) {
            config = config.merge(new ConfigSlurper().parse(defaultConfigFile.toURL()))
        }

        //Merge the config file that was passed on the commandline
        if (options.config) {
            def newConfigFile = new File(options.config)
            config = config.merge(new ConfigSlurper().parse(newConfigFile.toURL()))
        }

        return config
    }

    /**
     * Create a new GoogleApps Client Object
     **/
    private static createGoogleAppsClient(config) {
        if ((!config.admin) || (!config.password) || (!config.domain)) throw new RuntimeException("Domain admin credentials must be in ~/.GoogleAppsTool.conf or file specified by --config")
        def googleAppsClient = new GoogleAppsClient(config.admin, config.password, config.domain)
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

        if ((opt.rename) && (!opt.newUsername)) throw new IllegalArgumentException('--newUsername required!')
        if ((opt.delete) && (!opt.username)) throw new IllegalArgumentException('--username required!')
        if ((opt.update) && ((!opt.givenName) || (!opt.familyName))) throw new IllegalArgumentException('--givenName and --familyName required!')
        if ((opt.create) && ((!opt.givenName) || (!opt.familyName) || (!opt.username) || (!opt.password))) throw new IllegalArgumentException('--givenName, --familyName, --username and --password required!')
    }

    private static runAction(googleAppsClient, options) {

        checkOptions(options)

        //Create a new user
        if (options.create) {
            def result

            if (options.hash) {
                result = googleAppsClient.createUser(options.username, options.givenName, options.familyName, options.password, options.hash)
            } else {
                result = googleAppsClient.createUser(options.username, options.givenName, options.familyName, options.password)
            }

            if (result) return "User ${options.username} created."

            //Update a user
        } else if (options.update) {

            def updateMap = [username: options.username, familyName: options.familyName, givenName: options.givenName]

            if (googleAppsClient.updateUser(updateMap)) return "User ${options.username} updated."

            //Rename a user
        } else if (options.rename) {
            def updateMap = [username: options.username, newUsername: options.newUsername]

            if (googleAppsClient.updateUser(updateMap)) return "User ${options.username} renamed to ${options.newUsername}."

            //Delete a user
        } else if (options.delete) {
            if (googleAppsClient.deleteUser(options.username)) return "User ${options.username} deleted."

            //lock
        } else if (options.lock) {
            if (googleAppsClient.suspendUser(options.username)) return "User ${options.username} suspended."

            //unlock
        } else if (options.unlock) {
            if (googleAppsClient.restoreUser(options.username)) return "User ${options.username} restored."
        }
    }

    private static exitOnError(errorString) {
        println("\nERROR: ${errorString}\n")
        System.exit(1)
    }
}