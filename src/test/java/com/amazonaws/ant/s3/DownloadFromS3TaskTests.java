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
package com.amazonaws.ant.s3;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.ant.AWSTestUtils;
import com.amazonaws.ant.s3.DownloadFileFromS3Task;
import com.amazonaws.services.s3.AmazonS3Client;

public class DownloadFromS3TaskTests {
    private static final String RES_FILE = "res";
    private static final String TEST_FILE_3 = "test3";
    private static final String TEST_FILE_2 = "test2";
    private static final String TEST_FILE_1 = "test";
    private static final String BUCKET_NAME = "deployfilesettos3testbucket";
    private static final String KEY_PREFIX = "deployfilesettos3test/";
    private static final String TESTFILE_SUFFIX = ".txt";
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String DIR = USER_DIR + File.pathSeparator
            + KEY_PREFIX;
    private static File testFile1, testFile2, testFile3;
    private static AmazonS3Client client;
    private File resFile1, resFile2, resFile3;

    @BeforeClass
    public static void setUp() throws IOException {
        testFile1 = AWSTestUtils.createRandomFile(TEST_FILE_1, TESTFILE_SUFFIX);
        testFile2 = AWSTestUtils.createRandomFile(TEST_FILE_2, TESTFILE_SUFFIX);
        testFile3 = AWSTestUtils.createRandomFile(TEST_FILE_3, TESTFILE_SUFFIX);
        client = new AmazonS3Client();
        client.createBucket(BUCKET_NAME);
        client.putObject(BUCKET_NAME, KEY_PREFIX + testFile1.getName(),
                testFile1);
        client.putObject(BUCKET_NAME, KEY_PREFIX + testFile2.getName(),
                testFile2);
        client.putObject(BUCKET_NAME, KEY_PREFIX + testFile3.getName(),
                testFile3);
    }

    @Test
    public void testDownloadSingleFile() throws IOException {
        DownloadFileFromS3Task task = new DownloadFileFromS3Task();
        task.setProject(new Project());
        task.setBucketName(BUCKET_NAME);
        task.setKey(KEY_PREFIX + testFile1.getName());
        resFile1 = File.createTempFile(RES_FILE, TESTFILE_SUFFIX);
        resFile1.createNewFile();
        task.setFile(resFile1);
        task.execute();
        assertTrue(FileUtils.contentEquals(testFile1, resFile1));
    }

    @Test
    public void testDownloadFilesWithPrefix() throws IOException {
        DownloadFileFromS3Task task = new DownloadFileFromS3Task();
        task.setProject(new Project());
        task.setBucketName(BUCKET_NAME);
        task.setKeyPrefix(KEY_PREFIX);
        task.setDir(USER_DIR);
        resFile1 = new File(DIR + testFile1.getName());
        resFile2 = new File(DIR + testFile2.getName());
        resFile3 = new File(DIR + testFile3.getName());
        task.execute();
        assertTrue(FileUtils.contentEquals(testFile1, resFile1));
        assertTrue(FileUtils.contentEquals(testFile2, resFile2));
        assertTrue(FileUtils.contentEquals(testFile3, resFile3));
    }

    @After
    public void tearDown() throws IOException {
        if (resFile1 != null) {
            resFile1.delete();
        }
        if (resFile2 != null) {
            resFile2.delete();
        }
        if (resFile3 != null) {
            resFile3.delete();
        }
        FileUtils.deleteDirectory(new File(DIR));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        AWSTestUtils.emptyAndDeleteBucket(client, BUCKET_NAME);
        testFile1.delete();
        testFile2.delete();
        testFile3.delete();
    }
}
