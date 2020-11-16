package com.mf;

import com.hp.octane.integrations.CIPluginServices;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.PhaseType;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.integrations.dto.tests.BuildContext;
import com.hp.octane.integrations.dto.tests.TestRun;
import com.hp.octane.integrations.dto.tests.TestRunResult;
import com.hp.octane.integrations.dto.tests.TestsResult;
import com.hp.octane.integrations.utils.CIPluginSDKUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class CiServices extends CIPluginServices {

    private static final DTOFactory dtoFactory = DTOFactory.getInstance();
    private static final String ciServerVersion = "1.0";
    private static final String ciServerPluginVersion = "1.0";
    private static final String ciServerType = "MyCustomCiServerType";//CIServerTypes.JENKINS.value();//;
    private static final String ciServerUrl = "http://demo.ci-server:8080";//replace by real link to ci server

    private static final boolean hasProxySettings = false;
    private static final String proxyServer = "";//web-proxy.il.softwaregrp.net
    private static final Integer proxyPort = 8080;
    private static final String proxyUser = "";
    private static final String proxyPassword = "";
    private static final String noProxyHost = "localhost";

    private static final Map<String, String> ciJobsKey2Name = new HashMap();

    public CiServices() {

        //Job that exist in CI Server
        ciJobsKey2Name.put("jobA", "Job A simple");
        ciJobsKey2Name.put("jobB", "Job B complex");
        ciJobsKey2Name.put("jobC", "Job C important");
    }

    @Override
    public CIProxyConfiguration getProxyConfiguration(URL targetUrl) {
        if (hasProxySettings && !CIPluginSDKUtils.isNonProxyHost(targetUrl.getHost(), noProxyHost)) {
            return dtoFactory.newDTO(CIProxyConfiguration.class)
                    .setHost(proxyServer)
                    .setPort(proxyPort)
                    .setUsername(proxyUser)
                    .setPassword(proxyPassword);
        } else {
            return null;
        }
    }

    /***
     * Info about ci server
     * @return
     */

    @Override
    public CIServerInfo getServerInfo() {
        CIServerInfo result = dtoFactory.newDTO(CIServerInfo.class);
        result.setType(ciServerType)
                .setVersion(ciServerVersion)
                .setUrl(ciServerUrl) //External link to CI Server, so it would be possible to reach ci server from browser
                .setSendingTime(System.currentTimeMillis());
        return result;
    }

    /***
     * Info about plugin
     * @return
     */
    @Override
    public CIPluginInfo getPluginInfo() {
        CIPluginInfo result = dtoFactory.newDTO(CIPluginInfo.class);
        result.setVersion(ciServerPluginVersion);
        return result;
    }

    /***
     * Path for saving log and additional auxiliary files. It should be the path that application should have permissions to write to.
     * @return
     */
    @Override
    public File getAllowedOctaneStorage() {
        return getAllowedStorageFile();
    }

    public static File getAllowedStorageFile() {
        File f = new File("ciServerRepository");
        f.mkdirs();
        return f;
    }

    /***
     * Get list of job existing in CI Server. This list is used by Add new pipeline dialog in ALM Octane
     * @param includeParameters
     * @param workspaceId
     * @return
     */
    @Override
    public CIJobsList getJobsList(boolean includeParameters, Long workspaceId) {

        System.out.println("Get pipeline list ");
        List<PipelineNode> list = new ArrayList<>();
        for (Map.Entry<String, String> e : ciJobsKey2Name.entrySet()) {
            list.add(dtoFactory.newDTO(PipelineNode.class)
                    .setJobCiId(e.getKey())//job key
                    .setName(e.getValue()));//display name
        }
        CIJobsList result = dtoFactory.newDTO(CIJobsList.class);
        return result.setJobs(list.toArray(new PipelineNode[list.size()]));
    }

    /***
     * Get job by its jobId.
     * @param rootJobId
     * @return
     */
    @Override
    public PipelineNode getPipeline(String rootJobId) {
        System.out.println("Get pipeline node for " + rootJobId);
        return dtoFactory.newDTO(PipelineNode.class)
                .setJobCiId(rootJobId)
                .setName(ciJobsKey2Name.get(rootJobId));
    }

    /***
     * This method is called from ALM Octane
     * @param jobId
     * @param originalBody
     */
    @Override
    public void runPipeline(String jobId, String originalBody) {
        String buildId = System.currentTimeMillis() + "";
        System.out.println("Running job " + jobId + ", build id is " + buildId);


        //Start event should be send on job started event that received from CI server and not in runPipeline,
        // as job might be started in delay or even stuck because of missing free agent
        sentStartEvent(jobId, buildId);

        //simulation of execution
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Finish event should be send on event that received from CI
        sentFinishEvent(jobId, buildId, true/*flag indicating whether job has test results*/);

        //indicate to SDK that there are tests in job+build. Later SDK will call getTestsResult method to get stream to test result file
        OctaneSDK.getClients().forEach(client ->
                client.getTestsService().enqueuePushTestsResult(jobId, buildId, null));

    }

    /***
     * Stream to test results in Octane format
     * See more details about format : https://admhelp.microfocus.com/octane/en/15.0.40/Online/Content/API/test-results.htm
     *
     * @param jobId
     * @param buildId
     * @return
     */
    @Override
    public InputStream getTestsResult(String jobId, String buildId) {
        System.out.println("Sending test results for  " + jobId + "#" + buildId);

        TestRun tr1 = dtoFactory.newDTO(TestRun.class).setTestName("testAppA").setPackageName("MF.simple.tests").setClassName("AppTest").setDuration(5).setResult(TestRunResult.PASSED);
        TestRun tr2 = dtoFactory.newDTO(TestRun.class).setTestName("testAppB").setPackageName("MF.simple.tests").setClassName("AppTest").setDuration(6).setResult(TestRunResult.FAILED);
        TestRun tr3 = dtoFactory.newDTO(TestRun.class).setTestName("testAppC").setPackageName("MF.simple.tests").setClassName("AppTest").setDuration(7).setResult(TestRunResult.PASSED);
        BuildContext buildContext = dtoFactory.newDTO(BuildContext.class).setJobId(jobId).setBuildId(buildId).setServerId("to-be-filled-in-SDK");
        TestsResult results = dtoFactory.newDTO(TestsResult.class).setTestRuns(Arrays.asList(tr1, tr2, tr3)).setBuildContext(buildContext);
        //InputStream result = new FileInputStream(new File("results", "mqmTests.xml"));
        String xml = dtoFactory.dtoToXml(results);
        InputStream result = dtoFactory.dtoToXmlStream(results);
        return result;

    }

    private void sentStartEvent(String jobId, String buildId) {
        System.out.println("Sending start event for  " + jobId + "#" + buildId);
        CIEvent event = dtoFactory.newDTO(CIEvent.class)
                .setEventType(CIEventType.STARTED)
                .setProject(jobId)//job key id
                .setProjectDisplayName(ciJobsKey2Name.get(jobId)) //job display name
                .setBuildCiId(buildId)//build id
                .setNumber(buildId)//build display name
                .setStartTime(System.currentTimeMillis())
                .setEstimatedDuration(10l);

        event.setPhaseType(PhaseType.INTERNAL);
        publishEvent(event);
    }


    private void sentFinishEvent(String jobId, String buildId, boolean hasTests) {
        System.out.println("Sending finish event for  " + jobId + "#" + buildId);
        CIEvent event = dtoFactory.newDTO(CIEvent.class)
                .setEventType(CIEventType.FINISHED)
                .setProject(jobId)
                .setProjectDisplayName(ciJobsKey2Name.get(jobId))
                .setBuildCiId(buildId)
                .setNumber(buildId)
                .setDuration(10l)
                .setResult(CIBuildResult.SUCCESS)
                .setTestResultExpected(hasTests);

        publishEvent(event);
    }

    private void publishEvent(CIEvent event) {
        OctaneSDK.getClients().forEach(octaneClient -> {
            octaneClient.getEventsService().publishEvent(event);
        });
    }
}
