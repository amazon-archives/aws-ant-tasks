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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.Deployment;
import com.amazonaws.services.opsworks.model.DescribeDeploymentsRequest;

public class IncrementalDeploymentTask extends AWSAntTask {

    private LinkedList<DeploymentGroup> deploymentGroups = new LinkedList<DeploymentGroup>();

    /**
     * Allows you to add any number of nested DeploymentGroup elements to this
     * task.
     * 
     * @param deploymentGroup
     *            A preconfigured DeploymentGroup object.
     */
    public void addConfiguredDeploymentGroup(DeploymentGroup deploymentGroup) {
        deploymentGroups.add(deploymentGroup);
    }

    /**
     * Deploys each deployment in each deployment group, waits for the
     * deployments to succeed, then deploys the next group until finished.
     */
    public void execute() {
        AWSOpsWorksClient client = getOrCreateClient(AWSOpsWorksClient.class);
        for (DeploymentGroup deploymentGroup : deploymentGroups) {
            deploymentGroup.setClient(client);
            deploymentGroup.deployApps();
        }
    }

    /**
     * A class to be used as a nested element. Use to specify groups of
     * deployment tasks sequentially, where the second group must wait for the
     * first group to succeed in order to deploy, etc.
     */
    public static class DeploymentGroup implements TaskContainer {
        private List<Task> deployAppTasks = new LinkedList<Task>();
        private Set<String> deploymentIds = new HashSet<String>();
        private AWSOpsWorksClient client;

        /**
         * Allows you to add any number of nested DeployAppTask deployment
         * elements to this group. Fails if you try to add anything other than a
         * DeployAppTask element.
         * 
         * @param task
         *            A preconfigured DeployApptask object.
         */
        public void addTask(Task task) {
            try {
                deployAppTasks.add(task);
            } catch (Exception e) {
                throw new BuildException(
                        "Only deploy-opsworks-app is supported "
                                + e.getMessage());
            }
        }

        public DeploymentGroup() {
            // required by Ant
        }

        /**
         * Set the client to use to access AWS OpsWorks.
         * 
         * @param client
         *            The client to use to access AWS OpsWorks.
         */
        public void setClient(AWSOpsWorksClient client) {
            this.client = client;
        }

        /**
         * Deploys all apps in this deployment group, then waits for all the
         * deployments in the group to succeed. Deployments in a group will run
         * in parallel.
         */
        public void deployApps() {
            for (Task deployAppTask : deployAppTasks) {
                
                // This is in case of a rare bug that occurs in some JVM implementations
                if (deployAppTask instanceof UnknownElement) {
                    deployAppTask.maybeConfigure();
                    deployAppTask = ((UnknownElement) deployAppTask).getTask();
                }
                if (!deployAppTask.getTaskName().equals("deploy-opsworks-app")) {
                    throw new BuildException(
                            "Only <deploy-opsworks-app> elements are supported");
                }
                deployAppTask.execute();
                deploymentIds.add(deployAppTask.getDescription());
            }

            try {
                waitForDeploymentGroupToSucceed(deploymentIds, client);
            } catch (InterruptedException e) {
                throw new BuildException(e.getMessage(), e);
            }
        }

        /**
         * Waits for a deployment group to succeed
         * 
         * @param deploymentIds
         *            The set of the IDs of the deployments in the group
         * @param client
         *            The client to use to access AWSOpsWorks
         * @throws InterruptedException
         *             If the thread is interrupted
         */
        public void waitForDeploymentGroupToSucceed(Set<String> deploymentIds,
                AWSOpsWorksClient client) throws InterruptedException {
            int count = 0;
            while (true) {
                if (deploymentIds.isEmpty()) {
                    return;
                }

                Thread.sleep(1000 * 10);
                if (count++ > 100) {
                    throw new BuildException(
                            "Deployment never failed or succeeded");
                }

                List<Deployment> deployments = client.describeDeployments(
                        new DescribeDeploymentsRequest()
                                .withDeploymentIds(deploymentIds))
                        .getDeployments();
                for (Deployment deployment : deployments) {
                    String status = deployment.getStatus();
                    System.out.println(deployment.getDeploymentId() + " : "
                            + status);
                    if (status.equalsIgnoreCase("failed")) {
                        throw new BuildException("Deployment "
                                + deployment.getDeploymentId() + " failed");
                    } else if (status.equalsIgnoreCase("successful")) {
                        deploymentIds.remove(deployment.getDeploymentId());
                    }
                }
            }
        }
    }

}
