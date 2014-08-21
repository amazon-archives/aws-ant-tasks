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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.ant.AWSTestUtils;
import com.amazonaws.ant.s3.UploadFileSetToS3Task;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;

public class UploadFileSetToS3TaskTests {

    private static final String RES_FILE_3 = "res3";
    private static final String RES_FILE_2 = "res2";
    private static final String RES_FILE_1 = "res";
    private static final String TESTFILE_SUFFIX = ".txt";
    private static final String TEST_FILE_3 = "test3";
    private static final String TEST_FILE_2 = "test2";
    private static final String TEST_FILE_1 = "test";
    private static final String BUCKET_NAME = "deployfilesettos3testbucket";
    private static final String KEY_PREFIX = "deployfilesettos3test/";
    private static AmazonS3Client client;
    private static String fileName1, fileName2, fileName3;
    private static File testFile1, testFile2, testFile3;
    private File resFile1, resFile2, resFile3;

    @BeforeClass
    public static void setUp() throws IOException {
        testFile1 = AWSTestUtils.createRandomFile(TEST_FILE_1, TESTFILE_SUFFIX);
        testFile2 = AWSTestUtils.createRandomFile(TEST_FILE_2, TESTFILE_SUFFIX);
        testFile3 = AWSTestUtils.createRandomFile(TEST_FILE_3, TESTFILE_SUFFIX);
        fileName1 = testFile1.getName();
        fileName2 = testFile2.getName();
        fileName3 = testFile3.getName();

        client = new AmazonS3Client();
        client.createBucket(BUCKET_NAME);
    }

    @Test
    public void testExecuteSingleFile() throws FileNotFoundException,
            IOException {
        UploadFileSetToS3Task task = new UploadFileSetToS3Task();
        task.setProject(new Project());
        FileSet fileset = new FileSet();
        fileset.setDir(testFile1.getParentFile());
        fileset.setFile(testFile1);
        task.addFileset(fileset);
        task.setBucketName(BUCKET_NAME);
        task.setKeyPrefix(KEY_PREFIX);
        task.execute();
        resFile1 = File.createTempFile(RES_FILE_1, TESTFILE_SUFFIX);
        client.getObject(new GetObjectRequest(BUCKET_NAME, KEY_PREFIX
                + fileName1), resFile1);
        assertTrue(FileUtils.contentEquals(testFile1, resFile1));
    }

    @Test
    public void testExecuteMultipleFiles() throws IOException {
        UploadFileSetToS3Task task = new UploadFileSetToS3Task();
        task.setProject(new Project());
        FileSet fileset = new FileSet();
        fileset.setDir(testFile1.getParentFile());
        fileset.setIncludes("*.txt");
        task.addFileset(fileset);
        task.setBucketName(BUCKET_NAME);
        task.setKeyPrefix(KEY_PREFIX);
        task.execute();
        resFile1 = File.createTempFile(RES_FILE_1, TESTFILE_SUFFIX);
        resFile2 = File.createTempFile(RES_FILE_2, TESTFILE_SUFFIX);
        resFile3 = File.createTempFile(RES_FILE_3, TESTFILE_SUFFIX);
        client.getObject(new GetObjectRequest(BUCKET_NAME, KEY_PREFIX
                + fileName1), resFile1);
        client.getObject(new GetObjectRequest(BUCKET_NAME, KEY_PREFIX
                + fileName2), resFile2);
        client.getObject(new GetObjectRequest(BUCKET_NAME, KEY_PREFIX
                + fileName3), resFile3);
        assertTrue(FileUtils.contentEquals(testFile1, resFile1));
        assertTrue(FileUtils.contentEquals(testFile1, resFile1));
        assertTrue(FileUtils.contentEquals(testFile1, resFile1));
    }

    @After
    public void tearDown() {

        if (resFile1 != null) {
            resFile1.delete();
        }
        if (resFile2 != null) {
            resFile2.delete();
        }
        if (resFile3 != null) {
            resFile3.delete();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() {
        AWSTestUtils.emptyAndDeleteBucket(client, BUCKET_NAME);
        testFile1.delete();
        testFile2.delete();
        testFile3.delete();
    }
}
