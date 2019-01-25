/**
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2016, 2018
 */

package org.zowe.jobs.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.apache.http.HttpStatus;
import org.hamcrest.text.MatchesPattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zowe.jobs.exceptions.JobFileIdNotFoundException;
import org.zowe.jobs.exceptions.JobIdNotFoundException;
import org.zowe.jobs.exceptions.JobNameNotFoundException;
import org.zowe.jobs.model.Job;
import org.zowe.jobs.model.JobFile;
import org.zowe.jobs.model.JobStatus;

import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

//TODO - rewrite using rest assured
public class JobFilesIntegrationTest extends AbstractJobsIntegrationTest {

    private static Job job;

    @BeforeClass
    public static void submitJob() throws Exception {
        job = submitJobAndPoll(JOB_IEFBR14, JobStatus.OUTPUT);
    }

    @AfterClass
    public static void purgeJob() throws Exception {
        deleteJob(job);
    }

    @Test
    public void testGetJobOutputFiles() throws Exception {
        String jobName = job.getJobName();
        String jobId = job.getJobId();

        JobFile jesmsglg = JobFile.builder().ddname("JESMSGLG").recfm("UA").lrecl(133).id(2).byteCount(1102)
            .recordCount(20).build();
        JobFile jesjcl = JobFile.builder().ddname("JESJCL").recfm("V").lrecl(136).id(3).byteCount(182).recordCount(3)
            .build();
        JobFile jessysmsg = JobFile.builder().ddname("JESYSMSG").recfm("VA").lrecl(137).id(4).byteCount(819)
            .recordCount(13).build();

        List<JobFile> actual = getJobFiles(jobName, jobId).then().statusCode(HttpStatus.SC_OK).extract().body()
            .jsonPath().getList("", JobFile.class);

        assertThat(actual, hasItems(jesmsglg, jesjcl, jessysmsg));
    }

    @Test
    public void testGetJobOutputFilesInvalidJobId() throws Exception {
        String jobName = job.getJobName();
        String jobId = "z0000000";
        verifyExceptionReturn(new JobIdNotFoundException(jobName, jobId), getJobFiles(jobName, jobId));
    }

    @Test
    public void testGetJobOutputFilesInvalidJobNameAndId() throws Exception {
        String jobName = "z";
        String jobId = "z0000000";
        verifyExceptionReturn(new JobIdNotFoundException(jobName, jobId), getJobFiles(jobName, jobId));
    }

    public static Response getJobFiles(String jobName, String jobId) throws Exception {
        return RestAssured.given().when().get(getJobPath(jobName, jobId) + "/files");
    }

    @Test
    public void testGetJobOutputFileContents() throws Exception {
        String jobName = job.getJobName();
        String jobId = job.getJobId();
        String expectedContentRegex = ".*J E S 2  J O B  L O G.*------ JES2 JOB STATISTICS ------.*3 CARDS READ.*"
                + "-           40 SYSOUT PRINT RECORDS.*-            0 SYSOUT PUNCH RECORDS.*"
                + "-            5 SYSOUT SPOOL KBYTES.*-         0.00 MINUTES EXECUTION TIME.*";
        Pattern regex = Pattern.compile(expectedContentRegex, Pattern.DOTALL);
        getJobFileContent(jobName, jobId, "2").then().statusCode(HttpStatus.SC_OK).body("content",
                MatchesPattern.matchesPattern(regex));
    }

    @Test
    public void testGetJobOutputFileContentsInvalidJobId() throws Exception {
        String jobName = job.getJobName();
        String jobId = "z0000000";
        verifyExceptionReturn(new JobNameNotFoundException(jobName, jobId), getJobFileContent(jobName, jobId, "2"));
    }

    @Test
    public void testGetJobOutputFileContentsInvalidJobName() throws Exception {
        String jobName = "z";
        String jobId = "z0000000";
        verifyExceptionReturn(new JobIdNotFoundException(jobName, jobId), getJobFileContent(jobName, jobId, "2"));
    }

    @Test
    public void testGetJobOutputFileContentsInvalidJobFileId() throws Exception {
        String jobName = job.getJobName();
        String jobId = job.getJobId();
        String fileId = "999";
        verifyExceptionReturn(new JobFileIdNotFoundException(jobName, jobId, fileId),
                getJobFileContent(jobName, jobId, fileId));
    }

    public static Response getJobFileContent(String jobName, String jobId, String fileId) throws Exception {
        return RestAssured.given().when().get(getJobPath(jobName, jobId) + "/files/" + fileId + "/content");
    }
}