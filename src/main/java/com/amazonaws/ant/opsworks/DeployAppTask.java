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
import com.amazonaws.ant.SimpleNestedElement;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.CreateDeploymentRequest;
import com.amazonaws.services.opsworks.model.DeploymentCommand;

public class DeployAppTask extends AWSAntTask {

    private List<String> instanceIds = new LinkedList<String>();
    private String stackId;
    private String appId;
    private String comment;
    private String customJson;
    private String deploymentId;
    private DeploymentCommand command;
    private String propertyNameForDeploymentId;

    /**
     * The ID of the stack this deployment takes place in. You can find the ID
     * of your stack in the opsworks console. If you create a stack earlier in
     * this task, it will be assigned to the "stackId" property. If you have
     * already set the "stackId" property, you do not need to set this
     * attribute--it will automatically search for the "stackId" attribute. You
     * are required to either set the "stackId" attribute or this parameter.
     * 
     * @param stackId
     */
    public void setStackId(String stackId) {
        this.stackId = stackId;
    }

    /**
     * Allows you to add any number of preconfigured InstanceId nested elements.
     * 
     * @param instanceId
     *            A preconfigured InstanceId object.
     */
    public void addConfiguredInstanceId(InstanceId instanceId) {
        instanceIds.add(instanceId.getValue());
    }

    /**
     * Allows you to add a preconfigured Command nested element.
     * 
     * @param command
     *            A preconfigured Command object.
     */
    public void addConfiguredCommand(Command command) {
        if (this.command != null) {
            System.out
                    .println("Warning: It seems you've tried to set more than one command."
                            + " You can only specify one command per deployment."
                            + " Only the last command you specify will be run.");
        }
        this.command = new DeploymentCommand().withName(command.getName())
                .withArgs(command.getArgs());
    }

    /**
     * The ID of the app to deploy. This parameter is required. You can find the
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
     * Set a comment to go with this deployment.
     * 
     * @param comment
     *            A user-defined comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Set a JSON string used to override stack configuration JSON attributes.
     * Must be well-formed and properly escaped JSON.
     * 
     * @param customJson
     *            A string containing user-defined, custom JSON.
     */
    public void setCustomJson(String customJson) {
        this.customJson = customJson;
    }

    /**
     * Set which property to assign the ID of this deployment to
     * 
     * @param propertyToSet
     *            The property to assign the ID of this deployment to
     */
    public void setPropertyNameForDeploymentId(String propertyNameForDeploymentId) {
        this.propertyNameForDeploymentId = propertyNameForDeploymentId;
    }

    /**
     * Get the ID of this deployment
     * 
     * @return The ID of this deployment
     */
    public String getDeploymentId() {
        return deploymentId;
    }

    public void checkParams() {
        StringBuilder errors = new StringBuilder("");
        boolean areMissingParams = false;
        if (stackId == null) {
            if (!Boolean.TRUE.equals(getProject().getReference(Constants.STACK_ID_REFERENCE))) {
                stackId = getProject().getProperty(Constants.STACK_ID_PROPERTY);
            }
            if (stackId == null) {
                areMissingParams = true;
                errors.append("Missing parameter: stackId is required \n");
            } else {
                System.out.println("Using " + Constants.STACK_ID_PROPERTY
                        + " property as stackId");
            }
        }
        if (appId == null) {
            if (!Boolean.TRUE.equals(getProject().getReference(Constants.APP_ID_REFERENCE))) {
                appId = getProject().getProperty(Constants.APP_ID_PROPERTY);
            }
            if (appId == null) {
                areMissingParams = true;
                errors.append("Missing parameter: appId is required \n");
            } else {
                System.out.println("Using " + Constants.APP_ID_PROPERTY
                        + " property as appId");
            }
        }
        if (command == null) {
            areMissingParams = true;
            errors.append("Missing parameter: You must specify one Command \n");
        }

        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    /**
     * Creates a deployment according to the set parameters. Also sets a
     * deploymentId property. Which property will be set depends on what order
     * this deployment is created in the project. If it is the first deployment
     * created in this Ant build, deployment1 is set. If it's the second,
     * deployment2 is set, etc. The ID is also printed for you to set to your
     * own property for later use.
     */
    public void execute() {
        checkParams();
        AWSOpsWorksClient client = createClient(AWSOpsWorksClient.class);
        CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest()
                .withStackId(stackId).withAppId(appId).withCommand(command)
                .withInstanceIds(instanceIds);
        if (comment != null) {
            createDeploymentRequest.setComment(comment);
        }
        if (customJson != null) {
            createDeploymentRequest.setCustomJson(customJson);
        }
        String deploymentId;
        try {
            deploymentId = client.createDeployment(createDeploymentRequest)
                    .getDeploymentId();
            System.out
                    .println("Deployment request submitted. You can view the status of your deployment at https://console.aws.amazon.com/opsworks/home?#/stack/"
                            + stackId + "/deployments/" + deploymentId);
            this.deploymentId = deploymentId;
        } catch (Exception e) {
            throw new BuildException("Could not create deployment: "
                    + e.getMessage(), e);
        }
        if (deploymentId != null) {
            if (getProject().getProperty(Constants.DEPLOYMENT_IDS_PROPERTY) == null) {
                getProject().setProperty(Constants.DEPLOYMENT_IDS_PROPERTY, deploymentId);
            } else {
                getProject().setProperty(
                        Constants.DEPLOYMENT_IDS_PROPERTY,
                        getProject().getProperty(Constants.DEPLOYMENT_IDS_PROPERTY) + ","
                                + deploymentId);
            }
            if (propertyNameForDeploymentId != null) {
                getProject().setProperty(propertyNameForDeploymentId, deploymentId);
                setDescription(deploymentId);
            }
        }
    }

    /**
     * A class to be used as a nested element, specifying the IDs of EC2
     * instances to deploy the app to
     */
    public static class InstanceId extends SimpleNestedElement {
    }

    /**
     * A class to be used as a nested element. Used to create a command, which
     * has a name and a set of arguments
     */
    public static class Command {

        private Map<String, List<String>> args = new HashMap<String, List<String>>();
        private String name;

        /**
         * Allows you to add any number of preconfigured Arg nested elements.
         * 
         * @param arg
         *            A preconfigured Arg object.
         */
        public void addConfiguredArg(Arg arg) {
            args.put(arg.getName(), arg.getValues());
        }

        /**
         * Get this command's arguments.
         * 
         * @return This command's arguments.
         */
        public Map<String, List<String>> getArgs() {
            return args;
        }

        /**
         * Get the name of this command.
         * 
         * @return The name of this command.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name of this command.
         * 
         * @param name
         *            The name of this command.
         */
        public void setName(String name) {
            this.name = name;
        }

        public Command() {
            // required by Ant
        }

   /**
    * A class to be used as a nested element in Command. Used to specify an
    * argument to a command, which has a name and a set of values.
    */
    public static class Arg {
    
        private List<String> values = new LinkedList<String>();
        private String name;

        /**
         * Allows you to add any number of preconfigured ArgVal nested
         * elements.
         * 
         * @param argVal
         *            a preconfgirued ArgVal object.
         */
         public void addConfiguredArgVal(ArgVal argVal) {
             values.add(argVal.getValue());
         }

       /**
        * Get the values of this argument.
        * 
        * @return The values of this argument.
        */
        public List<String> getValues() {
           return values;
        }

       /**
        * Get the name of this argument.
        * 
        * @return The name of this argument.
        */
        public String getName() {
           return name;
        }

       /**
        * Set the name of this argument.
        * 
        * @param name
        *            The name of this argument.
        */
        public void setName(String name) {
           this.name = name;
        }

        public Arg() {
           // required by Ant
        }
    /**
     * A class to be used as a nested element by Arg. Used to specify
     * the value of an argument.
     */
    public static class ArgVal extends SimpleNestedElement{
     }
    }
    }
}
