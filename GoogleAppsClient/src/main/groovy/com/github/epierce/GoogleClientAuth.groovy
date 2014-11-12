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

/**
 * Created by epierce on 11/10/14.
 */
class GoogleClientAuth {

    private static def applicationName
    private static def secretsFileStream

    private static FileDataStoreFactory dataStoreFactory
    private static HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport()
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


    GoogleClientAuth( appName, dataStore, secretsFile) {
        applicationName  = appName
        dataStoreFactory = new FileDataStoreFactory(new File(dataStore))
        secretsFileStream = new FileInputStream(secretsFile)
        refreshListener  = new DataStoreCredentialRefreshListener(applicationName, dataStoreFactory)

        // load client secrets
        clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(secretsFileStream))

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

        return

    }

    def getApplicationName() {
        return applicationName
    }

    def getDataStoreFactory(){
        return dataStoreFactory
    }

    def getHttpTransport(){
        return httpTransport
    }

    def getJsonFactory() {
        return jsonFactory
    }

    def getCredential(){
        return credential
    }
}
