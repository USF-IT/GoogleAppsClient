package com.github.epierce

import groovy.json.*
import groovy.util.CliBuilder
import org.apache.commons.cli.Option

import com.github.epierce.GoogleAppsClient
import com.google.gdata.data.appsforyourdomain.AppsForYourDomainException

class GoogleAppsTool {
    public static void main(String[] args) {

        try {           

            def opt = getCommandLineOptions(args)
            def config = getConfigSettings(opt)
        
            def googleAppsClient = createGoogleAppsClient(config)

            println runAction(googleAppsClient,opt) ?: ' '
        
        }catch(AppsForYourDomainException e) {
           exitOnError e.errorCode
        }catch(Exception e) {
            exitOnError e.message
        }
    }

    /**
    * Parse Command-line options
    **/
    private static getCommandLineOptions(String[] args){
        def cli = new CliBuilder(
                        usage:"GoogleAppsTool [options]",
                        header:"\nAvailable options (use -h for help):\n",
                        width:100)

        cli.with {
            h longOpt:'help', 'usage information', required: false 
            u longOpt:'username', args:1, argName:'userName', 'GoogleApps userName', required: false
            g longOpt:'givenName', args:1, argName:'givenName', 'Given Name', required: false
            f longOpt:'familyName', args:1, argName:'familyName', 'Family Name', required: false
            p longOpt:'password', args:1, argName:'password', 'Password', required: false
            _ longOpt:'hash', args:1, argName:'hash', 'Password Hash Algorithm (MD5 or SHA1)', required: false
            n longOpt:'newUsername', args:1, argName:'newUsername', 'New username (--rename required)', required: false
            _ longOpt:'create', 'Create new user', required: false
            _ longOpt:'rename', 'Change username', required: false
            _ longOpt:'delete', 'Delete user', required: false
            _ longOpt:'update', 'Update Given/Family Name', required: false
            _ longOpt:'lock', 'Lock account', required: false
            _ longOpt:'unlock', 'Unlock account', required: false
            c longOpt:'config', args:1, argName:'configFileName', 'groovy config file (default: ~/.GoogleAppsTool.conf)', required: false
        }

        def options = cli.parse(args)
        if( 
            (options.help) || 
            ((! options.create) && 
            (! options.lock) && 
            (! options.unlock) && 
            (! options.update) && 
            (! options.rename) && 
            (! options.delete)) 
        ){
            cli.usage() 
            System.exit(0)
        }

        return options
    }

    /**
    * Read external config files
    **/
    private static getConfigSettings(options){
        def config = new ConfigObject()

        config.admin = ""
        config.password = ""
        config.domain = ""       

        /** Defaut configuration values can be set in $HOME/.GoogleAppsTool.conf **/                   
        def defaultConfigFile = new File(System.getProperty("user.home")+'/.GoogleAppsTool.conf')

        if (defaultConfigFile.exists() && defaultConfigFile.canRead()) {
            config = config.merge(new ConfigSlurper().parse(defaultConfigFile.toURL()))
        }

        //Merge the config file that was passed on the commandline
        if(options.config){
            def newConfigFile = new File(options.config)
            config = config.merge(new ConfigSlurper().parse(newConfigFile.toURL()))
        }

        return config
    }

    /**
    * Create a new GoogleApps Client Object
    **/
    private static createGoogleAppsClient(config){
        if ((!config.admin)||(!config.password)||(!config.domain)) throw new RuntimeException("Domain admin credentials must be in ~/.GoogleAppsTool.conf or file specified by --config")
        def googleAppsClient = new GoogleAppsClient(config.admin, config.password, config.domain)
    }

    /**
    * Validate Command-line options
    **/
    private static checkOptions(opt){
        if((opt.lock)&&(opt.unlock)) throw new IllegalArgumentException('--lock, --unlock are mutually exclusive')

        if((opt.create)&&((opt.rename) || (opt.delete) || (opt.update))) throw new IllegalArgumentException('--create, --rename, --delete and --update are mutually exclusive')
        if((opt.rename)&&((opt.create) || (opt.delete) || (opt.update))) throw new IllegalArgumentException('--create, --rename, --delete and --update are mutually exclusive')
        if((opt.delete)&&((opt.rename) || (opt.create) || (opt.update))) throw new IllegalArgumentException('--create, --rename, --delete and --update are mutually exclusive')
        if((opt.update)&&((opt.rename) || (opt.delete) || (opt.create))) throw new IllegalArgumentException('--create, --rename, --delete and --update are mutually exclusive')

        if((opt.rename)&&(!opt.newUsername)) throw new IllegalArgumentException('--newUsername required!')
        if((opt.delete)&&(!opt.username)) throw new IllegalArgumentException('--username required!')
        if((opt.update)&&((!opt.givenName)||(!opt.familyName))) throw new IllegalArgumentException('--givenName and --familyName required!')
        if((opt.create)&&((!opt.givenName)||(!opt.familyName)||(!opt.username)||(!opt.password))) throw new IllegalArgumentException('--givenName, --familyName, --username and --password required!')
    }

    private static runAction(googleAppsClient,options) {
        
        checkOptions(options)

        //Create a new user
        if(options.create){
            def result         

            if(options.hash) {
                result = googleAppsClient.createUser(options.username, options.givenName, options.familyName, options.password, options.hash)
            } else {
                result = googleAppsClient.createUser(options.username, options.givenName, options.familyName, options.password)
            }

            if (result) return "User ${options.username} created."  

        //Update a user
        } else if (options.update){

            def updateMap = [username: options.username, familyName: options.familyName, givenName: options.givenName]

            if( googleAppsClient.updateUser(updateMap) ) return "User ${options.username} updated."

        //Rename a user
        } else if (options.rename){
            def updateMap = [username: options.username, newUsername: options.newUsername]

            if( googleAppsClient.updateUser(updateMap) ) return "User ${options.username} renamed to ${options.newUsername}."

        //Delete a user
        } else if (options.delete) {
            if( googleAppsClient.deleteUser(options.username) ) return "User ${options.username} deleted."
        
        //lock
        } else if (options.lock) {
            if( googleAppsClient.suspendUser(options.username) ) return "User ${options.username} suspended."

        //unlock
        } else if (options.unlock) {
            if( googleAppsClient.restoreUser(options.username) ) return "User ${options.username} restored."
        }
    }

    private static exitOnError(errorString){
        println("\nERROR: ${errorString}\n")
        System.exit(1)
    }
}