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
import java.util.Map;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.ant.KeyValueNestedElement;
import com.amazonaws.services.opsworks.model.ChefConfiguration;
import com.amazonaws.services.opsworks.model.CreateStackRequest;
import com.amazonaws.services.opsworks.model.Source;
import com.amazonaws.services.opsworks.model.StackConfigurationManager;
import com.amazonaws.services.opsworks.model.StartStackRequest;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;

public class CreateStackTask extends AWSAntTask {

    private static final String CHEF = "Chef";
    private Map<String, String> attributes = new HashMap<String, String>();
    private String vpcId;
    private String name;
    private String region;
    private String defaultAvailabilityZone;
    private String defaultOs;
    private String defaultRootDeviceType;
    private String defaultInstanceProfileArn;
    private String serviceRoleArn;
    private String defaultSshKeyName;
    private String hostnameTheme;
    private String chefVersion;
    private String customJson;
    private String repoSshKey;
    private String repoType;
    private String repoUrl;
    private String repoUsername;
    private String repoPassword;
    private String repoRevision;
    private String berkshelfVersion;
    private String propertyNameForStackId = Constants.STACK_ID_PROPERTY;
    private boolean useCustomCookbooks = false;
    private boolean useOpsworksSecurityGroups = true;
    private boolean manageBerkshelf;
    private boolean startOnCreate = true;

    /**
     * Allows you to add any number of attributes (Key-value pairs) to associate
     * with this stack
     * 
     * @param attribute
     *            A preconfigured attribute object to add
     */
    public void addConfiguredStackAttribute(StackAttribute attribute) {
        attributes.put(attribute.getKey(), attribute.getValue());
    }

    /**
     * Set the ID of the VPC the stack will be launched into. Not required.
     * 
     * @param vpcId
     *            The ID of the VPC the stack will be launched into
     */
    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    /**
     * Set the name of this stack. Required.
     * 
     * @param name
     *            The name of this stack.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the region of this stack. Required.
     * 
     * @param region
     *            The region of this stack.
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Set the stack's default availability zone. Note that it must be in the
     * specified region. Not required.
     * 
     * @param defaultAvailabilityZone
     *            The stack's default availability zone.
     */
    public void setDefaultAvailabilityZone(String defaultAvailabilityZone) {
        this.defaultAvailabilityZone = defaultAvailabilityZone;
    }

    /**
     * Set the default operating system. This must be set to Amazon Linux or
     * Ubuntu 12.04 LTS. Not required.
     * 
     * @param defaultOs
     *            The default operating system.
     */
    public void setDefaultOs(String defaultOs) {
        this.defaultOs = defaultOs;
    }

    /**
     * Set the default root device type of this stack. Not required.
     * 
     * @param defaultRootDeviceType
     *            The default root device type of this stack.
     */
    public void setDefaultRootDeviceType(String defaultRootDeviceType) {
        this.defaultAvailabilityZone = defaultRootDeviceType;
    }

    /**
     * Set the IAM role to be the default profile for all of this stack's EC2
     * instances. Must be a valid instance profile that you have access to.
     * Required.
     * 
     * @param defaultInstanceProfileArn
     *            The ARN of the IAM role to be the default profile for all of
     *            this stack's EC2 instances.
     */
    public void setDefaultInstanceProfileArn(String defaultInstanceProfileArn) {
        this.defaultInstanceProfileArn = defaultInstanceProfileArn;
    }

    /**
     * Set the default SSH key to use for the stack's instances. Not required.
     * 
     * @param defaultSshKeyName
     *            A default SSH key to use for the stack's instances.
     */
    public void setDefaultSshKeyName(String defaultSshKeyName) {
        this.defaultSshKeyName = defaultSshKeyName;
    }

    /**
     * Set this stack's host name theme, with spaces replaced by underscores.
     * 
     * @param hostnameTheme
     *            This stack's host name theme
     */
    public void setHostnameTheme(String hostnameTheme) {
        this.hostnameTheme = hostnameTheme;
    }

    /**
     * Set the version of Chef to use. Not required, default is 11.4
     * 
     * @param chefVersion
     *            The version of Chef to use.
     */
    public void setChefVersion(String chefVersion) {
        this.chefVersion = chefVersion;
    }

    /**
     * Set custom JSON to use to override the default corresponding stack
     * configuration values. Must be correctly formed and properly escaped.
     * 
     * @param customJson
     *            A string of user-defined custom JSON.
     */
    public void setCustomJson(String customJson) {
        this.customJson = customJson;
    }

    /**
     * Set the version of Berkshelf to use. Not required.
     * 
     * @param berkshelfVersion
     *            The version of Berkshelf.
     */
    public void setBerkshelfVersion(String berkshelfVersion) {
        this.berkshelfVersion = berkshelfVersion;
    }

    /**
     * Set whether to associate the OpsWorks built-in security groups with this
     * stack's layers. Not required.
     * 
     * @param useOpsworksSecurityGroups
     *            Whether to associate the OpsWorks built-in security groups
     *            with this stack's layers
     */
    public void setUseOpsworksSecurityGroups(boolean useOpsworksSecurityGroups) {
        this.useOpsworksSecurityGroups = useOpsworksSecurityGroups;
    }

    /**
     * Set whether this stack will use custom cookbooks. Not required.
     * 
     * @param useCustomCookbooks
     *            Whether this stack will use custom cookbooks.
     */
    public void setUseCustomCookbooks(boolean useCustomCookbooks) {
        this.useCustomCookbooks = useCustomCookbooks;
    }

    /**
     * Set whether to enable Berkshelf. Not required.
     * 
     * @param manageBerkshelf
     *            Whether to enable Berkshelf.
     */
    public void setManageBerkshelf(boolean manageBerkshelf) {
        this.manageBerkshelf = manageBerkshelf;
    }

    /**
     * Set the IAM role which will allow OpsWorks to access AWS resources on
     * your behalf. Must be a valid role with proper access permissions that you
     * have access to. Required.
     * 
     * @param serviceRoleArn
     *            The ARN of the IAM role which will allow OpsWorks to access
     *            AWS resources on your behalf.
     */
    public void setServiceRoleArn(String serviceRoleArn) {
        this.serviceRoleArn = serviceRoleArn;
    }

    /**
     * Set the SSH key for your cookbook repository. Required if you need such a
     * key to access your repository.
     * 
     * @param repoSshKey
     *            The SSH key for your cookbook repository.
     */
    public void setRepoSshKey(String repoSshKey) {
        this.repoSshKey = repoSshKey;
    }

    /**
     * Set the type of the repository your cookbook is stored in (S3, github,
     * etc). Not required.
     * 
     * @param repoType
     *            The type of the repository your cookbook is stored in.
     */
    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    /**
     * Set the URL in your repository where the source of your cookbook can be
     * accessed. Not required.
     * 
     * @param repoUrl
     *            The URL leading to the source of your cookbook.
     */
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    /**
     * Set the username to access your cookbook repository. Whether it is
     * required depends on the type of repository (For an S3 repository, use
     * your AWS Access Key)
     * 
     * @param repoUsername
     *            Username needed to access your cookbook repository
     */
    public void setRepoUsername(String repoUsername) {
        this.repoUsername = repoUsername;
    }

    /**
     * Set the password to access your cookbook repository. Whether it is
     * required depends on the type of repository (For an S3 repository, use
     * your AWS Secret key)
     * 
     * @param repoPassword
     *            Password need to access your cookbook repository
     */
    public void setRepoPassword(String repoPassword) {
        this.repoPassword = repoPassword;
    }

    /**
     * Set the revision/branch, etc of your cookbook repository. Whether it is
     * required depends on the type of repository
     * 
     * @param repoRevision
     *            The revision of the source to use.
     */
    public void setRepoRevision(String repoRevision) {
        this.repoRevision = repoRevision;
    }

    /**
     * Set whether to start this stack when it is created. Not required, default
     * is true.
     * 
     * @param startOnCreate
     *            Whether to start this stack when it is created.
     */
    public void setStartOnCreate(boolean startOnCreate) {
        this.startOnCreate = startOnCreate;
    }

    /**
     * Set the name of the property to set this stack ID to. Not required,
     * default is "stackId"
     * 
     * @param propertyNameForStackId
     *            The name of the property to set this stack's ID to.
     */
    public void setPropertyNameForStackId(String propertyNameForStackId) {
        this.propertyNameForStackId = propertyNameForStackId;
    }

    public void checkParams() {
        StringBuilder errors = new StringBuilder("");
        boolean areMalformedParams = false;

        if (name == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: name is required \n");
        }
        if (region == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: region is required \n");
        }
        if (serviceRoleArn == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: serviceRoleArn is required \n");
        }

        if (defaultInstanceProfileArn == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: defaultInstanceProfileArn is required \n");
        }
        if (!(repoType == null && repoUrl == null)
                && (repoType != null || repoUrl != null)) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: if one of repoType, repoUrl is set, they most both be set \n");
        }
        if (!(repoUsername == null && repoPassword == null)
                && (repoUsername != null || repoPassword != null)) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: if one of repoUsername, repoPassword is set, they most both be set \n");
        }
        if ((!useCustomCookbooks)
                && (repoPassword != null || repoRevision != null
                        || repoSshKey != null || repoType != null
                        || repoUrl != null || repoUsername != null)) {
            areMalformedParams = true;
            errors.append("You cannot specify any cookbook repo elements if useCustomCookbooks is false \n");
        }
        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    /**
     * Creates a stack according to the set parameters. Also sets the stackId
     * property to the created stack's ID. The ID is also printed for you to set
     * to your own property for later use.
     */
    public void execute() {
        checkParams();
        AWSOpsWorksClient client = getOrCreateClient(AWSOpsWorksClient.class);
        CreateStackRequest createStackRequest = new CreateStackRequest()
                .withName(name).withRegion(region)
                .withServiceRoleArn(serviceRoleArn)
                .withUseOpsworksSecurityGroups(useOpsworksSecurityGroups)
                .withUseCustomCookbooks(useCustomCookbooks).withVpcId(vpcId)
                .withDefaultAvailabilityZone(defaultAvailabilityZone)
                .withDefaultOs(defaultOs)
                .withDefaultRootDeviceType(defaultRootDeviceType)
                .withDefaultInstanceProfileArn(defaultInstanceProfileArn)
                .withDefaultSshKeyName(defaultSshKeyName)
                .withHostnameTheme(hostnameTheme).withCustomJson(customJson);
        if (attributes.size() > 0) {
            createStackRequest.setAttributes(attributes);
        }
        if (chefVersion != null) {
            createStackRequest
                    .setConfigurationManager(new StackConfigurationManager()
                            .withName(CHEF).withVersion(chefVersion));
        }
        if (useCustomCookbooks) {
            if (repoType != null && repoUrl != null) {
                Source customCookBookSource = new Source().withType(repoType)
                        .withUrl(repoUrl).withSshKey(repoSshKey)
                        .withRevision(repoRevision).withPassword(repoPassword)
                        .withUsername(repoUsername);
                createStackRequest
                        .setCustomCookbooksSource(customCookBookSource);
            }
        }
        if (manageBerkshelf) {
            ChefConfiguration chefConfiguration = new ChefConfiguration()
                    .withManageBerkshelf(manageBerkshelf);
            if (berkshelfVersion != null) {
                chefConfiguration.setBerkshelfVersion(berkshelfVersion);
            }
            createStackRequest.setChefConfiguration(chefConfiguration);
        }

        String stackId;
        try {
            stackId = client.createStack(createStackRequest).getStackId();
            System.out.println("Created stack with stackId " + stackId);
        } catch (Exception e) {
            throw new BuildException("Could not create stack: "
                    + e.getMessage(), e);
        }

        if (startOnCreate) {
            client.startStack(new StartStackRequest().withStackId(stackId));
            System.out.println("Started stack.");
        }
        if (stackId != null) {
            if (propertyNameForStackId.equals(Constants.STACK_ID_PROPERTY)
                    && getProject().getProperty(Constants.STACK_ID_PROPERTY) != null) {
                getProject().addReference(Constants.STACK_ID_REFERENCE, true);
            } else {
                getProject().addReference(Constants.STACK_ID_REFERENCE, false);
                getProject().setNewProperty(propertyNameForStackId, stackId);
            }
        }
    }

    /**
     * A class to be used as a nested element. Use to specify any number of
     * key-value pairs to associate with the stack.
     */
    public static class StackAttribute extends KeyValueNestedElement {
    }

}
