AWS Java Ant Tasks
==================

Table of Contents
-----------------
* [General Usage](https://github.com/awslabs/aws-ant-tasks#usage-information)
* [Amazon S3 Tasks](https://github.com/awslabs/aws-ant-tasks#s3-task-usage-guide)
    * [Create bucket task](https://github.com/awslabs/aws-ant-tasks#create-bucket-task)
    * [Upload to bucket task](https://github.com/awslabs/aws-ant-tasks#upload-to-bucket-task)
    * [Download from bucket task](https://github.com/awslabs/aws-ant-tasks#download-from-bucket-task)
* [AWS Elastic Beanstalk Tasks](https://github.com/awslabs/aws-ant-tasks#-aws-elastic-beanstalk-task-usage-guide)
    * [Create Application Task](https://github.com/awslabs/aws-ant-tasks#create-application-task)
    * [Create Environment Task](https://github.com/awslabs/aws-ant-tasks#create-environment-task)
    * [Deploy Application Task](https://github.com/awslabs/aws-ant-tasks#deploy-application-task)
    * [Terminate Environment Task](https://github.com/awslabs/aws-ant-tasks#terminate-environment-task)
* [AWS OpsWorks tasks](https://github.com/awslabs/aws-ant-tasks#-aws-opsworks-task-usage-guide)
    * [Create Stack Task](https://github.com/awslabs/aws-ant-tasks#create-stack-task)
    * [Create Layer Task](https://github.com/awslabs/aws-ant-tasks#create-layer-task)
    * [Create Instance Task](https://github.com/awslabs/aws-ant-tasks#create-instance-task)
    * [Create App Task](https://github.com/awslabs/aws-ant-tasks#create-app-task)
    * [Deploy App Task](https://github.com/awslabs/aws-ant-tasks#deploy-app-task)
    * [Update App Task](https://github.com/awslabs/aws-ant-tasks#update-app-task)
    * [Incremental Deployment Task](https://github.com/awslabs/aws-ant-tasks#incremental-deployment-task)
* [AWS CloudFormation tasks](https://github.com/awslabs/aws-ant-tasks#aws-cloudformation-tasks-usage-guide)
    * [Create Stack Task](https://github.com/awslabs/aws-ant-tasks#create-stack-task-1)
    * [Update Stack Task](https://github.com/awslabs/aws-ant-tasks#update-stack-task)
    * [Set Stack Policy Task](https://github.com/awslabs/aws-ant-tasks#set-stack-policy-task)
    * [Wait For Stack To Reach State Task](https://github.com/awslabs/aws-ant-tasks#wait-for-stack-to-reach-state-task) 
    
Usage Information
-----------------

This project uses the AWS SDK for Java to create helpful Ant tasks for developers to incorporate into their builds. 
You can use these tasks to make your build process simpler and to automate the process of deployment.
To use the tasks, all you need to do is call taskdef, targeting "taskdefs.xml" in the jar.
```
<taskdef resource="taskdefs.xml" classpath="path/to/aws-java-sdk-ant-tasks-${version}.jar" />
```
The tasks will then be ready for you to call throughout the rest of the buildfile. For some usage examples, check out the integration-tests.xml. Read ahead for some more in-depth documentation.
Note that all tasks have two optional fields: `awsAccessKeyId` and `awsSecretKey`. You can use these to specify your AWS credentials, or you can leave them blank. If you leave them blank, the tasks will defer to the default credential chain, which looks for your credentials in the following places, in this order:
    Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_KEY
    Java System Properties - aws.accessKeyId and aws.secretKey
    Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI
    Instance profile credentials delivered through the Amazon EC2 metadata service 
If no valid credentials are found after looking in all 5 places, the task will default to anonymous access, which is likely to lead to errors.

S3 task usage guide
===================

Create bucket task
------------------

Defined in taskdefs.xml as `create-bucket`
The only required parameter here is the name of the bucket. Make sure it follows the rules of a bucket name and isn't taken, or an exception will be thrown.
```
<create-s3-bucket bucketName="mybucketname" />
```

Upload to bucket task
---------------------
Defined in taskdefs.xml as `upload-to-s3`
Upload to bucket

This uses Ant's fileset strategy. This makes it easy to specify either a single file or a fileset, which can use pattern sets. If you specify the keyPrefix parameter, then all files in the fileset will have that prefixed to their key in S3. Read up on Ant's filesets here.

Available attributes:

| Attribute      | Description                                                                                          | Required?                                                                                                                                                                                                            |
|----------------|------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId | Your AWS Access Key credential                                                                       | No. If not specified, the task will defer to the default credential chain.                                                                                                                                           |
| awsSecretKey   | Your AWS Secret Key credential                                                                       | No. If not specified, the task will defer to the default                                                                                                                                                             |
| bucketName     | The name of your bucket in S3                                                                        | Yes.                                                                                                                                                                                                                 |
| keyPrefix      | A prefix to append to the beginning of all keys.                                                     | No. If specified, the keys of the files you upload will be of the format: keyPrefix+fileName                                                                                                                         |
| continueOnFail | Whether to continue uploading files if one file in the fileset fails to upload. Defaults to "false". | No. Defaults to "false". If set to "true", the task will continue to upload the rest of the files in the set, even if one fails to upload. If left "false", an exception will be thrown if one file fails to upload. |
| printStatusUpdates | Display progress during uploading                                                                | No. If not specified, no progress will be displayed |

Nested elements:

Nested [filesets](https://ant.apache.org/manual/Types/fileset.html). At least one is required. Every file included in the fileset will be uploaded to S3.

Some examples:  

Uploading one file:
```
<upload-to-s3 bucketName="mybucketname">
     <fileset file="myfile.txt" />
</upload-to-s3>
```

Uploading a set of files:
```
<upload-to-s3 bucketName="mybucketname" keyPrefix="myprefix/">
     <fileset includes="*.txt"/>
</upload-to-s3>
```

Uploading a set of files, continuing if one happens to fail:
```
<upload-to-s3 bucketName="mybucketname" keyPrefix="myprefix/" continueOnFail="true"/>
     <fileset dir = "~/mydir">
</upload-to-s3>
```

Download from bucket task
-------------------------

Two major options for use here: To download a single file, specify a key, and if you want, a target file. If the target file is specified, the object from S3 will be downloaded to that file. If not specified, the name of the file will be equal to the key.

To download multiple files, specify a prefix and a target directory. All files that begin with the prefix will download to the target directory. Their names will be equal to their key names.

Available attributes: 

| Attribute      | Description                                                                                                           | Required?                                                                           |
|----------------|-----------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| awsAccessKeyId | Your AWS Access Key credential                                                                                        | No. If not specified, the task will defer to the default credential chain.          |
| awsSecretKey   | Your AWS Secret Key credential                                                                                        | No. If not specified, the task will defer to the default credential chain.          |
| bucketName     | The name of your bucket in S3                                                                                         | Yes.                                                                                |
| key            | If downloading one single S3 object, the key of the S3 Object you want to download.                                   | If you want to download single file. Otherwise, it cannot be set.                   |
| file           | If downloading one single S3 object, the absolute path of the file where the object will be downloaded to.            | No. Can only be set if you are downloading a single file (i.e. if key is specified).|
| keyPrefix      | If downloading multiple S3 objects, this specifies the prefix the keys of the objects you're downloading should have. | If you want to download mutliple files. Otherwise, it cannot be set.                |
| dir            | If downloading multiple S3 objects, the target directory to download them to.                                         | Yes, if you are downloading multiple files (i.e. if "keyPrefix" is specified).      |


Some examples:


Downloading one file to a target file:
```
<download-from-s3 bucketName="mybucketname" key="mykey.txt" file="targetfile.txt" />
```

Result: Downloads object with key "mykey.txt" in bucket "mybucketname" to "targetfile.txt".
(Note that in Ant, any relative paths resolve based on the project's basedir)

Downloading one file without a target file:
```
 <download-from-s3 bucketName="mybucketname" key="mykey.txt" />
```
Result: Downloads object with key "mykey.txt" in bucket "mybucketname" to "mykey.txt"

Downloading several files:
```
 <download-from-s3 bucketName="mybucketname" keyPrefix="myprefix/" dir="~/targetdir/" /> 
```
Result: Downloads all objects whose keys begin with "myprefix/" to "~/targetdir/", with file names equal to their keys.

 AWS Elastic Beanstalk Task usage guide
======================================

Create Application task
-----------------------

Sets up an application in AWS Elastic Beanstalk. Two attributes, both required: Application name and description. Parameters:

| Attribute              | Description                                              | Required?                                                                  |
|------------------------|----------------------------------------------------------|----------------------------------------------------------------------------|
| awsAccessKeyId         | Your AWS Access Key credential                           | No. If not specified, the task will defer to the default credential chain. |
| awsSecretKey           | Your AWS Secret Key credential                           | No. If not specified, the task will defer to the default credential chain. |
| applicationName        | The name of your AWS Elastic Beanstalk Application       | Yes                                                                        |
| applicationDescription | A description for your AWS Elastic Beanstalk application | Yes                                                                        |

Nested elements: 
None

Example code:

```
<create-beanstalk-app applicationName="mybeanstalkapp" applicationDescription="myapplication" /> 
```

Create Environment task
-----------------------
 
Creates an environment for your Application to run in. This task just creates the sample environment--to run your own application you will have to update this environment with the Deploy task. 
Parameters:

| Attribute             | Description                                                                                                                                                                                                  | Required?                                                                                |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| awsAccessKeyId        | Your AWS Access Key credential                                                                                                                                                                               | No. If not specified, the task will defer to the default credential chain.               |
| awsSecretKey          | Your AWS Secret Key credential                                                                                                                                                                               | No. If not specified, the task will defer to the default credential chain.               |
| applicationName       | The name of the application to attach this environment to                                                                                                                                                    | Yes                                                                                      |
| environmentName       | The name of this environment                                                                                                                                                                                 | Yes                                                                                      |
| environmentDescription| A description of this environment                                                                                                                                                                            | Yes                                                                                      |
| solutionStackName     | The solution stack of this environment. Must match one of the available solution stack names.                                                                                                                | Yes                                                                                      |
| tierName              | Name of this environment's tier.                                                                                                                                                                             | Conditional: If one of tierName, tierType, tierVersion is set, then they must all be set |
| tierType              | Type of this environment's tier                                                                                                                                                                              | Conditional: If one of tierName, tierType, tierversion is set, then they must all be set  |
| tierVersion           | Version of this environment's tier                                                                                                                                                                           | Conditional: If one of tierName, tierType, tierVersion is set, then they must all be set |
| cnamePrefix           | CNAME prefix for this environment, will be prefixed to this environment's URL if specified. Must be between 4-23 characters, contain only letters, numbers, and hyphens, and not begin or end with a hyphen. | No                                                                                       |
| versionLabel          | Version of the application                                                                                                                                                                                   | No                                                                                       |

Nested elements:

Nested Settings. A setting is a wrapper for an [Option Value](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/command-options.html), and similarly has three fields: namespace, optionName, and value

Example code: Simple web service:

```
<create-beanstalk-env applicationName="mybeanstalkapp" cnamePrefix="myprefix" environmentName="mybeanstalkenv" environmentDescription="myEnv" solutionStackName="64bit AmazonLinux running Tomcat 6">
     <Setting namespace="aws:autoscaling:launchconfiguration" optionname="IAmInstanceProfile" value="ElasticBeanstalkProfile />
</create-beanstalk-env>
```

More advanced Worker tier:

```
<create-beanstalk-env applicationName="mybeanstalkapp" environmentName="mybeanstalkenv" environmentDescription="myEnv" solutionStackName="64bit AmazonLinux running Tomcat 6" tierName="Worker" tierType="SQS/HTTP" tierVersion="1.0">
     <Setting namespace="aws:autoscaling:launchconfiguration" optionName="IAmInstanceProfile" value="ElasticBeanstalkProfile />
     <Setting namespace="aws:elasticbeanstalk:sqsd" optionName="WorkerQueueURL" value="sqsd.elasticbeanstalk.us-east-1.amazon.com" />
     <Setting namespace="aws:elasticbeanstalk:sqsd" optionName="HttpPath" value="/" />
     <Setting namespace="aws:elasticbeanstalk:sqsd" optionName="MimeType" value="application/json" />
     <Setting namespace="aws:elasticbeanstalk:sqsd" optionName="HttpConnections" value="75" />
     <Setting namespace="aws:elasticbeanstalk:sqsd" optionName="ConnectTimeout" value="10" />
     <Setting namespace="aws:elasticbeanstalk:sqsd" optionName="InactivityTimeout" value="10" />
     <Setting namespace="aws:elasticbeanstalk:sqsd" optionName="VisibilityTimeout" value="60" />
     <Setting namespace="aws:elasticbeanstalk:sqsd" optionName="RetentionPeriod" value="345600" />
</create-beanstalk-env>
```

Deploy Application task
-----------------------

This task creates a new version of your application with an application file you've specified, then updates the environment to the new version. You can either specify an application file that you've already uploaded in S3, or you can upload a local application file to S3.
 Parameters: 
 
| Attribute              | Description                                                                                                                                                                                       | Required?                                                                  |
|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| awsAccessKeyId         | Your AWS Access Key credential                                                                                                                                                                    | No. If not specified, the task will defer to the default credential chain. |
| awsSecretKey           | Your AWS Secret Key credential                                                                                                                                                                    | No. If not specified, the task will defer to the default credential chain. |
| bucketName             | This is either: The name of the bucket you want to upload your application file to, or the bucket your application is already in. Either way , it must be a valid bucket that you have access to. | Yes.                                                                       |
| versionLabel           | New version label for the deployed application version                                                                                                                                            | Yes.                                                                       |
| versionDescription     | Description of the new version of this application                                                                                                                                                | Yes.                                                                       |
| applicationName        | The name of the application to attach the new version to.                                                                                                                                         | Yes.                                                                       |
| environmentName        | The name of the environment to update                                                                                                                                                             | Yes.                                                                       |
| key                    | The key of your application file in S3                                                                                                                                                            | Conditional. Either this, or file must be set.                             |
| file                   | The local file to upload to S3 and set as your application file                                                                                                                                   | Conditional. Either this, or key must be set.                              |

Example code specifying a file you've already uploaded to S3:
```
<deploy-beanstalk-app bucketName="mybucket" key="application/myapp.war" versionLabel="Version1" versionDescription="myversion" applicationName="mybeanstalkapp" environmentName="mybeanstalkenv />
```
Example code uploading your own local file:
```
<deploy-beanstalk-app bucketName="mybucket" file="path/to/myapp.war" versionLabel="Version1" versionDescription="myversion" applicationName="mybeanstalkapp" environmentName="mybeanstalkenv" />
```

Terminate environment task
--------------------------

This task terminates your environment. There is one required field: The environment's name.

Parameters:

| Attribute              | Description                                        | Required?                                                                  |
|------------------------|----------------------------------------------------|----------------------------------------------------------------------------|
| awsAccessKeyId         | Your AWS Access Key credential                     | No. If not specified, the task will defer to the default credential chain. |
| awsSecretKey           | Your AWS Secret Key credential                     | No. If not specified, the task will defer to the default credential chain. |
| environmentName        | The name of your AWS Elastic Beanstalk Environment | Yes                                                                        |

 AWS OpsWorks Task usage guide
======================================

Create Stack Task
-----------------

Creates a stack in OpsWorks, and sets the "stackID" property to the resulting stack so that you can reference it later in the project as `${stackId}` (Though you can override the property to set)

Parameters:

| Attribute                 | Description                                                                                                                                                 | Required?                                                                                                                          |
|---------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId            | Your AWS Access Key credential                                                                                                                              | No. If not specified, the task will defer to the default credential chain.                                                         |
| awsSecretKey              | Your AWS Secret Key credential                                                                                                                              | No. If not specified, the task will defer to the default credential chain.                                                         |
| name                      | The name of this stack                                                                                                                                      | Yes.                                                                                                                               |
| region                    | The region of this stack.                                                                                                                                   | Yes.                                                                                                                               |
| serviceRoleArn            | The IAM role which will allow OpsWorks to access AWS resources on your behalf. Must be a valid role with proper access permissions that you have access to. | Yes.                                                                                                                               |
| defaultInstanceProfileArn | The IAM role to be the default profile for all of this stack's EC2 instances. Must be a valid instance profile that you have access to.                     | Yes.                                                                                                                               |
| repoType                  | The type of the repository your cookbook is stored in.                                                                                                      | If repoUrl is set, this must be set. If useCustomCookbooks is false, this cannot be set.                                           |
| repoUrl                   | The URL leading to the source of your cookbook.                                                                                                             | If repoType is set, this must be set. If useCustomCookbooks is false, this cannot be set.                                          |
| repoUsername              | The username needed to access your cookbook repository.                                                                                                     | If repoPassword is set, this must be set. If useCustomCookbooks is false, this cannot be set.                                      |
| repoPassword              | The password needed to access your cookbook repository.                                                                                                     | If repoUsername is set, this must be set. If useCustomCookbooks is false, this cannot be set.                                      |
| repoSshKey                | The SSH key for your cookbook repository.                                                                                                                   | Only if you need an SSH key to access the repository you are trying to access. If useCustomCookbooks is false, this cannot be set. |
| repoRevision              | The revision of the source for your cookbook repository to use.                                                                                             | No. If useCustomCookbooks is false, this cannot be set.                                                                            |
| vpcId                     | The ID of the VPC the stack will be launched into                                                                                                           | No.                                                                                                                                |
| defaultAvailabilityZone   | The stack's default availability zone.                                                                                                                      | No.                                                                                                                                |
| defaultOs                 | The stack's default operating system. This must be set to Amazon Linux or Ubuntu 12.04 LTS.                                                                 | No.                                                                                                                                |
| defaultRootDeviceType     | The default root device type of this stack.                                                                                                                 | No.                                                                                                                                |
| hostnameTheme             | This stack's host name theme.                                                                                                                               | No.                                                                                                                                |
| customJson                | A string of custom JSON to use to override the default corresponding stack configuration values. Must be correctly formed and properly escaped.             | No.                                                                                                                                |
| berkshelfVersion          | The version of Berkshelf to use.                                                                                                                            | No.                                                                                                                                |
| startOnCreate             | Whether to start this stack when it is created.                                                                                                             | No. Has a default of "true"                                                                                                        |
| useOpsworksSecurityGroups | Whether to associate the OpsWorks built-in security groups with this stack's layers.                                                                        | No. Has a default of "true"                                                                                                        |
| useCustomCookbooks        | Whether this stack will use custom cookbooks.                                                                                                               | No. Has a default of "false"                                                                                                       |
| manageBerkshelf           | Whether to enable Berkshelf.                                                                                                                                | No. Has a default of "false"                                                                                                       |
| chefVersion               | The version of Chef to use.                                                                                                                                 | No. Has a default of 11.4                                                                                                          |
| propertyNameForStackId    | The name of the property to set this stack's ID to.                                                                                                         | No. Has a default of "stackId"                                                                                                     |

Nested elements:
Nested StackAttributes. StackAttributes are simply key-value pairs to associate with a stack. StackAttributes are used simply as <StackAttribute key="..." value="..." /> (Both fields are required)
No nested elements are required.

Example code:

```
<create-opsworks-stack name="MyStack" region="us-east-1" defaultInstanceProfileArn="aws-opsworks-ec2-role" serviceRoleArn="aws-opsworks-service-role" />
<echo message="New stack (id=${stackId}) is created." />
```
Result: Creates an OpsWorks stack named "MyStack," starts the stack,  then sets the "stackId" property to the ID of the created stack.

Example code using more parameters and nested elements:

```
<create-opsworks-stack name="MyStack" region="us-east-1" defaultInstanceProfileArn="aws-opsworks-ec2-role" serviceRoleArn="aws-opsworks-service-role"
    defaultOs="Amazon Linux" defaultAvailabilityZone="us-east-1b" defaultRootDeviceType="Instance store" propertyForStackId="myStackId">
        <StackAttribute key="examplekey" value="examplevalue" />
</create-opsworks-stack>
<echo message="New stack (id=${myStackId}) is created." />
```
Result: Creates an OpsWorks stack named "MyStack" according to the set parameters, starts the stack, then sets the "myStackId" property to the ID of the created stack.

Create Layer Task
-----------------

Creates a layer in OpsWorks according to the parameters you set.

Parameters:

| Attribute                | Description                                                                                                         | Required?                                                                                                                                   |
|--------------------------|---------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId           | Your AWS Access Key credential                                                                                      | No. If not specified, the task will defer to the default credential chain.                                                                  |
| awsSecretKey             | Your AWS Secret Key credential                                                                                      | No. If not specified, the task will defer to the default credential chain.                                                                  |                                 
| stackId                  | The ID of the stack this layer will reside in.                                                                      | If not specified, the task will use the value of the "stackId" property. It is required that either that property or this attribute be set. |
| type                     | The type of this layer.                                                                                             | Yes.                                                                                                                                        |
| name                     | The name of this layer.                                                                                             | Yes.                                                                                                                                        |
| shortname                | The shortname of this layer.                                                                                        | Yes.                                                                                                                                        |
| customInstanceProfileArn | The ARN of an IAM profile to use for this layer's EC2 instances.                                                    | No.                                                                                                                                         |
| enableAutoHealing        | Whether to enable auto healing for this layer.                                                                      | No. Has a default of "true"                                                                                                                 |
| autoAssignPublicIps      | Whether to automatically assign a public IP address to the layer's instances, for stacks that are running in a VPC. | No. Has a default of "true"                                                                                                                 |
| installUpdatesOnBoot     | Whether to install operating system and package updates on boot.                                                    | No. Has a default of "true". It is hightly recommended you leave this as "True"                                                             |
| useEbsOptimizedInstances | Whether to use Amazon EBS-Optimized instances.                                                                      | No. Has a default of "true"                                                                                                                 |
| autoAssignElasticIps     | Whether to automatically assign an elastic IP address to this layer's instances                                     | No. Has a default of "false"                                                                                                                |
| propertyNameForLayerId   | The property name to assign this layer's ID to.                                                                     | No, but recommended if you want to refer to this layer later in the build.                                                                  |

Nested elements:

Nested LayerAttributes. LayerAttributes are simply key-value pairs to associate with a layer. LayerAttributes are used simply as <LayerAttribute key="..." value="..." /> (Both fields are required)

Nested CustomSecurityGroupIds. They have only one field, "value," which should be the customSecurityGroupId you want to use for this layer. Used as <CustomSecurityGroupId value="..." />

Nested LayerPackages. They have only one field, "value," which should be used to define the package. Used as <LayerPackage value="..." />

Nested LayerRecipes. They have two fields, "name," the name of the recipe, and "phase," the phase in which to execute the recipe. Used as <LayerRecipe name="..." phase="..." />

Nested LayerVolumeConfigurations. LayerVolumeConfigurations have the following parameters:

| Attribute     | Description                            |
|---------------|----------------------------------------|
| iops          | The IOPS per disk (For PIOPS volumes). |
| numberOfDisks | The number of disks in this volume.    |
| raidLevel     | The volume RAID level (Integer).       |
| size          | The size of this volume (Integer).     |
| volumeType    | The volume type.                       |
| mountPoint    | The volume mount point.                |

Used as <LayerVolumeConfiguration iops="..." numberOfDisks="..." raidLevel="..." size="..." volumeType="..." mountPoint="..." />

No nested elements are required.

Example code:

```
<create-opsworks-layer name="MyOpsWorksLayer" type="java-app" shortname="opsworkslayer" propertyNameForLayerId="layerId" stackId=${previously-specified-stack-id} />
```

Create Instance Task
--------------------

Creates an EC2 Instance according to the parameters you set.

Parameters:

| Attribute            | Description                                                                                                      | Required?                                                                                                                                   |
|----------------------|------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId       | Your AWS Access Key credential                                                                                   | No. If not specified, the task will defer to the default credential chain.                                                                  |
| awsSecretKey         | Your AWS Secret Key credential                                                                                   | No. If not specified, the task will defer to the default credential chain.                                                                  |
| stackId              | The ID of the stack to associate this instance with.                                                             | If not specified, the task will use the value of the "stackId" property. It is required that either that property or this attribute be set. |
| instanceType         | The type of this instance, such as m1.small, t2.medium, etc.                                                     | No.                                                                                                                                         |
| availabilityZone     | The availability zone of this instance                                                                           | No.                                                                                                                                         |
| virtualizationType   | The instances virtualization type. Should be paravirtual or hvm                                                  | No.                                                                                                                                         |
| subnetId             | The ID of this instance's subnet.                                                                                | No.                                                                                                                                         |
| autoScalingType      | The type of autoscaling to use for this instance.                                                                | No.                                                                                                                                         |
| amiId                | A custom AMI ID to be used to create this instance                                                               | No. Should be set if you set "os" to "Custom"                                                                                               |
| sshKeyName           | The IAM SSH key name used to SSH into this instance.                                                             | No, but needed if you want to SSH into this instance                                                                                        |
| os                   | The operating system to use for this instance.                                                                   | No. Has a default of "Amazon Linux"                                                                                                         |
| architecture         | The architecture of this instance.                                                                               | No. Has a default of "x86_64".                                                                                                              |
| rootDeviceType       | The root device type of this instance                                                                            | No. Has a default of "Ebs"                                                                                                                  |
| installUpdatesOnBoot | Whether to install OS and package updates when this instance                                                     | No. Has a default of "true," and it is highly recommended that you leave it as "True".                                                      |
| ebsOptimized         | Whether to create an Amazon EBS-Optimized instance.                                                              | No. Has a default of "false"                                                                                                                |
| useProjectLayerIds   | Whether to add all the IDs of all layers created earlier in this project to the layerIds group of this instance. | No. Has a default of "true"                                                                                                                 |
| startOnCreate        | Whether to start this instance at the end of the execution of this task.                                         | No. Has a default of "true"                                                                                                                 |

Nested elements:

Nested LayerIds. LayerIds only have one field, "value," used to specify the ID of a layer to associate this instance with. You can specify as many as you want, but you must specify at least one. Used as <LayerId value="..." />
If you create a Layer earlier in the build, and "useProjectLayerIds" is set to true, you don't have to specify layerIds because it will use the layers created earlier in the build.

Example code:

```
<create-opsworks-instance instanceType="m1.small" availabilityZone="us-east-1b" propertyNameForInstanceId="instanceId" stackId="${previously-specified-stack-id} >
    <LayerId value=${previously-defined-layer-id} />
</create-opsworks-instance>
```
Result: Creates an OpsWorks instance associated with the given layerId, and sets the "instanceId" property to the ID of the resulting instance.


Create App Task
---------------

Creates an OpsWorks application according to the paramters you set, using the app source you specify.

Parameters: 

| Attribute            | Description                                                                       | Required?                                                                                                                                   |
|----------------------|-----------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId       | Your AWS Access Key credential                                                    | No. If not specified, the task will defer to the default credential chain.                                                                  |
| awsSecretKey         | Your AWS Secret Key credential                                                    | No. If not specified, the task will defer to the default credential chain.                                                                  |
| stackId              | The ID of the stack for this app to reside in.                                    | If not specified, the task will use the value of the "stackId" property. It is required that either that property or this attribute be set. |
| name                 | The name of this app.                                                             | Yes.                                                                                                                                        |
| type                 | Set the type of this app.                                                         | Yes.                                                                                                                                        |
| shortname            | The shortname of this app. Must be in all lowercase.                              | No.                                                                                                                                         |
| description          | A description for this app. Not required.                                         | No.                                                                                                                                         |
| repoSshKey           | The SSH key for your app repository.                                              | If you need a key to access your repository.                                                                                                |
| repoType             | The type of the repository your app is stored in.                                 | No. If repoUrl is set, this must be set.                                                                                                    |
| repoUrl              | The URL leading to the source of your app.                                        | No. If repoType is set, this must be set.                                                                                                   |
| repoUsername         | The username to use to access your repository.                                    | No. If repoPassword is set, this must be set.                                                                                               |
| repoPassword         | The password to access your repository.                                           | No. If repoUsername is set, this must be set.                                                                                               |
| repoRevision         | The revision of the source to use.                                                | No.                                                                                                                                         |
| sslCertificate       | The SSL certificate for the SSL configuration of this app.                        | No. If enableSsl is "false," this cannot be set.                                                                                            |
| sslPrivateKey        | The private key for the SSL configuration of this app.                            | No. If enableSsl is "false," this cannot be set.                                                                                            |
| sslChain             | The chain for the SSL configuration of this app.                                  | No. If enableSsl is "false," this cannot be set.                                                                                            |
| propertyNameForAppId | The name of the property to set this app's ID.                                    | No. Has a default of "appId".                                                                                                               |
| enableSsl            | Whether to enable SSL for this app.                                               | No. Has a default of "true".                                                                                                                |
| useAwsKeysForRepo    | Whether to use the default credential chain to set repoUsername and repoPassword. | No. Has a default of "false".                                                                                                               |

Nested elements:

Nested AppAttributes. AppAttributes are simply key-value pairs to associate with an app. AppAttributes are used simply as <AppAttribute key="..." value="..." /> (Both fields are required)

Nested Domains. Domains only have on element, "value," which is used to specify a custom domain. Used as <Domain value="..." />

Nested DataSources. DataSources have three elements: "type," "arn," and "databaseName," used to specify the type of data source, the arn of the data source, and the name of a database to use. Used as <DataSource type="..." arn="..." databaseName="..." />

No nested elements are required.

Example code: 

```
<create-opsworks-app name="MyApp" type="java" repoType="s3" repoUrl="https://s3.amazonaws.com/mypublicbucket/mypublicjavaapp.war" stackId=${previously-defined-stack-id}/>
<echo message="New app (id=${appId}) is created." />
```

Creates an OpsWorks app called "MyApp" in OpsWorks, using a public app repository, then sets the "appId" property to the resulting app's ID.

Another example:

```
<create-opsworks-app name="MyApp" type="java" repoType="s3" repoUrl="https://s3.amazonaws.com/myprivatebucket/myprivatejavaapp.war" useAwsKeysForRepo="true" propertyNameForAppId="myAppId" stackId=${previously-defined-stack-id}/>
<echo message="New app (id=${myAppId}) is created." />
```

Creates an OpsWorks app called "myApp" in OpsWorks using the default credential chain to find credentials to access you private repository, then sets the "myAppId" property to the ID of the resulting app.


Deploy App Task
---------------

Deploys an OpsWorks app to a set of EC2 instances.

| Attribute                   | Description                                                                     | Required?                                                                                                                                   |
|-----------------------------|---------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId              | Your AWS Access Key credential                                                  | No. If not specified, the task will defer to the default credential chain.                                                                  |
| awsSecretKey                | Your AWS Secret Key credential                                                  | No. If not specified, the task will defer to the default credential chain.                                                                  |
| stackId                     | The ID of the stack for this app to reside in.                                  | If not specified, the task will use the value of the "stackId" property. It is required that either that property or this attribute be set. |
| appId                       | The ID of the app to deploy.                                                    | If not specified, the task will use the value of the "appId" property. It is required that either that property or this attribute be set.   |
| comment                     | A user-defined comment.                                                         | No.                                                                                                                                         |
| customJson                  | User-defined, custom JSON used to override stack configuration JSON attributes. | No. If specified, must be well-formed and properly escaped JSON.                                                                            |
| propertyNameForDeploymentId | The name of the property to set this deployment's ID.                           | No.                                                                                                                                         |

Nested elements:

Nested InstanceIds. InstanceIDs have one field, "value," used to specify the ID of an instance to deploy the app to. Used as <InstanceID value="..." />

Nested Commands. Commands have one element, "name," the name of the command, as well as any number of nested Args. Args themselves have one element, "name," the name of the arg, as well as any number of nested ArgVals. ArgVals have one field, "value," used to specify the argument value.
Commands are used as <Command name="..." /> or as

```
<Command name="...">
    <Arg name="...>
        <ArgVal value="..."/>
        <ArgVal value="..."/>
    </Arg>
</Command>
```
You must specify exactly one Command.

Example code: 

```
<deploy-opsworks-app propertyNameForDeploymentId="deploymentId1" comment="deploying v1 of MyApp" stackId="${previously-specified-stack-id" instanceId="${previously-specified-instance-id} >
    <Command name="deploy" />
    <InstanceId value="${previously-specified-instance-id}"/>
</deploy-opsworks-app>
```

Update App Task
---------------

Updates an OpsWorks app according to the specified parameters. 

Parameters: 

| Attribute            | Description                                                                       | Required?                                                                                                                                   |
|----------------------|-----------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId       | Your AWS Access Key credential                                                    | No. If not specified, the task will defer to the default credential chain.                                                                  |
| awsSecretKey         | Your AWS Secret Key credential                                                    | No. If not specified, the task will defer to the default credential chain.                                                                  |
| appId                | The ID of the app to update.                                                      | If not specified, the task will use the value of the "appId" property. It is required that either that property or this attribute be set.   |
| name                 | The name of this app.                                                             | Yes.                                                                                                                                        |
| type                 | Set the type of this app.                                                         | Yes.                                                                                                                                        |
| description          | A description for this app. Not required.                                         | No.                                                                                                                                         |
| repoSshKey           | The SSH key for your app repository.                                              | If you need a key to access your repository.                                                                                                |
| repoType             | The type of the repository your app is stored in.                                 | No. If repoUrl is set, this must be set.                                                                                                    |
| repoUrl              | The URL leading to the source of your app.                                        | No. If repoType is set, this must be set.                                                                                                   |
| repoUsername         | The username to use to access your repository.                                    | No. If repoPassword is set, this must be set.                                                                                               |
| repoPassword         | The password to access your repository.                                           | No. If repoUsername is set, this must be set.                                                                                               |
| repoRevision         | The revision of the source to use.                                                | No.                                                                                                                                         |
| sslCertificate       | The SSL certificate for the SSL configuration of this app.                        | No. If enableSsl is "false," this cannot be set.                                                                                            |
| sslPrivateKey        | The private key for the SSL configuration of this app.                            | No. If enableSsl is "false," this cannot be set.                                                                                            |
| sslChain             | The chain for the SSL configuration of this app.                                  | No. If enableSsl is "false," this cannot be set.                                                                                            |
| enableSsl            | Whether to enable SSL for this app.                                               | No. Has a default of "true".                                                                                                                |
| useAwsKeysForRepo    | Whether to use the default credential chain to set repoUsername and repoPassword. | No. Has a default of "false".                                                                                                               |

Nested elements:

Nested Domains. Domains only have on element, "value," which is used to specify a custom domain. Used as <Domain value="..." />

Nested DataSources. DataSources have three elements: "type," "arn," and "databaseName," used to specify the type of data source, the arn of the data source, and the name of a database to use. Used as <DataSource type="..." arn="..." databaseName="..." />

No nested elements are required.

Example code: 

```
<udpate-opsworks-app name="NewAppName" type="java" repoType="s3" repoUrl="https://s3.amazonaws.com/mypublicbucket/mypublicjavaapp.war" appId=${previously-defined-app-id} />
```

Incremental Deployment Task
---------------------------

To use this task, you specify deployment groups, which have any number of nested <deploy-opsworks-app> elements. All <deploy-opsworks-app> tasks in the same <DeploymentGroup> will run in parallel, but the task will not proceed to the next deployment group until all deployments in the group succeed.
The only elements in this task are nexted DeploymentGroups (Used as <DeploymentGroup><!--deployments here--></DeploymentGroup>) which themselves have nested <deploy-opsworks-app> tasks.

Example code:

```
<incremental-opsworks-deployment>
    <DeploymentGroup>
        <deploy-opsworks-app propertyNameForDeploymentId="deploymentId1">
            <Command name="deploy" />
            <InstanceId value="${previously-defined-instanceId1}"/>
        </deploy-opsworks-app>
        <deploy-opsworks-app propertyNameForDeploymentId="deploymentId2">
            <Command name="deploy" />
            <InstanceId value="${previously-defined-instanceId2}"/>
        </deploy-opsworks-app>
    </DeploymentGroup>
    <DeploymentGroup>
        <deploy-opsworks-app propertyNameForDeploymentId="deploymentId3">
            <Command name="deploy" />
            <InstanceId value="${previously-defined-instanceId3}"/>
        </deploy-opsworks-app>
        <deploy-opsworks-app propertyNameForDeploymentId="deploymentId4">
            <Command name="deploy" />
            <InstanceId value="${previously-defined-instanceId4}"/>
        </deploy-opsworks-app>
    </DeploymentGroup>
    <DeploymentGroup>
        <deploy-opsworks-app propertyNameForDeploymentId="deploymentId5">
            <Command name="deploy" />
            <InstanceId value="${previously-defined-instanceId5}"/>
        </deploy-opsworks-app>
    </DeploymentGroup>
</incremental-opsworks-deployment>
```

Result: deploys deploymentId1 and deploymentId2, blocks until they finish, then deploys deploymentId3 and deployment Id4, blocks until they finish, finally deploys deploymentId5 and blocks until it finishes.

AWS CloudFormation Tasks Usage Guide
======================================

Create stack task
------------------

This task creates a stack in CloudFormation using either a URL you specify, or a (well-formed, escaped) string specified right in the body of the task call.

Parameters:

| Attribute        | Description                                                                 | Required?                                                                                                                                |
|------------------|-----------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId   | Your AWS Access Key credential                                              | No. If not specified, the task will defer to the default credential chain.                                                               |
| awsSecretKey     | Your AWS Secret Key credential                                              | No. If not specified, the task will defer to the default credential chain.                                                               |
| onFailure        | An action to execute on failure.                                            | No. If this element is set, disableRollback should not be set. If disableRollback is set, this should not be set.                        |
| stackName        | What to name this stack.                                                    | Yes.                                                                                                                                     |
| stackPolicyBody  | Well formed, properly escaped JSON specifying a stack policy.               | No. If this is set, stackPolicyURL cannot be set. If stackPolicyURL is set, this cannot be set.                                          |
| stackPolicyUrl   | A valid URL pointing to a JSON object specifying a stack policy.            | No. If this is set, stackPolicyBody cannot be set. If stackPolicyBody is set, this cannot be set.                                        |
| templateBody     | Well formed, properly escaped JSON specifying a stack policy.               | If this is set, templateURL cannot be set. If templateURL is set, this cannot be set. It is required that this or templateURL be set.    |
| templateURL      | A valid URL pointing to a JSON object specifying a template.                | If this is set, templateBody cannot be set. If templateBody is set, this cannot be set. It is required that this or templateBody be set. |
| disableRollback  | Whether to disable rollback if stack creation fails.                        | No. Has a default of "false". If this is set, onFailure cannot be set.                                                                   |
| waitForCreation  | Whether to block the build until this stack successfully finishes creation. | No. Has a default of "false"                                                                                                             |
| timeoutInMinutes |  The amount of time to allow the stack to take to create before failing.    | Yes, must be greater than 0.                                                                                                             |

Nested elements:

Nested StackCapabilities. A StackCapability has only one element, "value," which is used to specify a capability you want to add to a stack. Used as <StackCapability value="..." />

Nested NotificationArns. A NotificationArn has only one element, "value," which is used to specify a NotificationArn you want to add to the stack. Used as <NotificationArn value="..." />

Nested StackParameters. A StackParameter is a key-value pair, where "key" is the name of the stack parameter to set, and "value" is the parameter's value. 
Instead of specifying "value," you can set "usePreviousValue" to "true", and the previous value will be used. Used as <StackParameter key="..." value="..." /> or <StackParameter key=".." usePreviousValue="true" />

Nested StackTags. StackTags are simply key value pairs to associate with the stack. Used as <StackTag key="..." value="..." />

No nested elements are required by the task, however the template you specified may require that you specify some StackParameters. 

Example code:

```
<create-cloudformation-stack stackName="anttaskteststack" timeoutInMinutes="100"
templateURL="https://s3-us-west-2.amazonaws.com/cloudformation-templates-us-west-2/WordPress_Multi_AZ.template">
    <StackParameter key="KeyName" value="myKeyName" />
</create-cloudformation-stack>
```

Result: Uses [this](https://s3-us-west-2.amazonaws.com/cloudformation-templates-us-west-2/WordPress_Multi_AZ.template) template to create "anttaskteststack", specifying that the build should fail if it takes more than 100 minutes. Uses "myKeyName" as the key pair for your EC2 instances.

Update Stack Task
-----------------

Updates an existing CloudFormation stack according to the parameters you set. 

Parameters: 

| Attribute                   | Description                                                                                                                                               | Required?                                                                                                                                                                                                                                    |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId              | Your AWS Access Key credential                                                                                                                            | No. If not specified, the task will defer to the default credential chain.                                                                                                                                                                   |
| awsSecretKey                | Your AWS Secret Key credential                                                                                                                            | No. If not specified, the task will defer to the default credential chain.                                                                                                                                                                   |
| stackName                   | The name of the stack to update.                                                                                                                          | Yes.                                                                                                                                                                                                                                         |
| stackPolicyBody             | Well formed, properly escaped JSON specifying a stack policy.                                                                                             | No. If this is set, stackPolicyURL cannot be set. If stackPolicyURL is set, this cannot be set.                                                                                                                                              |
| stackPolicyUrl              | A valid URL pointing to a JSON object specifying a stack policy.                                                                                          | No. If this is set, stackPolicyBody cannot be set. If stackPolicyBody is set, this cannot be set.                                                                                                                                            |
| templateBody                | Well formed, properly escaped JSON specifying a stack policy.                                                                                             | If this is set, templateURL cannot be set. If templateURL is set, this cannot be set. It is required that this or templateURL be set, or that usePreviousTemplate be set to true. If usePreviousTemplate is true, this should not be set.    |
| templateURL                 | A valid URL pointing to a JSON object specifying a template.                                                                                              | If this is set, templateBody cannot be set. If templateBody is set, this cannot be set. It is required that this or templateBody be set, or that usePreviousTemplate be set to true. If usePreviousTemplate is true, this should not be set. |
| stackPolicyDuringUpdateBody | Well formed, properly escaped JSON specifying a stack policy to use during this update only, overriding the current policy until the update completes.    | No. If this is set, stackPolicyDuringUpdateURL cannot be set. If stackPolicyDuringUpdateURL is set, this cannot be set.                                                                                                                      |
| stackPolicyDuringUpdateURL  | A valid URL pointing to a JSON object specifying a stack policy to use during this update only, overriding the current policy until the update completes. | No. If this is set, stackPolicyDuringUpdateBody cannot be set. If stackPolicyDuringUpdateBody is set, this cannot be set.                                                                                                                    |
| usePreviousTemplate         | Whether to use the previous template during this update.                                                                                                  | No. If this is set, templateURL and templateBody should not be set.                                                                                                                                                                          | 

Nested elements: 

Nested StackCapabilities. A StackCapability has only one element, "value," which is used to specify a capability you want to add to a stack. Used as <StackCapability value="..." />

Nested NotificationArns. A NotificationArn has only one element, "value," which is used to specify a NotificationArn you want to add to the stack. Used as <NotificationArn value="..." />

Nested StackParameters. A StackParameter is a key-value pair, where "key" is the name of the stack parameter to set, and "value" is the parameter's value. 
Instead of specifying "value," you can set "usePreviousValue" to "true", and the previous value will be used. Used as <StackParameter key="..." value="..." /> or <StackParameter key=".." usePreviousValue="true" />

No nested elements are required by the task, however the template you specified may require that you specify some StackParameters. 

Example code:

```
<update-cloudformation-stack stackName="anttaskteststack" usePreviousTemplate="true">
    <StackParameter key="KeyName" usePreviousValue="true" />
    <StackParameter key="WebServerCapacity" value="3" />
</update-cloudformation-stack>
```

Result: updates "anttaskteststack", using the same template as before and keeping the key name the same, but setting the web server capacity to 3. 

Set Stack Policy Task
---------------------

Updates the stack policy of a specified stack.

Parameters:

| Attribute       | Description                                                      | Required?                                                                                                                                                   |
|-----------------|------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| awsAccessKeyId  | Your AWS Access Key credential                                   | No. If not specified, the task will defer to the default credential chain.                                                                                  |
| awsSecretKey    | Your AWS Secret Key credential                                   | No. If not specified, the task will defer to the default credential chain.                                                                                  |                                 
| stackName       | The name of the stack to apply the update to.                    | Yes.                                                                                                                                                        |
| stackPolicyBody | Well formed, properly escaped JSON specifying a stack policy.    | If this is set, stackPolicyURL cannot be set. If stackPolicyURL is set, this cannot be set. It is required that either this or stackPolicyUrl be set.       |
| stackPolicyURL  | A valid URL pointing to a JSON object specifying a stack policy. | If this is set, stackPolicyBody cannot be set. If stackPolicyBody is set, this cannot be set. It is required that either this or stackPolicyBody be set.    |

Nested elements:

None.
Example code:

```
 <set-cloudformation-stack-policy stackName="anttaskteststack"
            stackPolicyBody="{
                   &quot;Statement&quot;: [{
                    &quot;Effect&quot;: &quot;Deny&quot;,
                    &quot;Action&quot;: &quot;Update:*&quot;,
                    &quot;Principal&quot;: &quot;*&quot;,
                    &quot;Resource&quot;: &quot;LogicalResourceId/DBInstance&quot;
                }, {
                    &quot;Effect&quot;: &quot;Allow&quot;,
                    &quot;Action&quot;: &quot;Update:*&quot;,
                    &quot;Principal&quot;: &quot;*&quot;,
                    &quot;Resource&quot;: &quot;*&quot;
                }]
            }" />
```

Result: Updates "anttaskteststack" with the specified JSON object. This policy prevents any updates to the stack's DBInstance, but allows all other updates.

Wait For Stack To Reach State Task
----------------------------------

Blocks the build until the specified stack reaches the specified state. Fails the build if the state contains "FAILED".

Parameters:

| Attribute       | Description                                 | Required?                                                                  |
|-----------------|---------------------------------------------|----------------------------------------------------------------------------|
| awsAccessKeyId  | Your AWS Access Key credential              | No. If not specified, the task will defer to the default credential chain. |
| awsSecretKey    | Your AWS Secret Key credential              | No. If not specified, the task will defer to the default credential chain. |                                 
| stackName       | The name of the stack to wait for.          | Yes.                                                                       |
| status          | The status to wait for the stack to reach.  | Yes.                                                                       |

Nested elements:
None.

Example code: 
```
<let-cloudformation-stack-reach-status stackName="anttaskteststack" status="UPDATE_COMPLETE" />
```

Waits for "anttaskteststack" to reach "UDPATE_COMPLETE". The build is blocked until it completes. 