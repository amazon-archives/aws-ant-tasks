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
package com.amazonaws.ant;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;

public class AssertFileEqualsTask extends AWSAntTask {

    private String firstFile;

    public void setFirstFile(String firstFile) {
        this.firstFile = firstFile;
    }

    private String secondFile;

    public void setSecondFile(String secondFile) {
        this.secondFile = secondFile;
    }

    private void checkParams() {
        if (firstFile == null) {
            throw new BuildException("First file must be specified");
        }
        if (secondFile == null) {
            throw new BuildException("Second file must be specified");
        }

    }

    public void execute() {
        checkParams();
        File file1 = new File(firstFile);
        if (!file1.exists()) {
            throw new BuildException("The file " + firstFile + "does not exist");
        }
        File file2 = new File(secondFile);
        if (!file2.exists()) {
            throw new BuildException("The file " + secondFile
                    + "does not exist");
        }

        try {
            if (!FileUtils.contentEquals(file1, file2)) {
                throw new BuildException(
                        "The two input files are not equal in content");
            }
        } catch (IOException e) {
            throw new BuildException(
                    "IOException while trying to compare content of files: "
                            + e.getMessage());
        }
    }
}
