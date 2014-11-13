package com.github.epierce

class GoogleAppsTool {
    public static void main(String[] args) {


        try {

            def opt = getCommandLineOptions(args)

            def configDir = System.getProperty("user.home")+"/.GoogleAppsTool"

            def googleClientAuth = new GoogleClientAuth("GoogleAppsTool", configDir , "${configDir}/client_secrets.json")

            def googleAppsClient = new GoogleAppsClient(
                    googleClientAuth.getApplicationName(),
                    googleClientAuth.getHttpTransport(),
                    googleClientAuth.getJsonFactory(),
                    googleClientAuth.getCredential())

            runAction(googleAppsClient, opt)

        }catch(Exception e) {
             exitOnError e.message
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
            _ longOpt: 'alias', args: 1, argName: 'newAlias', 'New user@domain alias (--add-alias required)', required: false
            _ longOpt: 'addAlias', 'Add new alias', required: false
            _ longOpt: 'deleteAlias', 'Delete alias', required: false
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
                        (!options.view) &&
                        (!options.addAlias) &&
                        (!options.deleteAlias)
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

        if ((opt.hash) && ((opt.hash != 'MD5') && (opt.hash != 'SHA-1'))) throw new IllegalArgumentException('"MD5" and "SHA-1" are the only valid hash functions')

        if ((opt.rename) && (!opt.newUser)) throw new IllegalArgumentException('--newUser required!')
        if (((opt.addAlias) || (opt.deleteAlias)) && (!opt.alias)) throw new IllegalArgumentException('--alias required!')
        if ((opt.delete) && (!opt.user)) throw new IllegalArgumentException('--user required!')
        if ((opt.update) && ((!opt.givenName) || (!opt.familyName))) throw new IllegalArgumentException('--givenName and --familyName required!')
        if ((opt.create) && ((!opt.givenName) || (!opt.familyName) || (!opt.user) || (!opt.password))) throw new IllegalArgumentException('--givenName, --familyName, --user and --password required!')

        //Account identifiers are email addresses, not usernames
        if ((opt.user) && (! "${opt.user}".contains('@'))) throw new IllegalArgumentException('Invalid user identifier. user@domain required.')

    }

    private static runAction(googleAppsClient, options) {

        checkOptions(options)

        //View user info
        if(options.view) {
            println "User Info:"
            println googleAppsClient.retrieveUser(options.user).toPrettyString()

            println "Email Aliases:"
            def aliases = googleAppsClient.retrieveAliases(options.user).getAliases()
            aliases.each { alias ->
                println alias.toPrettyString()
            }

        //Create a new user
        } else if (options.create) {
            def result = null

            if (options.hash) {
                result = googleAppsClient.createUser(options.user, options.givenName, options.familyName, options.password, options.hash)
            } else {
                result = googleAppsClient.createUser(options.user, options.givenName, options.familyName, options.password)
            }

            println result.toPrettyString()

        //Create a new alias
        } else if (options.addAlias) {
            println googleAppsClient.createAlias(options.user, options.alias).toPrettyString()

        //Delete an alias
        } else if (options.deleteAlias) {
            googleAppsClient.deleteAlias(options.user, options.alias).toPrettyString()
            println "Deleted"

        //Update a user
        } else if (options.update) {

            def updateMap = [   username: options.user,
                                familyName: options.familyName,
                                givenName: options.givenName
            ]

            if (options.hash && options.password) {
                updateMap.passwordHashFunction = options.hash
                updateMap.password= options.password
            }

            println googleAppsClient.updateUser(updateMap).toPrettyString()

        //Rename a user
        } else if (options.rename) {
            def updateMap = [username: options.user, newUsername: options.newUser]

            println googleAppsClient.updateUser(updateMap).toPrettyString()

        //Delete a user
        } else if (options.delete) {

            googleAppsClient.deleteUser(options.user).toPrettyString()
            println "Deleted"

        //lock
        } else if (options.lock) {
            println googleAppsClient.suspendUser(options.user).toPrettyString()

        //unlock
        } else if (options.unlock) {
            println googleAppsClient.restoreUser(options.user).toPrettyString()
        }
    }

    private static exitOnError(errorString) {
        println("\nERROR: ${errorString}\n")
        System.exit(1)
    }
}