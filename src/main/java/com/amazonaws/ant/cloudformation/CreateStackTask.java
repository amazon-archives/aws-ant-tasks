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
package com.amazonaws.ant.cloudformation;

import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.ant.KeyValueNestedElement;
import com.amazonaws.ant.SimpleNestedElement;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;

public class CreateStackTask extends AWSAntTask {

    private static final String CREATE_COMPLETE = "CREATE_COMPLETE";
    private String onFailure;
    private String stackName;
    private String stackPolicyBody;
    private String stackPolicyURL;
    private String templateBody;
    private String templateURL;

    private List<String> capabilities = new LinkedList<String>();
    private List<String> notificationArns = new LinkedList<String>();
    private List<Parameter> parameters = new LinkedList<Parameter>();
    private List<Tag> tags = new LinkedList<Tag>();

    private Boolean disableRollback;
    private boolean waitForCreation = false;

    private Integer timeoutInMinutes;

    /**
     * Allows you to add any number of nested preconfigured Capability elements.
     * Will warn you if the Capability is not supported by our model, but will
     * still try to execute.
     * 
     * @param capability
     *            a preconfigured Capability object.
     */
    public void addConfiguredCapability(StackCapability capability) {
        String toAdd = capability.getValue();
        try {
            Capability.fromValue(toAdd);
        } catch (IllegalArgumentException e) {
            System.out
                    .println("The capability "
                            + toAdd
                            + " does not seem to be in our model. If this build fails, this may be why.");
        } finally {
            capabilities.add(toAdd);
        }
    }

    /**
     * Allows you to add any number of nested preconfigured NotificationArn
     * elements.
     * 
     * @param notificationArn
     *            a preconfigured NotificationArn object.
     */
    public void addConfiguredNotificationArn(NotificationArn notificationArn) {
        notificationArns.add(notificationArn.getValue());
    }

    /**
     * Allows you to add any number of nested preconfigured StackParameter
     * elements.
     * 
     * @param stackParameter
     *            a preconfigured StackParameter object.
     */
    public void addConfiguredStackParameter(StackParameter stackParameter) {
        parameters.add(new Parameter()
                .withParameterKey(stackParameter.getKey())
                .withParameterValue(stackParameter.getValue())
                .withUsePreviousValue(stackParameter.getUsePreviousValue()));
    }

    /**
     * Allows you to add any number of nested preconfigured StackTag elements.
     * 
     * @param stackTag
     *            a preconfigured StackTag object.
     */
    public void addConfiguredStackTag(StackTag stackTag) {
        tags.add(new Tag().withKey(stackTag.getKey()).withValue(
                stackTag.getValue()));
    }

    /**
     * Set an action to execute upon failure. If this element is set,
     * disableRollback should not be set. If disableRollback is set, this should
     * not be set. Will print out a warning if your argument is not supported by
     * our model, but will still try to execute. Not required.
     * 
     * @param onFailure
     *            An action to execute on failure.
     */
    public void setOnFailure(String onFailure) {
        try {
            OnFailure.fromValue(onFailure);
        } catch (IllegalArgumentException e) {
            System.out
                    .println("The OnFailure "
                            + onFailure
                            + " does not seem to be in our model. If this build fails, this may be why.");
        } finally {
            this.onFailure = onFailure;
        }
    }

    /**
     * Set the name of this stack. Required.
     * 
     * @param stackName
     *            The stack name
     */
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    /**
     * Set the body of a stack policy to apply. Must be well-formed, properly
     * escaped JSON if specified. If this is set, stackPolicyURL cannot be set.
     * If stackPolicyURL is set, this cannot be set. Not required.
     * 
     * @param stackPolicyBody
     *            Well formed, properly escaped JSON specifying a stack policy.
     */
    public void setStackPolicyBody(String stackPolicyBody) {
        this.stackPolicyBody = stackPolicyBody;
    }

    /**
     * Set the URL leading to the body of a stack policy to apply. If this is
     * set, stackPolicyBody cannot be set. If stackPolicyBody is set, this
     * cannot be set. Not required.
     * 
     * @param stackPolicyURL
     *            A valid URL pointing to a JSON object specifying a stack
     *            policy.
     */
    public void setStackPolicyURL(String stackPolicyURL) {
        this.stackPolicyURL = stackPolicyURL;
    }

    /**
     * Set the body of the template to use for this stack. Must be well-formed,
     * properly escaped JSON if specified. If this is set, templateURL cannot be
     * set. If templateURL is set, this cannot be set. It is required that this
     * or templateURL be set.
     * 
     * @param templateBody
     *            Well formed, properly escaped JSON specifying a template.
     */
    public void setTemplateBody(String templateBody) {
        this.templateBody = templateBody;
    }

    /**
     * Set the URL leading to the body of the template to use for this stack. If
     * this is set, templateBody cannot be set. If templateBody is set, this
     * cannot be set. It is required that this or templateBody be set.
     * 
     * @param templateURL
     *            A valid URL pointing to a JSON object specifying a template.
     */
    public void setTemplateURL(String templateURL) {
        this.templateURL = templateURL;
    }

    /**
     * Set whether to disable rollback if stack creation fails. If onFailure is
     * set, this cannot be set. If this is set, onFailure cannot be set. Not
     * required, default is false.
     * 
     * @param disableRollback
     *            Whether to disable rollback if stack creation fails.
     */
    public void setDisableRollback(boolean disableRollback) {
        this.disableRollback = disableRollback;
    }
    
    /**
     * Set whether to block the build until this stack successfully finishes
     * creation. Not required, default is false.
     * 
     * @param waitForCreation
     *            Whether to block the build until this stack successfully
     *            finishes creation
     */
    public void setWaitForCreation(boolean waitForCreation) {
        this.waitForCreation = waitForCreation;
    }

    /**
     * Set the amount of time to allow the stack to take to create. Required,
     * and should be greater than 0.
     * 
     * @param timeoutInMinutes
     *            The amount of time to allow the stack to take to create before
     *            failing.
     */
    public void setTimeoutInMinutes(int timeoutInMinutes) {
        this.timeoutInMinutes = timeoutInMinutes;
    }

    private void checkParams() {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");

        if (stackName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: stackName is required. \n");
        }

        if (timeoutInMinutes == null || timeoutInMinutes.intValue() <= 0) {
            areMalformedParams = true;
            errors.append("Missing parameter: timeoutInMinutes is required and cannot be 0 \n");
        }
        if (stackPolicyBody != null && stackPolicyURL != null) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: You can set either stackPolicyBody or stackPolicyURL, but not both \n");
        }

        if ((templateBody == null) == (templateURL == null)) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: You must set either templateBody or templateURL (But not both) \n");
        }

        if (disableRollback != null && onFailure != null) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration :You can specify disableRollback or onFailure, but not both \n");
        }

        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }

    }

    public void execute() {
        checkParams();
        AmazonCloudFormationClient client = getOrCreateClient(AmazonCloudFormationClient.class);
        CreateStackRequest createStackRequest = new CreateStackRequest()
                .withDisableRollback(disableRollback).withOnFailure(onFailure)
                .withStackName(stackName).withStackPolicyBody(stackPolicyBody)
                .withStackPolicyURL(stackPolicyURL)
                .withTemplateBody(templateBody).withTemplateURL(templateURL)
                .withTimeoutInMinutes(timeoutInMinutes);

        if (capabilities.size() > 0) {
            createStackRequest.setCapabilities(capabilities);
        }
        if (parameters.size() > 0) {
            createStackRequest.setParameters(parameters);
        }
        if (tags.size() > 0) {
            createStackRequest.setTags(tags);
        }
        try {
            client.createStack(createStackRequest);
            System.out.println("Create stack " + stackName
                    + " request submitted.");
            if(waitForCreation) {
                WaitForStackToReachStateTask.waitForCloudFormationStackToReachStatus(client, stackName, CREATE_COMPLETE);
            }
        } catch (Exception e) {
            throw new BuildException(
                    "Could not create stack " + e.getMessage(), e);
        }
    }

    /**
     * Nested element for specifying a Capability. Set the value to the
     * Capability you want to add to the stack.
     */
    public static class StackCapability extends SimpleNestedElement {
    }

    /**
     * Nested element for specifying a NotificationArn. Set the value to the
     * NotificationArn you want to add to the stack.
     */
    public static class NotificationArn extends SimpleNestedElement {
    }

    /**
     * Nested element for specifying a Parameter. Set the key to the name of the
     * parameter, and the value to the value of the parameter.
     */
    public static class StackParameter extends KeyValueNestedElement {
        private boolean usePreviousValue;

        /**
         * Set whether to use the previous value when setting a parameter that
         * has already been set.
         * 
         * @param usePreviousValue
         *            Whether to use the previous value of this parameter.
         */
        public void setUsePreviousValue(boolean usePreviousValue) {
            this.usePreviousValue = usePreviousValue;
        }

        public boolean getUsePreviousValue() {
            return usePreviousValue;
        }
    }

    /**
     * Nested element for specifying a Tag (Key-value pair to associate with the
     * Stack).
     */
    public static class StackTag extends KeyValueNestedElement {
    }
}
