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
import com.hp.octane.integrations.dto.general.CIServerTypes;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.integrations.utils.CIPluginSDKUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CiServices extends CIPluginServices {

    private static final DTOFactory dtoFactory = DTOFactory.getInstance();
    private static final String ciServerVersion = "1.0";
    private static final String ciServerPluginVersion = "1.0";
    private static final String ciServerType = "MyCustomCiServerType";//CIServerTypes.JENKINS.value();//;
    private static final String ciServerUrl = "http://demo.ci-server:8080";//replace by real link to ci server

    private static final boolean hasProxySettings = true;
    private static final String proxyServer = "web-proxy.il.hpecorp.net";
    private static final Integer proxyPort = 8080;
    private static final String proxyUser = "";
    private static final String proxyPassword = "";
    private static final String noProxyHost = "localhost";

    private static final Map<String, String> ciJobsKey2Name = new HashMap();

    public CiServices() {
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

    @Override
    public CIServerInfo getServerInfo() {
        CIServerInfo result = dtoFactory.newDTO(CIServerInfo.class);
        result.setType(ciServerType)
                .setVersion(ciServerVersion)
                .setUrl(ciServerUrl)
                .setSendingTime(System.currentTimeMillis());
        return result;
    }

    @Override
    public CIPluginInfo getPluginInfo() {
        CIPluginInfo result = dtoFactory.newDTO(CIPluginInfo.class);
        result.setVersion(ciServerPluginVersion);
        return result;
    }

    @Override
    public File getAllowedOctaneStorage() {
        return getAllowedStorageFile();
    }

    public static File getAllowedStorageFile() {
        File f = new File("ciServerRepository");
        f.mkdirs();
        return f;
    }

    @Override
    public CIJobsList getJobsList(boolean includeParameters) {

        System.out.println("Get pipeline list ");
        List<PipelineNode> list = new ArrayList<>();
        for (Map.Entry<String, String> e : ciJobsKey2Name.entrySet()) {
            list.add(dtoFactory.newDTO(PipelineNode.class)
                    .setJobCiId(e.getKey())
                    .setName(e.getValue()));
        }
        CIJobsList result = dtoFactory.newDTO(CIJobsList.class);
        return result.setJobs(list.toArray(new PipelineNode[list.size()]));
    }

    @Override
    public PipelineNode getPipeline(String rootJobId) {
        System.out.println("Get pipeline node for " + rootJobId);
        return dtoFactory.newDTO(PipelineNode.class)
                .setJobCiId(rootJobId)
                .setName(ciJobsKey2Name.get(rootJobId));
    }

    @Override
    public void runPipeline(String jobId, String originalBody) {
        String buildId = System.currentTimeMillis() + "";
        System.out.println("Running job " + jobId + ", build id is " + buildId);
        sentStartEvent(jobId, buildId);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sentFinishEvent(jobId, buildId, true);

        //indicate to sdk that there are tests
        OctaneSDK.getClients().forEach(client ->
                client.getTestsService().enqueuePushTestsResult(jobId, buildId, null));

    }

    @Override
    public InputStream getTestsResult(String jobId, String buildId) {
        System.out.println("Sending test results for  " + jobId + "#" + buildId);
        try {
            InputStream result = new FileInputStream(new File("results", "mqmTests.xml"));
            return result;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void sentStartEvent(String jobId, String buildId) {
        System.out.println("Sending start event for  " + jobId + "#" + buildId);
        CIEvent event = dtoFactory.newDTO(CIEvent.class)
                .setEventType(CIEventType.STARTED)
                .setProject(jobId)
                .setProjectDisplayName(ciJobsKey2Name.get(jobId))
                .setBuildCiId(buildId)
                .setNumber(buildId)
                .setStartTime(System.currentTimeMillis())
                .setEstimatedDuration(10l);

        event.setPhaseType(PhaseType.POST);
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
