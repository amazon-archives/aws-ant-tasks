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

/**
 * A class containing properties and references for tasks in the project to use
 * to communicate certain elements with each other
 */
public class Constants {
    public static final String APP_ID_PROPERTY = "appId";
    public static final String STACK_ID_PROPERTY = "stackId";
    public static final String LAYER_IDS_PROPERTY = "layerIds";
    public static final String INSTANCE_IDS_PROPERTY = "instanceIds";
    public static final String DEPLOYMENT_IDS_PROPERTY = "deploymentIds";

    // This is used when creating a stack--if the task tries to set "stackId"
    // but it is already set, this sets a reference in the project to true.
    public static final String STACK_ID_REFERENCE = "stackIdAlreadyUsed";

    // Similarly this is used when creating a app--if the task tries to set
    // "appId" but it is already set, this sets a reference in the project to
    // true.
    public static final String APP_ID_REFERENCE = "appIdAlreadyUsed";
}
