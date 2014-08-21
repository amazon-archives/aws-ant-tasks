AWS Java Ant Tasks
==================

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
<deploy-beanstalk-app bucketName="mybucket" file="path/to/myapp.war" versionLabel="Version1" versionDescription="myversion" applicationName="mybeanstalkapp" environmentName="mybeanstalkenv />
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