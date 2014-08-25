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
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.CreateLayerRequest;
import com.amazonaws.services.opsworks.model.Recipes;
import com.amazonaws.services.opsworks.model.VolumeConfiguration;

public class CreateLayerTask extends AWSAntTask {


    private Map<String, String> attributes = new HashMap<String, String>();
    private List<String> customSecurityGroupIds = new LinkedList<String>();
    private List<String> packages = new LinkedList<String>();
    private List<VolumeConfiguration> volumeConfigurations = new LinkedList<VolumeConfiguration>();
    private List<String> configureRecipes = new LinkedList<String>();
    private List<String> deployRecipes = new LinkedList<String>();
    private List<String> setupRecipes = new LinkedList<String>();
    private List<String> shutdownRecipes = new LinkedList<String>();
    private List<String> undeployRecipes = new LinkedList<String>();
    private String stackId;
    private String type;
    private String name;
    private String shortname;
    private String customInstanceProfileArn;
    private String propertyNameForLayerId;
    private boolean enableAutoHealing = true;
    private boolean autoAssignElasticIps;
    private boolean autoAssignPublicIps = true;
    private boolean installUpdatesOnBoot = true;
    private boolean useEbsOptimizedInstances = true;
    private boolean isSetCustomRecipe;

    /**
     * Allows you to add any number of preconfigured LayerAttribute nested
     * elements. LayerAttributes are simply user-defined key/value pairs to
     * associate with this layer.
     * 
     * @param attribute
     *            a preconfigured LayerAttribute object
     */
    public void addConfiguredLayerAttribute(LayerAttribute attribute) {
        attributes.put(attribute.getKey(), attribute.getValue());
    }

    /**
     * Allows you to add any number of preconfigured CustomSecurityGroupId
     * nested elements.
     * 
     * @param customSecurityGroupId
     *            A preconfigured CustomSecrutiyGroupId object
     */
    public void addCustomSecurityGroupId(
            CustomSecurityGroupId customSecurityGroupId) {
        customSecurityGroupIds.add(customSecurityGroupId.getValue());
    }

    /**
     * Allows you to add any number of preconfigured LayerPackage nested
     * elements
     * 
     * @param layerPackage
     *            a preconfigured LayerPackage object
     */
    public void addLayerPackage(LayerPackage layerPackage) {
        packages.add(layerPackage.getValue());
    }

    /**
     * Allows you to add any number of preconfigured LayerVolumeConfiguration
     * nested elements
     * 
     * @param layerVolumeConfiguration
     *            a preconfigured LayerVolumeConfiguration object
     */
    public void addConfiguredLayerVolumeConfiguration(
            LayerVolumeConfiguration layerVolumeConfiguration) {
        volumeConfigurations.add(new VolumeConfiguration()
                .withIops(layerVolumeConfiguration.getIops())
                .withMountPoint(layerVolumeConfiguration.getMountPoint())
                .withNumberOfDisks(layerVolumeConfiguration.getNumberOfDisks())
                .withRaidLevel(layerVolumeConfiguration.getRaidLevel())
                .withSize(layerVolumeConfiguration.getSize())
                .withVolumeType(layerVolumeConfiguration.getVolumeType()));
    }

    /**
     * Allows you to add any number of preconfigured LayerRecipe nested elements
     * 
     * @param layerRecipe
     *            a preconfigured LayerRecipe object
     */
    public void addConfiguredLayerRecipe(LayerRecipe layerRecipe) {
        isSetCustomRecipe = true;
        String phase = layerRecipe.getPhase();
        if ("configure".equalsIgnoreCase(phase)) {
            configureRecipes.add(layerRecipe.getName());
        } else if ("deploy".equalsIgnoreCase(phase)) {
            deployRecipes.add(layerRecipe.getName());
        } else if ("setup".equalsIgnoreCase(phase)) {
            setupRecipes.add(layerRecipe.getName());
        } else if ("shutdown".equalsIgnoreCase(phase)) {
            shutdownRecipes.add(layerRecipe.getName());
        } else if ("undeploy".equalsIgnoreCase(phase)) {
            undeployRecipes.add(layerRecipe.getName());
        } else {
            throw new BuildException(
                    "The specified phase "
                            + phase
                            + " was not a valid phase. Valid phases are: configure, deploy, setup, shutdown, undeploy.");
        }
    }

    /**
     * Set the opsworks ID of the stack for this layer to reside in. You can
     * find the ID of your stack in the opsworks console. If you create a stack
     * earlier in this task, it will be assigned to the "stackId" property. If
     * you have already set the "stackId" property, you do not need to set this
     * attribute--it will automatically search for the "stackId" attribute. You
     * are required to either set the "stackId" attribute or this parameter.
     * 
     * @param stackId
     *            The ID of the stack for this app to reside in.
     */
    public void setStackId(String stackId) {
        this.stackId = stackId;
    }

    /**
     * Set the layer type. Note that a stack can only have one built-in layer of
     * the same type, but can have any number of custom layers. This parameter
     * is required. This parameter must be set to one of: 
     * custom: A custom layer
     * db-master: A MySQL layer 
     * java-app: A Java App Server layer 
     * rails-app: A Rails App Server layer 
     * lb: An HAProxy layer 
     * memcached: A Memcached layer
     * monitoring-master: A Ganglia layer 
     * nodejs-app: A Node.js App Server layer
     * php-app: A PHP App Server layer 
     * web: A Static Web Server layer
     * 
     * @param type
     *            The layer type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Set the name of this layer. Required.
     * 
     * @param name
     *            The name of this layer.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the shortname of this layer. Must have up to 200 characters,
     * consisting only of alphanumeric characters, "-", "_", and ".". Required.
     * 
     * @param shortname
     *            The shortname of this layer
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     * Set the ARN of an IAM profile to use for this layer's EC2 instances. Not
     * required.
     * 
     * @param customInstanceProfileArn
     *            The ARN of an IAM profile to use for this layer's EC2
     *            instances.
     */
    public void setCustomInstanceProfileArn(String customInstanceProfileArn) {
        this.customInstanceProfileArn = customInstanceProfileArn;
    }

    /**
     * Set whether to enable auto healing for this layer. Not required, default
     * is true.
     * 
     * @param enableAutoHealing
     *            Whether to enable auto healing for this layer.
     */
    public void setEnableAutoHealing(boolean enableAutoHealing) {
        this.enableAutoHealing = enableAutoHealing;
    }

    /**
     * Set whether to automatically assign an elastic IP address to this layer's
     * instances. Not required.
     * 
     * @param autoAssignElasticIps
     *            Whether to automatically assign an elastic IP address to this
     *            layer's instances.
     */
    public void setAutoAssignElasticIps(boolean autoAssignElasticIps) {
        this.autoAssignElasticIps = autoAssignElasticIps;
    }

    /**
     * Set whether to automatically assign a public IP address to the layer's
     * instances, for stacks that are running in a VPC. Not required, default is
     * true.
     * 
     * @param autoAssignPublicIps
     *            Whether to automatically assign a public IP address to the
     *            layer's instances
     */
    public void setAutoAssignPublicIps(boolean autoAssignPublicIps) {
        this.autoAssignPublicIps = autoAssignPublicIps;
    }

    /**
     * Set whether to install operating system and package updates on boot. Not
     * required, default is true. It is highly recommended that you leave this
     * as true.
     * 
     * @param installUpdatesOnBoot
     *            Whether to install operating system and package updates on
     *            boot.
     */
    public void setInstallUpdatesOnBoot(boolean installUpdatesOnBoot) {
        this.installUpdatesOnBoot = installUpdatesOnBoot;
    }

    /**
     * Set whether to use Amazon EBS-Optimized instances. Not required.
     * 
     * @param useEbsOptimizedInstances
     *            Whether to use Amazon EBS-Optimized instances.
     */
    public void setUseEbsOptimizedInstances(boolean useEbsOptimizedInstances) {
        this.useEbsOptimizedInstances = useEbsOptimizedInstances;
    }

    /**
     * Set which property to assign the ID of this layer to
     * 
     * @param propertyToSet
     *            The property to assign the ID of this layer to
     */
    public void setPropertyNameForLayerId(String propertyToSet) {
        this.propertyNameForLayerId = propertyToSet;
    }

    private void checkParams() {
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
        if (type == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: type is required \n");
        }
        if (name == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: name is required \n");
        }
        if (shortname == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: shortName is required \n");
        }
        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    /**
     * Creates a layer according to the set parameters. Also sets a layerId
     * property. Which property will be set depends on what order this layer is
     * created in the project. If it is the first layer created in this Ant
     * build, layerId1 is set. If it's the second, layerId2 is set, etc. The ID
     * is also printed for you to set to your own property for later use.
     */
    public void execute() {
        checkParams();
        AWSOpsWorksClient client = createClient(AWSOpsWorksClient.class);
        CreateLayerRequest createLayerRequest = new CreateLayerRequest()
                .withStackId(stackId).withType(type).withName(name)
                .withShortname(shortname)
                .withEnableAutoHealing(enableAutoHealing)
                .withAutoAssignElasticIps(autoAssignElasticIps)
                .withAutoAssignPublicIps(autoAssignPublicIps)
                .withInstallUpdatesOnBoot(installUpdatesOnBoot)
                .withUseEbsOptimizedInstances(useEbsOptimizedInstances)
                .withCustomInstanceProfileArn(customInstanceProfileArn);

        if (attributes.size() > 0) {
            createLayerRequest.setAttributes(attributes);
        }
        if (packages.size() > 0) {
            createLayerRequest.setPackages(packages);
        }
        if (volumeConfigurations.size() > 0) {
            createLayerRequest.setVolumeConfigurations(volumeConfigurations);
        }
        if (customSecurityGroupIds.size() > 0) {
            createLayerRequest
                    .setCustomSecurityGroupIds(customSecurityGroupIds);
        }
        if (isSetCustomRecipe) {
            Recipes customRecipes = new Recipes()
                    .withConfigure(configureRecipes).withDeploy(deployRecipes)
                    .withSetup(setupRecipes).withShutdown(shutdownRecipes)
                    .withUndeploy(undeployRecipes);
            createLayerRequest.setCustomRecipes(customRecipes);
        }
        String layerId;
        try {
            layerId = client.createLayer(createLayerRequest).getLayerId();
        } catch (Exception e) {
            throw new BuildException("Could not create layer: "
                    + e.getMessage(), e);
        }
        System.out.println("Created layer with ID " + layerId);
        if (layerId != null) {
            if (getProject().getProperty(Constants.LAYER_IDS_PROPERTY) == null) {
                getProject().setProperty(Constants.LAYER_IDS_PROPERTY, layerId);
            } else {
                getProject().setProperty(
                        Constants.LAYER_IDS_PROPERTY,
                        getProject().getProperty(Constants.LAYER_IDS_PROPERTY) + ","
                                + layerId);
            }
            if (propertyNameForLayerId != null) {
                getProject().setProperty(propertyNameForLayerId, layerId);
            }
        }
    }

    /**
     * A class to be used as a nested element. Use to make attributes (Key-value
     * pairs) to associate with this instance.
     */
    public static class LayerAttribute {
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
         *            the value of this attribute
         */
        public void setValue(String value) {
            this.value = value;
        }

        public LayerAttribute() {
            // required by Ant
        }
    }

    /**
     * A class to be used as a nested element. Use to add any number of custom
     * security group IDs to use in this layer.
     */
    public class CustomSecurityGroupId {
        private String value;

        /**
         * Get the custom security group ID
         * 
         * @return The custom security group ID
         */
        public String getValue() {
            return value;
        }

        /**
         * Set the custom security group ID
         * 
         * @param value
         *            The custom security group ID
         */
        public void setValue(String value) {
            this.value = value;
        }

        public CustomSecurityGroupId() {
            // required by Ant
        }
    }

    /**
     * A class to use as a nested element. Use to create objects that describe
     * the layer packages.
     */
    public static class LayerPackage {
        private String value;

        /**
         * Get the layer package
         * 
         * @return The layer Package
         */
        public String getValue() {
            return value;
        }

        /**
         * Get the layer package
         * 
         * @param value
         *            The layer Package
         */
        public void setValue(String value) {
            this.value = value;
        }

        public LayerPackage() {
            // required by Ant
        }
    }

    /**
     * A class to be used as a nested element. Use to make volume configuration
     * objects that describe the layer's EBS volumes.
     */
    public static class LayerVolumeConfiguration {
        private int iops;
        private int numberOfDisks;
        private int raidLevel;
        private int size;
        private String volumeType;
        private String mountPoint;

        /**
         * Get the IOPS per disk (For PIOPS volumes)
         * 
         * @return The IOPS per disk
         */
        public int getIops() {
            return iops;
        }

        /**
         * Set the IOPS per disk (For PIOPS volumes)
         * 
         * @param iops
         *            The IOPS per disk
         */
        public void setIops(int iops) {
            this.iops = iops;
        }

        /**
         * Get the number of disks in this volume.
         * 
         * @return The number of disks in this volume.
         */
        public int getNumberOfDisks() {
            return numberOfDisks;
        }

        /**
         * Set the number of disks in this volume.
         * 
         * @param numberOfDisks
         *            The number of disks in this volume.
         */
        public void setNumberOfDisks(int numberOfDisks) {
            this.numberOfDisks = numberOfDisks;
        }

        /**
         * Get the volume RAID level
         * 
         * @return The volume RAID level
         */
        public int getRaidLevel() {
            return raidLevel;
        }

        /**
         * Set the volume RAID level
         * 
         * @param raidLevel
         *            The volume RAID level
         */
        public void setRaidLevel(int raidLevel) {
            this.raidLevel = raidLevel;
        }

        /**
         * Get the size of this volume
         * 
         * @return The size of this volume
         */
        public int getSize() {
            return size;
        }

        /**
         * Set the size of this volume
         * 
         * @param size
         *            The size of this volume
         */
        public void setSize(int size) {
            this.size = size;
        }

        /**
         * Get the volume type
         * 
         * @return The volume type
         */
        public String getVolumeType() {
            return volumeType;
        }

        /**
         * Set the volume type, must be standard or PIOPS
         * 
         * @param volumeType
         *            The volume type
         */
        public void setVolumeType(String volumeType) {
            this.volumeType = volumeType;
        }

        /**
         * Get the volume mount point
         * 
         * @return The volume mount point
         */
        public String getMountPoint() {
            return mountPoint;
        }

        /**
         * Set the volume mount point
         * 
         * @param mountPoint
         *            The volume mount point
         */
        public void setMountPoint(String mountPoint) {
            this.mountPoint = mountPoint;
        }

        public LayerVolumeConfiguration() {
            // required by Ant

        }
    }

    /**
     * A class to be used as a nested element. Use to describe recipes to use in
     * the layer configuration, and what phase to use them in.
     */
    public static class LayerRecipe {
        private String name;
        private String phase;

        /**
         * Get the name of this recipe
         * 
         * @return The name of this recipe
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name of this recipe
         * 
         * @param name
         *            The name of this recipe
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Get the phase this recipe will execute in
         * 
         * @return The phase this recipe will execute in
         */
        public String getPhase() {
            return phase;
        }

        /**
         * Set the phase this recipe will execute in
         * 
         * @param phase
         *            The phase this recipe will execute in
         */
        public void setPhase(String phase) {
            this.phase = phase;
        }
    }
}
