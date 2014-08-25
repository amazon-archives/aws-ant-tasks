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

import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.Architecture;
import com.amazonaws.services.opsworks.model.AutoScalingType;
import com.amazonaws.services.opsworks.model.CreateInstanceRequest;
import com.amazonaws.services.opsworks.model.RootDeviceType;
import com.amazonaws.services.opsworks.model.StartInstanceRequest;

public class CreateInstanceTask extends AWSAntTask {

    
    private List<String> layerIds = new LinkedList<String>();
    private String stackId;
    private String instanceType;
    private String os;
    private String amiId;
    private String sshKeyName;
    private String availabilityZone;
    private String virtualizationType;
    private String subnetId;
    private String propertyNameForInstanceId;
    private Architecture architecture;
    private AutoScalingType autoScalingType;
    private RootDeviceType rootDeviceType = RootDeviceType.Ebs;
    private boolean installUpdatesOnBoot = true;
    private boolean ebsOptimized;
    private boolean useProjectLayerIds = true;
    private boolean startOnCreate = true;

    /**
     * Allows you to add a proconfigured nested LayerId element. At least one
     * LayerId must be specified.
     * 
     * @param layerId
     *            A preconfigured LayerID object to add
     */
    public void addConfiguredLayerId(LayerId layerId) {
        layerIds.add(layerId.getValue());
    }

    /**
     * The ID of the stack to link to this instance. You can find the ID of your
     * stack in the opsworks console. If you create a stack earlier in this
     * task, it will be assigned to the "stackId" property. If you have already
     * set the "stackId" property, you do not need to set this attribute--it
     * will automatically search for the "stackId" attribute. You are required
     * to either set the "stackId" attribute or this parameter.
     * 
     * @param stackId
     */
    public void setStackId(String stackId) {
        this.stackId = stackId;
    }

    /**
     * Set the type of this instance, such as m1.small, t2.medium, etc.
     * Required.
     * 
     * @param instanceType
     *            The type of this instance.
     */
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Set the type of autoscaling to use for this instance. Not required.
     * 
     * @param autoScalingType
     *            The type of autoscaling to use for this instance.
     */
    public void setAutoScalingType(String autoScalingType) {
        try {
            this.autoScalingType = AutoScalingType.valueOf(autoScalingType);
        } catch (IllegalArgumentException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }

    /**
     * Set the operating system for this instance. Not required, defaults to
     * Amazon Linux.
     * 
     * @param os
     *            The operating system to use for this instance.
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * Set the custom AMI ID to be used to create this instance. Use if you set
     * "os" to "Custom". Not required.
     * 
     * @param amiId
     *            A custom AMI ID to be used to create this instance
     */
    public void setAmiId(String amiId) {
        this.amiId = amiId;
    }

    /**
     * Set the SSH key name of this instance. Not required, but this must be set
     * if you want to SSH into this instance later.
     * 
     * @param sshKeyName
     *            The IAM SSH key name used to SSH into this instance.
     */
    public void setSshKeyName(String sshKeyName) {
        this.sshKeyName = sshKeyName;
    }

    /**
     * Set the availability zone of this instance. Not required.
     * 
     * @param availabilityZone
     *            The availability zone of this instance
     */
    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * Set the architecture of this instance. Not required, defaults to x86_64.
     * 
     * @param architecture
     *            The architecture of this instance.
     */
    public void setArchitecture(String architecture) {
        try {
            this.architecture = Architecture.valueOf(architecture);
        } catch (IllegalArgumentException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }

    /**
     * Set whether to install OS and package updates when this instance boots.
     * Not required, default is true. It is highly recommended that you leave
     * this as true.
     * 
     * @param installUpdatesOnBoot
     *            Whether to install OS and package updates when this instance
     *            boots.
     */
    public void setInstallUpdatesOnBoot(boolean installUpdatesOnBoot) {
        this.installUpdatesOnBoot = installUpdatesOnBoot;
    }

    /**
     * Set whether to create an Amazon EBS-Optimized instance. Not required,
     * default is false.
     * 
     * @param ebsOptimized
     *            Whether to create an Amazon EBS-Optimized instance.
     */
    public void setEbsOptimized(boolean ebsOptimized) {
        this.ebsOptimized = ebsOptimized;
    }

    /**
     * Set whether to add all LayerId project properties to the layerIds group
     * of this instance. If set to true, then all layers created earlier in this
     * build will be used as layerIds for this instance. Not required, defaults
     * to true.
     * 
     * @param useProjectLayerIds
     *            Whether to add all the IDs of all layers created earlier in
     *            this project to the layerIds group of this instance.
     */
    public void setUseProjectLayerIds(boolean useProjectLayerIds) {
        this.useProjectLayerIds = useProjectLayerIds;
    }

    /**
     * Set whether to start this instance at the end of the execution of this
     * task. Not required, defaults to true.
     * 
     * @param startOnCreate
     *            Whether to start this instance at the end of the execution of
     *            this task.
     */
    public void setStartOnCreate(boolean startOnCreate) {
        this.startOnCreate = startOnCreate;
    }

    /**
     * Set the instance's virtualization type. Not required.
     * 
     * @param virtualizationType
     *            The instances virtualization type. Should be paravirtual or
     *            hvm.
     */
    public void setVirtualizationType(String virtualizationType) {
        this.virtualizationType = virtualizationType;
    }

    /**
     * Set the root device type of this instance. Not required.
     * 
     * @param rootDeviceType
     *            The root device type of this instance
     */
    public void setRootDeviceType(String rootDeviceType) {
        try {
            this.rootDeviceType = RootDeviceType.valueOf(rootDeviceType);
        } catch (IllegalArgumentException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }

    /**
     * The ID of this instance's subnet. You can use this parameter to override
     * the default, if this stack is running in a VPC.
     * 
     * @param subnetId
     *            The ID of this instance's subnet.
     */
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    /**
     * Set which property to assign the ID of this instance to
     * 
     * @param propertyToSet
     *            The property to assign the ID of this instance to
     */
    public void setPropertyNameForInstanceId(String propertyToSet) {
        this.propertyNameForInstanceId = propertyToSet;
    }

    public void checkParams() {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");
        if (stackId == null) {
            if (!Boolean.TRUE.equals(getProject().getReference(Constants.STACK_ID_REFERENCE))) {
                stackId = getProject().getProperty(Constants.STACK_ID_PROPERTY);
            }
            if (stackId == null) {
                areMalformedParams = true;
                errors.append("Missing parameter: stackId is required \n");
            } else {
                System.out.println("Using " + Constants.STACK_ID_PROPERTY
                        + " property as stackId");
            }
        }
        if (useProjectLayerIds) {
            addProjectLayerIds();
        }
        if (layerIds.size() <= 0) {
            areMalformedParams = true;
            errors.append("Missing parameter: You must specify at least one LayerId \n");
        }
        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    private void addProjectLayerIds() {
        String projectLayerIds = getProject().getProperty(Constants.LAYER_IDS_PROPERTY);
        for (String layerId : projectLayerIds.split(",")) {
            layerIds.add(layerId);
        }
    }

    /**
     * Creates an instance according to the set parameters. Also sets an
     * instanceId property. Which property will be set depends on what order
     * this instance is created in the project. If it is the first instance
     * created in this Ant build, instanceId1 is set. If it's the second,
     * instanceId2 is set, etc. The ID is also printed for you to set to your
     * own property for later use.
     */
    public void execute() {
        checkParams();
        AWSOpsWorksClient client = createClient(AWSOpsWorksClient.class);
        CreateInstanceRequest createInstanceRequest = new CreateInstanceRequest()
                .withStackId(stackId)
                .withInstallUpdatesOnBoot(installUpdatesOnBoot)
                .withEbsOptimized(ebsOptimized).withLayerIds(layerIds)
                .withInstanceType(instanceType).withOs(os)
                .withAmiId(amiId).withSshKeyName(sshKeyName)
                .withAvailabilityZone(availabilityZone)
                .withVirtualizationType(virtualizationType)
                .withRootDeviceType(rootDeviceType).withSubnetId(subnetId);
        String instanceId;
        if(autoScalingType != null) {
            createInstanceRequest.setAutoScalingType(autoScalingType);
        }
        if(architecture != null) {
            createInstanceRequest.setArchitecture(architecture);
        }
        try {
            instanceId = client.createInstance(createInstanceRequest)
                    .getInstanceId();
            if (startOnCreate) {
                client.startInstance(new StartInstanceRequest()
                        .withInstanceId(instanceId));
                System.out.println("Starting created instance.");
            }
            Thread.sleep(10); // Wait for the instance metadata to populate
            System.out
                    .println("Created instance with instanceId "
                            + instanceId
                            + ". View the status of this instance at https://console.aws.amazon.com/opsworks/home?#/stack/"
                            + stackId + "/instances");
        } catch (Exception e) {
            throw new BuildException("Could not create Instance: "
                    + e.getMessage(), e);
        }

        if (instanceId != null) {
            if (getProject().getProperty(Constants.INSTANCE_IDS_PROPERTY) == null) {
                getProject().setProperty(Constants.INSTANCE_IDS_PROPERTY, instanceId);
            } else {
                getProject().setProperty(
                        Constants.INSTANCE_IDS_PROPERTY,
                        getProject().getProperty(Constants.INSTANCE_IDS_PROPERTY) + ","
                                + instanceId);
            }
            if (propertyNameForInstanceId != null) {
                getProject().setProperty(propertyNameForInstanceId, instanceId);
            }
        }
    }

    /**
     * A container class to use as a nested element, so you can specify any
     * number of layerIds for this instance. You can find the IDs of layers to
     * use in the OpsWorks console.
     * 
     * @author jesduart
     * 
     */
    public static class LayerId {
        private String value;

        /**
         * Get the layerId you set
         * 
         * @return The layerId
         */
        public String getValue() {
            return value;
        }

        /**
         * Set the layerId
         * 
         * @param value
         *            A layerId to use in an instance.
         */
        public void setValue(String value) {
            this.value = value;
        }

        public LayerId() {
            // required by Ant
        }

    }

}
