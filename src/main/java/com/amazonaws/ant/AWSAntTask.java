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
package com.amazonaws.ant;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.Task;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

/**
 * Base class for AWS-related Ant tasks. Handles all shared logic.
 */
public abstract class AWSAntTask extends Task {

    private static final String CLIENT_CACHE_REFERENCE = "clientCache";
    private static final String USER_AGENT_PREFIX = "AWS Ant Tasks/";
    protected String awsAccessKeyId;
    protected String awsSecretKey;

    /**
     * Sets AWS Access Key.
     * 
     * @param awsAccessKeyId
     *            The AWS Access key to set. Only use for testing or with public
     *            accounts, for a private account use a properties file or
     *            environment variable
     */
    public void setAWSAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    /**
     * Sets AWS Secret Key.
     * 
     * @param awsSecretKey
     *            The AWS Secret key to set. Only use for testing or with public
     *            accounts, for a private account use a credentials file or
     *            environment variable
     */
    public void setAWSSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    
    @SuppressWarnings("unchecked")
    public <T extends AmazonWebServiceClient> T getOrCreateClient(
            Class<T> clientClass) {
        if(getProject().getReference(CLIENT_CACHE_REFERENCE) == null) {
            Map<Class<?>, Object> cache = 
                    new HashMap<Class<?>, Object>();
            getProject().addReference(CLIENT_CACHE_REFERENCE, cache);
        }
        Map<Class<?>, Object> cache =
                getProject().getReference(CLIENT_CACHE_REFERENCE);
        T client = (T) cache.get(clientClass);
        if(client == null) {
            T newClient = createClient(clientClass);
            cache.put(clientClass, newClient);
            return newClient;
        } else {
            return client;
        }
    }
    
    /**
     * Returns a web service client of the specified class. Uses your
     * credentials if they are specified, otherwise the credentials used will be
     * according to the standard credential chain(Environment variables, Java
     * System Properties, credential profiles file, instance profile
     * credentials)
     * 
     * @param clientClass
     *            The class of the web service client returned
     * @return The web service client specified
     */
    public <T extends AmazonWebServiceClient> T createClient(Class<T> clientClass) {
        try {
            ClientConfiguration clientConfiguration = new ClientConfiguration()
                    .withUserAgent(USER_AGENT_PREFIX + this.getClass().getSimpleName());
            if (awsSecretKey != null && awsAccessKeyId != null) {
                Constructor<T> constructor = clientClass.getConstructor(
                        AWSCredentials.class, ClientConfiguration.class);
                return constructor.newInstance(new BasicAWSCredentials(
                        awsAccessKeyId, awsSecretKey), clientConfiguration);
            }
            Constructor<T> constructor = clientClass
                    .getConstructor(ClientConfiguration.class);
            return constructor
                    .newInstance(clientConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create client: "
                    + e.getMessage(), e);
        }
    }
}
