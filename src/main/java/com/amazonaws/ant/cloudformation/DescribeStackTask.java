/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;

public class DescribeStackTask extends AWSAntTask {

    private String stackName;
    
    private Map<String, StackItem> parameters = new HashMap<String, StackItem>();
    private Map<String, StackItem> tags = new HashMap<String, StackItem>();
    
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
     * Allows you to add any number of nested stack parameter elements.
     * 
     * @param stackParameter
     *            a StackItem object.
     */
    public void addConfiguredStackParameter(StackItem stackParameter) {
        parameters.put(stackParameter.getName(), stackParameter);
    }
    
    /**
     * Allows you to add any number of nested stack tag elements.
     * 
     * @param stackTag
     *            a StackItem object.
     */
    public void addConfiguredStackTag(StackItem stackTag) {
        tags.put(stackTag.getName(), stackTag);
    }
    
    private void checkParams() {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");

        if (stackName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: stackName is required. \n");
        }

        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }

    }
    
    @Override
    public void execute() throws BuildException
    {
        checkParams();
        AmazonCloudFormationClient client = getOrCreateClient(AmazonCloudFormationClient.class);
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest()
            .withStackName(stackName);

        try {
            DescribeStacksResult result = client.describeStacks(describeStacksRequest);
            Stack stack = result.getStacks().get(0);

            // put the desired stack parameters into properties
            if(stack.getParameters() != null) {
                for(Parameter parameter : stack.getParameters()) {
                    StackItem item = parameters.remove(parameter.getParameterKey());
                    if(item != null) {
                        getProject().setNewProperty(item.getProperty(), parameter.getParameterValue());
                    }
                }
            }
            
            for(StackItem item : parameters.values()) {
                getProject().setNewProperty(item.getName(), item.getDefault());
            }
            
            if(stack.getTags() != null) {
                for(Tag tag : stack.getTags()) {
                    StackItem item = tags.remove(tag.getKey());
                    if(item != null) {
                        getProject().setNewProperty(item.getName(), tag.getValue());
                    }
                }
            }
            
            for(StackItem item : tags.values()) {
                getProject().setNewProperty(item.getProperty(), item.getDefault());
            }
        } catch (Exception e) {
            throw new BuildException(
                    "Could not describe stack " + e.getMessage(), e);
        }
    }
    
    /**
     * Nested element for specifying a Parameter. Set the key to the name of the
     * parameter, and the value of the property to set.
     */
    public static class StackItem {
        private String name;
        private String property;
        private String def;

        public void setName(String name) {
            this.name = name;
        }

        public void setProperty(String property) {
            this.property = property;
        }
        
        public void setDefault(String def) {
            this.def = def;
        }

        public String getName() {
            return name;
        }

        public String getProperty() {
            return property;
        }
        
        public String getDefault() {
            return def;
        }
    }
}
