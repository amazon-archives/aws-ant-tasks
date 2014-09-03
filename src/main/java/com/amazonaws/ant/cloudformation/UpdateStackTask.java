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
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;

public class UpdateStackTask extends AWSAntTask {
    private String stackName;
    private String stackPolicyBody;
    private String stackPolicyURL;
    private String stackPolicyDuringUpdateBody;
    private String stackPolicyDuringUpdateURL;
    private String templateBody;
    private String templateURL;

    private List<String> capabilities = new LinkedList<String>();
    private List<String> notificationArns = new LinkedList<String>();
    private List<Parameter> parameters = new LinkedList<Parameter>();

    private Boolean usePreviousTemplate;

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
    public void addConfiguredStackParameter(StackParameter parameter) {
        parameters.add(new Parameter().withParameterKey(parameter.getKey())
                .withParameterValue(parameter.getValue())
                .withUsePreviousValue(parameter.getUsePreviousValue()));
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
     * Set the body of a stack policy to apply during the update only,
     * overriding the currently operational policy until the update completes.
     * Must be well-formed, properly escaped JSON if specified. If this is set,
     * stackPolicyDuringUpdateURL cannot be set. If stackPolicyDuringUpdateURL
     * is set, this cannot be set. Not required.
     * 
     * @param stackPolicyDuringUpdateBody
     *            Well formed, properly escaped JSON specifying a stack policy.
     */
    public void setStackPolicyDuringUpdateBody(
            String stackPolicyDuringUpdateBody) {
        this.stackPolicyDuringUpdateBody = stackPolicyDuringUpdateBody;
    }

    /**
     * Set the URL leading to the body of a stack policy to apply during the
     * update only, overriding the currently operational policy until the update
     * completes. If this is set, stackPolicyDuringUpdateBody cannot be set. If
     * stackPolicyDuringUpdateBody is set, this cannot be set. Not required.
     * 
     * @param stackPolicyDuringUpdateURL
     *            A valid URL pointing to a JSON object specifying a stack
     *            policy.
     */
    public void setStackPolicyDuringUpdateURL(String stackPolicyDuringUpdateURL) {
        this.stackPolicyDuringUpdateURL = stackPolicyDuringUpdateURL;
    }

    /**
     * Set the body of the template to use for this stack. Must be well-formed,
     * properly escaped JSON if specified. If this is set, templateURL cannot be
     * set. If templateURL is set, this cannot be set. It is required that this
     * or templateURL be set, or that usePreviousTemplate be set to true. If
     * usePreviousTemplate is true, this should not be set.
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
     * cannot be set. It is required that this or templateBody be set, or that
     * usePreviousTemplate be set to true. If usePreviousTemplate is true, this
     * should not be set.
     * 
     * @param templateURL
     *            A valid URL pointing to a JSON object specifying a template.
     */
    public void setTemplateURL(String templateURL) {
        this.templateURL = templateURL;
    }

    /**
     * Set whether to just use the previous template during this update. If this
     * is set, templateURL and templateBody should not be set. Not required.
     * 
     * @param usePreviousTemplate
     *            Whether to use the previous template during this update.
     */
    public void setUsePreviousTemplate(boolean usePreviousTemplate) {
        this.usePreviousTemplate = usePreviousTemplate;
    }

    private void checkParams() {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");

        if (stackName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: stackName is required. \n");
        }
        if (stackPolicyBody != null && stackPolicyURL != null) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: You can set either stackPolicyBody or stackPolicyURL, but not both \n");
        }

        if (!Boolean.TRUE.equals(usePreviousTemplate)
                && (templateBody == null) == (templateURL == null)) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: You must set either templateBody or templateURL (But not both), "
                    + "or set usePreviousTemplate to true. \n");
        }

        if (stackPolicyDuringUpdateBody != null
                && stackPolicyDuringUpdateURL != null) {
            areMalformedParams = true;
            errors.append("You can set stackPolicyDuringUpdateBody or stackPolicyDuringUpdateURL, but not both \n");
        }

        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AmazonCloudFormationClient client = createClient(AmazonCloudFormationClient.class);
        UpdateStackRequest request = new UpdateStackRequest()
                .withStackName(stackName).withStackPolicyBody(stackPolicyBody)
                .withStackPolicyURL(stackPolicyURL)
                .withTemplateBody(templateBody).withTemplateURL(templateURL)
                .withStackPolicyDuringUpdateBody(stackPolicyDuringUpdateBody)
                .withStackPolicyDuringUpdateURL(stackPolicyDuringUpdateURL)
                .withUsePreviousTemplate(usePreviousTemplate);

        if (capabilities.size() > 0) {
            request.setCapabilities(capabilities);
        }
        if (parameters.size() > 0) {
            request.setParameters(parameters);
        }
        if (notificationArns.size() > 0) {
            request.setNotificationARNs(notificationArns);
        }

        try {
            client.updateStack(request);
            System.out.println("Update stack " + stackName
                    + " request submitted.");
        } catch (Exception e) {
            throw new BuildException("Could not update stack: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Nested element for specifying a Capability. Set the value to the
     * Capability you want to add to the satck.
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

        public void setUsePreviousValue(boolean usePreviousValue) {
            this.usePreviousValue = usePreviousValue;
        }

        public boolean getUsePreviousValue() {
            return usePreviousValue;
        }
    }
}
