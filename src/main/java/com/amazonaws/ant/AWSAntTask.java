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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringUtils;

/**
 * Base class for AWS-related Ant tasks. Handles all shared logic.
 */
public abstract class AWSAntTask extends Task {

    private static final String CLIENT_CACHE_REFERENCE = "clientCache";
    private static final String USER_AGENT_PREFIX = "AWS Ant Tasks/";
    protected String awsAccessKeyId;
    protected String awsSecretKey;
    protected String awsRegion;

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
    
    /**
     * Sets AWS Region.
     * 
     * @param awsRegion
     *            The AWS Region to set.
     */
    public void setAWSRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends AmazonWebServiceClient> T getOrCreateClient(
            Class<T> clientClass) {
        if(getProject().getReference(CLIENT_CACHE_REFERENCE) == null) {
            Map<List<String>, Object> cache = new HashMap<List<String>, Object>();
            getProject().addReference(CLIENT_CACHE_REFERENCE, cache);
        }
        Map<List<String>, Object> cache = getProject().getReference(CLIENT_CACHE_REFERENCE);
        
        List<String> key = Arrays.asList(clientClass.getName(), this.awsRegion);
        
        T client = (T) cache.get(key);
        if(client == null) {
            T newClient = createClient(clientClass);
            Region region = getRegion();
            if(region != null) {
                newClient.setRegion(region);
            }
            cache.put(key, newClient);
            return newClient;
        } else {
            return client;
        }
    }
    
    private Region getRegion() {
        if(StringUtils.isNullOrEmpty(this.awsRegion)) {
            return null;
        }
        Regions regions = Regions.DEFAULT_REGION;
        try {
            regions = Regions.fromName(this.awsRegion);
        } catch(IllegalArgumentException ex) {
            throw new BuildException("Error in parameter configuration: The specified awsRegion [" + this.awsRegion + "] is not valid\n");
        }
        return Region.getRegion(regions);
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
