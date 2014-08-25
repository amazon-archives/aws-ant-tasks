/*
 * Copyright 2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.ant.opsworks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.AppType;
import com.amazonaws.services.opsworks.model.DataSource;
import com.amazonaws.services.opsworks.model.Source;
import com.amazonaws.services.opsworks.model.SslConfiguration;
import com.amazonaws.services.opsworks.model.UpdateAppRequest;

public class UpdateAppTask extends AWSAntTask {
    private String appId;
    private String name;
    private String type;
    private String description;
    private String repoSshKey;
    private String repoType;
    private String repoUrl;
    private String repoUsername;
    private String repoPassword;
    private String repoRevision;
    private String sslCertificate;
    private String sslPrivateKey;
    private String sslChain;
    private boolean enableSsl;
    private boolean useAwsKeysForRepo;
    private List<DataSource> dataSources = new LinkedList<DataSource>();
    private List<String> domains = new LinkedList<String>();
    private Map<String, String> attributes = new HashMap<String, String>();

    /**
     * The ID of the app to update. This parameter is required. You can find the
     * ID of your app in the console. If you set the appId property, you do not
     * need to set this parameter--it will automatically look for the "appId"
     * parameter. If you created an app or apps earlier in the project, use a
     * property such as "${appId1}", "${appId2}", etc., depending on which app
     * was created in which order.
     * 
     * @param appId
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Set the app name.
     * 
     * @param name
     *            The app name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the app type.
     * 
     * @param type
     *            The app type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Set a description for the app.
     * 
     * @param description
     *            A description for the app.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set the SSH key for your app repository. Required if you need such a key
     * to access your repository.
     * 
     * @param repoSshKey
     *            The SSH key for your app repository.
     */
    public void setRepoSshKey(String repoSshKey) {
        this.repoSshKey = repoSshKey;
    }

    /**
     * Set the type of the repository your app is stored in (S3, github, etc).
     * Not required.
     * 
     * @param repoType
     *            The type of the repository your app is stored in.
     */
    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    /**
     * Set the URL in your repository where the source of your app can be
     * accessed. Not required.
     * 
     * @param repoUrl
     *            The URL leading to the source of your app.
     */
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    /**
     * Set the username to access your repository. Whether it is required
     * depends on the type of repository (For an S3 repository, use your AWS
     * Access Key)
     * 
     * @param repoUsername
     *            Username needed to access your repository
     */
    public void setRepoUsername(String repoUsername) {
        this.repoUsername = repoUsername;
    }

    /**
     * Set the password to access your repository. Whether it is required
     * depends on the type of repository (For an S3 repository, use your AWS
     * Secret key)
     * 
     * @param repoPassword
     *            Password needed to access your repository
     */
    public void setRepoPassword(String repoPassword) {
        this.repoPassword = repoPassword;
    }

    /**
     * Set the revision/branch, etc of your repository. Whether it is required
     * depends on the type of repository
     * 
     * @param repoRevision
     *            The revision of the source to use.
     */
    public void setRepoRevision(String repoRevision) {
        this.repoRevision = repoRevision;
    }

    /**
     * Set the SSL certification for the SSL configuration of this app. Not
     * required. Can only be set if enableSsl is true.
     * 
     * @param sslCertificate
     *            The SSL certificate for the SSL configuration of this app.
     */
    public void setSslCertificate(String sslCertificate) {
        this.sslCertificate = sslCertificate;
    }

    /**
     * Set the private key for the SSL configuration of this app. Not required.
     * Can only be set if enableSsl is true.
     * 
     * @param sslPrivateKey
     *            the privatey key for the SSL configuration of this app.
     */
    public void setSslPrivateKey(String sslPrivateKey) {
        this.sslPrivateKey = sslPrivateKey;
    }

    /**
     * Set the chain for the SSL configuration of this app. Not required. Can
     * only be set if enableSsl is true.
     * 
     * @param sslChain
     *            The chain for the SSL configuration of this app.
     */
    public void setsslChain(String sslChain) {
        this.sslChain = sslChain;
    }

    /**
     * Set whether to enable SSL for this app. Not required, defaults to false.
     * 
     * @param enableSsl
     *            Whether to enable SSL for this app.
     */
    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    /**
     * Whether to use your AWS Secret Key and Access Key for your
     * username/password in your repo credentials. Enable this to use the
     * default credential chain to set your repo username and password, so you
     * don't have to expose you secret key and access key.
     * 
     * @param useAwsKeysForRepo
     *            Whether to use the default credential chain to set
     *            repoUsername and repoPassword.
     */
    public void setUseAwsKeysForRepo(boolean useAwsKeysForRepo) {
        this.useAwsKeysForRepo = useAwsKeysForRepo;
    }

    /**
     * Allows you to add any number of preconfigured nested Datasource elements.
     * 
     * @param datasource
     *            A preconfigured nested Datasource object to add.
     */
    public void addConfiguredDatasource(Datasource datasource) {
        dataSources.add(new DataSource().withArn(datasource.getArn())
                .withDatabaseName(datasource.getDatabaseName())
                .withType(datasource.getType()));
    }

    /**
     * Allows you to add any number of preconfigured Domain elements.
     * 
     * @param domain
     *            A preconfigured Domain object to add
     */
    public void addConfiguredDomain(Domain domain) {
        domains.add(domain.getValue());
    }

    /**
     * Allows you to add any number of preconfigured AppAttribute elements.
     * 
     * @param domain
     *            A preconfigured AppAttribute object to add
     */
    public void addConfiguredAppAttribute(AppAttribute appAttribute) {
        attributes.put(appAttribute.getKey(), appAttribute.getValue());
    }

    private void checkParams() {
        StringBuilder errors = new StringBuilder("");
        boolean areMalformedParams = false;

        if (useAwsKeysForRepo) {
            setRepoUsername(new DefaultAWSCredentialsProviderChain()
                    .getCredentials().getAWSAccessKeyId());
            setRepoPassword(new DefaultAWSCredentialsProviderChain()
                    .getCredentials().getAWSSecretKey());
        }

        if (appId == null) {
            if (!Boolean.TRUE.equals(getProject().getReference(Constants.APP_ID_REFERENCE))) {
                appId = getProject().getProperty(Constants.APP_ID_PROPERTY);
            }            if (appId == null) {
                areMalformedParams = true;
                errors.append("Missing parameter: appId is required \n");
            } else {
                System.out.println("Using " + Constants.APP_ID_PROPERTY
                        + " property as appId");
            }
        }
        if (!areMalformedParams) {
            try {
                AppType.valueOf(type);
            } catch (IllegalArgumentException e) {
                System.out
                        .println("Warning: It seems that the app type "
                                + type
                                + " is not in our model. If this build fails, this may be why.");
            }
        }
        if (!enableSsl
                && (sslCertificate != null && sslChain != null && sslPrivateKey != null)) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: you cannot specify ssl elements if enableSsl is false. \n");
        }
        if (!(repoType == null && repoUrl == null)
                && (repoType == null || repoUrl == null)) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: if one of repoType, repoUrl is set, they most both be set \n");
        }
        if (!(repoUsername == null && repoPassword == null)
                && (repoUsername == null || repoPassword == null)) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: if one of repoUsername, repoPassword is set, they most both be set \n");
        }
        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AWSOpsWorksClient client = createClient(AWSOpsWorksClient.class);
        UpdateAppRequest updateAppRequest = new UpdateAppRequest()
                .withName(name).withType(type).withEnableSsl(enableSsl)
                .withAppId(appId).withDescription(description)
                .withDataSources(dataSources).withDomains(domains)
                .withAttributes(attributes);
        if (repoType != null && repoUrl != null) {
            Source appSource = new Source().withType(repoType).withUrl(repoUrl)
                    .withSshKey(repoSshKey).withRevision(repoRevision)
                    .withPassword(repoPassword).withUsername(repoUsername);
            updateAppRequest.setAppSource(appSource);
        }
        if (enableSsl) {
            if (sslCertificate != null && sslChain != null
                    && sslPrivateKey != null) {
                SslConfiguration sslConfiguration = new SslConfiguration()
                        .withCertificate(sslCertificate).withChain(sslChain)
                        .withPrivateKey(sslPrivateKey);
                updateAppRequest.setSslConfiguration(sslConfiguration);
            }
        }
        try {
            client.updateApp(updateAppRequest);
            System.out.println("Update app request submitted. ");
        } catch (Exception e) {
            throw new BuildException("Could not update app: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Nested class for specifying a data source for your app
     */
    public static class Datasource {
        private String type;
        private String arn;
        private String databaseName;

        /**
         * Get the data source type
         * 
         * @return The data source type
         */
        public String getType() {
            return type;
        }

        /**
         * Set the data source type
         * 
         * @param type
         *            The data source type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Get the arn of the data source
         * 
         * @return The data source arn
         */
        public String getArn() {
            return arn;
        }

        /**
         * Set the arn of the data source
         * 
         * @param arn
         *            The data source arn
         */
        public void setArn(String arn) {
            this.arn = arn;
        }

        /**
         * Get the name of the database used by this data source
         * 
         * @return The name of this data source's database
         */
        public String getDatabaseName() {
            return databaseName;
        }

        /**
         * Set the name of the database to use in this data source
         * 
         * @param databaseName
         *            The name of the database to use in this data source
         */
        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public Datasource() {
            // required by Ant
        }
    }

    /**
     * Nested element for specifying any number of custom domains
     * 
     * @author jesduart
     * 
     */
    public static class Domain {
        private String value;

        /**
         * Get the domain
         * 
         * @return The domain
         */
        public String getValue() {
            return value;
        }

        /**
         * Set the domain to use
         * 
         * @param value
         *            The domain to use
         */
        public void setValue(String value) {
            this.value = value;
        }

        public Domain() {
            // required by Ant
        }
    }

    /**
     * Container class to use as a nested element. Allows you to specify any
     * number of attributes(key-value pairs) to associate with this app
     * 
     * @author jesduart
     * 
     */
    public static class AppAttribute {
        private String key;
        private String value;

        /**
         * Get the key of this attribute
         * 
         * @return The key of this attribute
         */
        public String getKey() {
            return key;
        }

        /**
         * Set the key of this attribute
         * 
         * @param key
         *            The key of this attribute
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * Get the value of this attribute
         * 
         * @return The value of this attribute
         */
        public String getValue() {
            return value;
        }

        /**
         * Set the value of this attribute
         * 
         * @param value
         *            The value of this attribute
         */
        public void setValue(String value) {
            this.value = value;
        }

        public AppAttribute() {
            // required by Ant
        }
    }

}
