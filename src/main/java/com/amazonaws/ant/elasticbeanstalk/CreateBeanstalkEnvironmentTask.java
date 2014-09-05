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
package com.amazonaws.ant.elasticbeanstalk;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult;
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier;

/**
 * Task for creating an AWS Elastic Beanstalk environment
 */
public class CreateBeanstalkEnvironmentTask extends AWSAntTask {

    private String applicationName;
    private String versionLabel;
    private String cnamePrefix;
    private String environmentName;
    private String environmentDescription;
    private String solutionStackName;
    private String tierName;
    private String tierType;
    private String tierVersion;
    private Vector<Setting> settings = new Vector<Setting>();

    /**
     * Specify the application to associate with this environment. Required.
     * 
     * @param applicationName
     *            The name of the application to associate with this environment
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Specify the version to associate with this environment. Not required.
     * 
     * @param versionLabel
     *            the label of the version to associate with this environment
     */
    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    /**
     * Specify the CNAME prefix of this application. Must be between 4-23
     * characters, contain only letters, numbers, and hypens, and not begin or
     * end with a hyphen. Not required.
     * 
     * @param cnameprefix
     *            The CNAME of this application; will be used in the application
     *            URL
     */
    public void setCnamePrefix(String cnamePrefix) {
        this.cnamePrefix = cnamePrefix;
    }

    /**
     * Specify the name of this environment. Required.
     * 
     * @param environmentName
     *            The name of this environment
     */
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    /**
     * Specify the description of this environment. Required
     * 
     * @param environmentDescription
     *            The description of this environment
     */
    public void setEnvironmentDescription(String environmentDescription) {
        this.environmentDescription = environmentDescription;
    }

    /**
     * Specify the solution stack of this environment. Required.
     * 
     * @param solutionStackName
     *            The solution stack name of this environment
     */
    public void setSolutionStackName(String solutionStackName) {
        this.solutionStackName = solutionStackName;
    }

    /**
     * The name of the tier of this environment. Conditionally required; if you
     * are specifying a tier, then tierType, tierName, and tierVersion must all
     * be set.
     * 
     * @param tier
     *            The tier name of this environment
     */
    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    /**
     * The version of the tier of this environment.Conditionally required; if
     * you are specifying a tier, then tierType, tierName, and tierVersion must
     * all be set.
     * 
     * @param tierVersion
     *            The tier version of this environment
     */
    public void setTierVersion(String tierVersion) {
        this.tierVersion = tierVersion;
    }

    /**
     * Specify the type of the tier. Conditionally required; if you are
     * specifying a tier, then tierType, tierName, and tierVersion must all be
     * set.
     * 
     * @param tierType
     *            The type of the this environments tier
     */
    public void setTierType(String tierType) {
        this.tierType = tierType;
    }

    /**
     * Adds an option setting for this environment only after it has been
     * configured within an Ant call
     * 
     * @param setting
     *            The preconfigured option setting to add
     */
    public void addConfiguredSetting(Setting setting) {
        settings.add(setting);
    }

    private void checkParams() {
        StringBuilder errors = new StringBuilder("");
        boolean areMissingParams = false;

        if (environmentName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: environmentName is required \n");
        }
        if (environmentDescription == null) {
            areMissingParams = true;
            errors.append("Missing parameter: environmentDescription is required \n");
        }
        if (solutionStackName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: solutionStackName is required \n");
        }
        if (applicationName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: applicationName is required \n");
        }
        if (!(tierName == null && tierType == null && tierVersion == null)
                && (tierName == null || tierType == null || tierVersion == null)) {
            areMissingParams = true;
            errors.append("Invalid paramater configuration: If one of tierName, tierType, and tierVersion is set, then they must all be set. \n");
        }
        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AWSElasticBeanstalkClient client = getOrCreateClient(AWSElasticBeanstalkClient.class);
        CreateEnvironmentRequest eRequest = new CreateEnvironmentRequest(
                applicationName, environmentName)
                .withDescription(environmentDescription)
                .withVersionLabel(versionLabel)
                .withSolutionStackName(solutionStackName);
        if (!(tierName == null || tierType == null || tierVersion == null)) {
            eRequest.setTier(new EnvironmentTier().withName(tierName)
                    .withType(tierType).withVersion(tierVersion));
        }

        if (cnamePrefix != null) {
            CheckDNSAvailabilityResult dnsResult = client
                    .checkDNSAvailability(new CheckDNSAvailabilityRequest(
                            cnamePrefix));
            if (!dnsResult.isAvailable()) {
                throw new BuildException("The specified CNAME " + cnamePrefix
                        + " was not available");
            }
            eRequest.setCNAMEPrefix(cnamePrefix);
        }
        List<ConfigurationOptionSetting> optionSettings = new LinkedList<ConfigurationOptionSetting>();
        for (Setting setting : settings) {
            optionSettings.add(new ConfigurationOptionSetting(setting
                    .getNamespace(), setting.getOptionName(), setting
                    .getValue()));
        }
        if (optionSettings.size() > 0) {
            eRequest.setOptionSettings(optionSettings);
        }
        System.out.println("Creating environment " + environmentName + "...");
        String cNAME = "";
        try {
            CreateEnvironmentResult result = client.createEnvironment(eRequest);
            if ((cNAME = result.getCNAME()) == null) {
                System.out
                        .println("Create environment request submitted. The environment configuration does not support a CNAME.");
            } else {
                System.out
                        .println("Create environment request submitted. When the environment is finished launching, your deployment will be available at "
                                + cNAME);
            }
        } catch (Exception e) {
            throw new BuildException(
                    "Exception while attempting to create environment: "
                            + e.getMessage(), e);
        }

    }

    /**
     * Wrapper for an environment option setting, to be used as a nested element
     */
    public static class Setting {
        private String namespace;
        private String optionName;
        private String value;

        public Setting() {
            // required by Ant
        }

        public String getNamespace() {
            return namespace;
        }

        /**
         * specify namespace of this option setting. Required.
         * 
         * @param namespace
         *            The namespace of this option setting
         */
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getOptionName() {
            return optionName;
        }

        /**
         * Specify the option name of this option setting. Required.
         * 
         * @param optionName
         *            The name of the option to set.
         */
        public void setOptionName(String optionName) {
            this.optionName = optionName;
        }

        public String getValue() {
            return value;
        }

        /**
         * Specify the value of this option setting. Required.
         * 
         * @param value
         *            The value for this option setting
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
}
