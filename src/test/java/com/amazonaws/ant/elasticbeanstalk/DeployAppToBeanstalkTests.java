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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.ant.AWSTestUtils;
import com.amazonaws.ant.elasticbeanstalk.CreateBeanstalkApplicationTask;
import com.amazonaws.ant.elasticbeanstalk.CreateBeanstalkEnvironmentTask;
import com.amazonaws.ant.elasticbeanstalk.DeployAppToBeanstalkTask;
import com.amazonaws.ant.elasticbeanstalk.CreateBeanstalkEnvironmentTask.Setting;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus;
import com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription;
import com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription;
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.DeleteInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.RemoveRoleFromInstanceProfileRequest;
import com.amazonaws.services.s3.AmazonS3Client;

public class DeployAppToBeanstalkTests {

	private static final String TEST_APPNAME = "AntTaskTestBeanstalkApplicationName";
	private static final String TEST_APPDESC = "AntTaskTestBeanstalkApplication Description";
	private static final String TEST_VERSIONLABEL = "Version1";
	private static final String TEST_VERSIONDESC = "Test description for Version 1";
	private static final String TEST_CNAMEPREFIX = "testdeployapptobeanstalk";
	private static final String TEST_ENVIRONMENTNAME = "tst"
			+ System.currentTimeMillis();
	private static final String TEST_ENVIRONMENTDESC = "Test description for environment";
	private static final String TEST_SOLUTIONSTACKNAME = "64bit Amazon Linux running Tomcat 6";
	private static final String BUCKET_NAME = "deployapptobeanstalktestbucket";
	private static final String KEY = "test/test.war";
	private static final String WAR_FILE = System.getProperty("user.dir")
			+ "/test.war";
	private static final String TEST_INSTANCEPROFILE_NAMESPACE = "aws:autoscaling:launchconfiguration";
	private static final String INSTANCEPROFILE_OPTIONNAME = "IamInstanceProfile";
	private static final String TEST_INSTANCEPROFILE_VALUE = "ElasticBeanstalkProfile";
	private static final String SQSD_NAMESPACE = "aws:elasticbeanstalk:sqsd";
	private static final String WORKERQUEUEURL_OPTIONNAME = "WorkerQueueURL";
	private static final String TEST_WORKERQUEUEURL_VALUE = "http://sqsd.elasticbeanstalk.us-east-1.amazon.com";
	private static final String HTTPPATH_OPTIONNAME = "HttpPath";
	private static final String TEST_HTTPPATH_VALUE = "/";
	private static final String MIMETYPE_OPTIONNAME = "MimeType";
	private static final String TEST_MIMETYPE_VALUE = "application/json";
	private static final String HTTPCONNECTIONS_OPTIONNAME = "HttpConnections";
	private static final String TEST_HTTPCONNECTIONS_VALUE = "75";
	private static final String CONNECTTIMEOUT_OPTIONNAME = "ConnectTimeout";
	private static final String TEST_CONNECTTIMEOUT_VALUE = "10";
	private static final String INACTIVITYTIMEOUT_OPTIONNAME = "InactivityTimeout";
	private static final String TEST_INACTIVITYTIMEOUT_VALUE = "10";
	private static final String VISIBILITYTIMEOUT_OPTIONNAME = "VisibilityTimeout";
	private static final String TEST_VISIBILITYTIMEOUT_VALUE = "60";
	private static final String RETENTIONPERIOD_OPTIONNAME = "RetentionPeriod";
	private static final String TEST_RETENTIONPERIOD_VALUE = "345600";
	private static final String WORKER_TIER = "Worker";
	private static final String TEST_TIERTYPE = "SQS/HTTP";
	private static final String TEST_WORKERVERSION = "1.0";

	private static AmazonS3Client s3Client;
	private static AWSElasticBeanstalkClient bcClient;
	private static AmazonIdentityManagementClient iamClient;

	@BeforeClass
	public static void setUp() throws InterruptedException {
		bcClient = new AWSElasticBeanstalkClient();
		s3Client = new AmazonS3Client();
		s3Client.createBucket(BUCKET_NAME);
		bcClient = new AWSElasticBeanstalkClient();
		iamClient = new AmazonIdentityManagementClient();
		iamClient.createInstanceProfile(new CreateInstanceProfileRequest()
				.withInstanceProfileName(TEST_INSTANCEPROFILE_VALUE));
		iamClient
				.addRoleToInstanceProfile(new AddRoleToInstanceProfileRequest()
						.withRoleName("aws-elasticbeanstalk-ec2-role")
						.withInstanceProfileName(TEST_INSTANCEPROFILE_VALUE));
	}

	@Test
	public void testExecuteWebService() {
		s3Client.putObject(BUCKET_NAME, KEY, new File(WAR_FILE));
		CreateBeanstalkApplicationTask createAppTask = new CreateBeanstalkApplicationTask();
		CreateBeanstalkEnvironmentTask createEnvTask = new CreateBeanstalkEnvironmentTask();
		DeployAppToBeanstalkTask deployTask = new DeployAppToBeanstalkTask();
		deployTask.setApplicationName(TEST_APPNAME);
		createAppTask.setApplicationDescription(TEST_APPDESC);
		deployTask.setVersionLabel(TEST_VERSIONLABEL);
		deployTask.setVersionDescription(TEST_VERSIONDESC);
		deployTask.setBucketName(BUCKET_NAME);
		deployTask.setKey(KEY);
		createEnvTask.setCnamePrefix(TEST_CNAMEPREFIX);
		deployTask.setEnvironmentName(TEST_ENVIRONMENTNAME);
		createEnvTask.setEnvironmentDescription(TEST_ENVIRONMENTDESC);
		Setting setting = new Setting();
		setting.setNamespace(TEST_INSTANCEPROFILE_NAMESPACE);
		setting.setOptionName(INSTANCEPROFILE_OPTIONNAME);
		setting.setValue(TEST_INSTANCEPROFILE_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		createEnvTask.setSolutionStackName(TEST_SOLUTIONSTACKNAME);
		createAppTask.setApplicationName(TEST_APPNAME);
		createEnvTask.setEnvironmentName(TEST_ENVIRONMENTNAME);
		createEnvTask.setApplicationName(TEST_APPNAME);
		createAppTask.execute();
		createEnvTask.execute();
		try {
			AWSTestUtils.waitForEnvironmentToTransitionToStateAndHealth(
					TEST_ENVIRONMENTNAME, EnvironmentStatus.Ready, null,
					bcClient);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail();
		}
		deployTask.execute();
		DescribeApplicationsResult appResult = bcClient.describeApplications();
		boolean appNameFound = false;
		for (ApplicationDescription appdesc : appResult.getApplications()) {
			if (appdesc.getApplicationName().equals(TEST_APPNAME)) {
				appNameFound = true;
				assertTrue(appdesc.getDescription().equals(TEST_APPDESC));
				break;
			}
		}
		assertTrue(appNameFound);
		boolean appDescFound = false;
		DescribeApplicationVersionsResult verResult = bcClient
				.describeApplicationVersions();
		for (ApplicationVersionDescription verdesc : verResult
				.getApplicationVersions()) {
			if (verdesc.getVersionLabel().equals(TEST_VERSIONLABEL)) {
				appDescFound = true;
				assertTrue(verdesc.getDescription().equals(TEST_VERSIONDESC));
				break;
			}
		}
		assertTrue(appDescFound);
		try {
			AWSTestUtils.waitForEnvironmentToTransitionToStateAndHealth(
					TEST_ENVIRONMENTNAME, EnvironmentStatus.Ready,
					EnvironmentHealth.Green, bcClient);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail();
		}
		assertTrue(bcClient.describeEnvironments().getEnvironments().size() > 0);
		EnvironmentDescription environment = bcClient
				.describeEnvironments(
						new DescribeEnvironmentsRequest()
								.withEnvironmentNames(TEST_ENVIRONMENTNAME))
				.getEnvironments().get(0);
		assertEquals(TEST_ENVIRONMENTDESC, environment.getDescription());
		assertTrue(environment.getCNAME().startsWith(TEST_CNAMEPREFIX));
		assertEquals(TEST_SOLUTIONSTACKNAME, environment.getSolutionStackName());
	}

	@Test
	public void testExecuteWorkerTier() {
		s3Client.putObject(BUCKET_NAME, KEY, new File(WAR_FILE));
		CreateBeanstalkApplicationTask createAppTask = new CreateBeanstalkApplicationTask();
		CreateBeanstalkEnvironmentTask createEnvTask = new CreateBeanstalkEnvironmentTask();
		DeployAppToBeanstalkTask deployTask = new DeployAppToBeanstalkTask();
		deployTask.setApplicationName(TEST_APPNAME);
		createAppTask.setApplicationDescription(TEST_APPDESC);
		deployTask.setVersionLabel(TEST_VERSIONLABEL);
		deployTask.setVersionDescription(TEST_VERSIONDESC);
		deployTask.setBucketName(BUCKET_NAME);
		deployTask.setKey(KEY);
		deployTask.setEnvironmentName(TEST_ENVIRONMENTNAME);
		createEnvTask.setEnvironmentDescription(TEST_ENVIRONMENTDESC);
		createEnvTask.setSolutionStackName(TEST_SOLUTIONSTACKNAME);
		Setting setting = new Setting();
		setting.setNamespace(TEST_INSTANCEPROFILE_NAMESPACE);
		setting.setOptionName(INSTANCEPROFILE_OPTIONNAME);
		setting.setValue(TEST_INSTANCEPROFILE_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		setting = new Setting();
		setting.setNamespace(SQSD_NAMESPACE);
		setting.setOptionName(WORKERQUEUEURL_OPTIONNAME);
		setting.setValue(TEST_WORKERQUEUEURL_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		setting = new Setting();
		setting.setNamespace(SQSD_NAMESPACE);
		setting.setOptionName(HTTPPATH_OPTIONNAME);
		setting.setValue(TEST_HTTPPATH_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		setting = new Setting();
		setting.setNamespace(SQSD_NAMESPACE);
		setting.setOptionName(MIMETYPE_OPTIONNAME);
		setting.setValue(TEST_MIMETYPE_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		setting = new Setting();
		setting.setNamespace(SQSD_NAMESPACE);
		setting.setOptionName(HTTPCONNECTIONS_OPTIONNAME);
		setting.setValue(TEST_HTTPCONNECTIONS_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		setting = new Setting();
		setting.setNamespace(SQSD_NAMESPACE);
		setting.setOptionName(CONNECTTIMEOUT_OPTIONNAME);
		setting.setValue(TEST_CONNECTTIMEOUT_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		setting = new Setting();
		setting.setNamespace(SQSD_NAMESPACE);
		setting.setOptionName(INACTIVITYTIMEOUT_OPTIONNAME);
		setting.setValue(TEST_INACTIVITYTIMEOUT_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		setting = new Setting();
		setting.setNamespace(SQSD_NAMESPACE);
		setting.setOptionName(VISIBILITYTIMEOUT_OPTIONNAME);
		setting.setValue(TEST_VISIBILITYTIMEOUT_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		setting = new Setting();
		setting.setNamespace(SQSD_NAMESPACE);
		setting.setOptionName(RETENTIONPERIOD_OPTIONNAME);
		setting.setValue(TEST_RETENTIONPERIOD_VALUE);
		createEnvTask.addConfiguredSetting(setting);
		createEnvTask.setTierName(WORKER_TIER);
		createEnvTask.setTierType(TEST_TIERTYPE);
		createEnvTask.setTierVersion(TEST_WORKERVERSION);
		createAppTask.setApplicationName(TEST_APPNAME);
		createEnvTask.setEnvironmentName(TEST_ENVIRONMENTNAME);
		createEnvTask.setApplicationName(TEST_APPNAME);
		createAppTask.execute();
		createEnvTask.execute();
		try {
			AWSTestUtils.waitForEnvironmentToTransitionToStateAndHealth(
					TEST_ENVIRONMENTNAME, EnvironmentStatus.Ready, null,
					bcClient);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail();
		}
		deployTask.execute();
		DescribeApplicationsResult appResult = bcClient.describeApplications();
		boolean appNameFound = false;
		for (ApplicationDescription appdesc : appResult.getApplications()) {
			if (appdesc.getApplicationName().equals(TEST_APPNAME)) {
				appNameFound = true;
				assertTrue(appdesc.getDescription().equals(TEST_APPDESC));
				break;
			}
		}
		assertTrue(appNameFound);
		boolean appDescFound = false;
		DescribeApplicationVersionsResult verResult = bcClient
				.describeApplicationVersions();
		for (ApplicationVersionDescription verdesc : verResult
				.getApplicationVersions()) {
			if (verdesc.getVersionLabel().equals(TEST_VERSIONLABEL)) {
				appDescFound = true;
				assertTrue(verdesc.getDescription().equals(TEST_VERSIONDESC));
				break;
			}
		}
		assertTrue(appDescFound);
		try {
			AWSTestUtils.waitForEnvironmentToTransitionToStateAndHealth(
					TEST_ENVIRONMENTNAME, EnvironmentStatus.Ready, null,
					bcClient);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail();
		}
		assertTrue(bcClient.describeEnvironments().getEnvironments().size() > 0);
		EnvironmentDescription environment = bcClient
				.describeEnvironments(
						new DescribeEnvironmentsRequest()
								.withEnvironmentNames(TEST_ENVIRONMENTNAME))
				.getEnvironments().get(0);
		assertEquals(TEST_ENVIRONMENTDESC, environment.getDescription());
		assertEquals(TEST_SOLUTIONSTACKNAME, environment.getSolutionStackName());
	}

	@AfterClass
	public static void tearDownAfterClass() throws InterruptedException {
		iamClient
				.removeRoleFromInstanceProfile(new RemoveRoleFromInstanceProfileRequest()
						.withRoleName("aws-elasticbeanstalk-ec2-role")
						.withInstanceProfileName(TEST_INSTANCEPROFILE_VALUE));
		iamClient.deleteInstanceProfile(new DeleteInstanceProfileRequest()
				.withInstanceProfileName(TEST_INSTANCEPROFILE_VALUE));
		AWSTestUtils.emptyAndDeleteBucket(s3Client, BUCKET_NAME);
	}

	@After
	public void tearDown() throws InterruptedException {
		bcClient.terminateEnvironment(new TerminateEnvironmentRequest()
				.withEnvironmentName(TEST_ENVIRONMENTNAME));
		AWSTestUtils.waitForEnvironmentToTransitionToStateAndHealth(
				TEST_ENVIRONMENTNAME, EnvironmentStatus.Terminated, null,
				bcClient);
		bcClient.deleteApplication(new DeleteApplicationRequest(TEST_APPNAME));
		while (true) {
			DescribeApplicationsResult appResult = bcClient
					.describeApplications();
			boolean appNameFound = false;
			for (ApplicationDescription appdesc : appResult.getApplications()) {
				if (appdesc.getApplicationName().equals(TEST_APPNAME)) {
					appNameFound = true;
					break;
				}
			}
			if (!appNameFound) {
				break;
			}
		}
	}
}
