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

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.amazonaws.ant.AWSTestUtils;

public class SetUpS3TestsTask extends Task {

    private File file;

    public void setFile(File file) {
        this.file = file;
    }

    private void checkParams() {
        if (file == null) {
            throw new BuildException("Missing parameter: File is required");
        }
    }

    public void execute() {
        checkParams();
        try {
            file.createNewFile();
            AWSTestUtils.writeRandomLinesToFile(file);
        } catch (Exception e) {
            throw new BuildException("Error setting up s3 tests.", e);
        }
    }
}
