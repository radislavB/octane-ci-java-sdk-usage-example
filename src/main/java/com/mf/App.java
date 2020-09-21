package com.mf;

import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    //Plugin configuration settings , should be saved
    public static String INSTANCE_ID = "9d8ee555-3660-469e-91e5-d7c7e9443966";
    public static String OCTANE_SERVER_URL = "http://localhost:8080/octane";
    public static String SPACE_ID = "1001";
    public static String CLIENT_ID = "sa@nga";
    public static String CLIENT_PASSWORD = "Welcome1";//should be save encrypted

    private static Logger logger;

    public static void main(String[] args) {
        //this property should be defined first (before LogManager.getLogger)  as this path is used for saving log files
        System.setProperty("octaneAllowedStorage", CiServices.getAllowedStorageFile().getAbsolutePath() + File.separator);

        logger = LogManager.getLogger(App.class);
        logger.info("*****************************************************************");
        logger.info("*********************Starting CI SDK Demo************************");
        logger.info("*****************************************************************");

        try {
            startSdk(loadConfigurations());

            System.out.println("Ci SKD Demo  is started.");
            System.out.println("Please do the following in the ALM Octane :");
            System.out.println("(1) add new CI server in ALM Octane");
            System.out.println("(2) add pipeline in ALM Octane");
            System.out.println("(3) run the pipeline from ALM Octane");

            System.out.println("If your CI Server is not visible in ALM Octane, check in log file for errors.");
            System.out.println("Logs are available in : " + CiServices.getAllowedStorageFile().getAbsolutePath() + "\\nga\\logs");

            System.in.read();
        } catch (IOException e) {
            System.out.println("Exception occurred" + e.getMessage());

        }
        System.out.println("Ci SKD Demo  is stopped.");
    }

    /***
     * Should be read from file/db
     * @return
     */
    private static List<PluginSettings> loadConfigurations(){
        PluginSettings ps = new PluginSettings(INSTANCE_ID,OCTANE_SERVER_URL,SPACE_ID,CLIENT_ID,CLIENT_PASSWORD);
        return Arrays.asList(ps);
    }

    private static void startSdk(List<PluginSettings> settings) {
        settings.forEach(s->{
            OctaneConfiguration octaneConfiguration = OctaneConfiguration.create(s.getInstanceId(), s.getOctaneServerUrl(), s.getOctaneSpace());
            octaneConfiguration.setClient(s.getClientId());
            octaneConfiguration.setSecret(s.getClientPassword());
            OctaneSDK.addClient(octaneConfiguration, CiServices.class);
        });
    }

    private static class PluginSettings {
        private String instanceId;
        private String octaneServerUrl;
        private String octaneSpace;
        private String clientId;
        private String clientPassword;

        public PluginSettings(String instanceId, String octaneServerUrl, String octaneSpace, String clientId, String clientPassword) {
            this.instanceId = instanceId;
            this.octaneServerUrl = octaneServerUrl;
            this.octaneSpace = octaneSpace;
            this.clientId = clientId;
            this.clientPassword = clientPassword;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public String getOctaneServerUrl() {
            return octaneServerUrl;
        }

        public String getOctaneSpace() {
            return octaneSpace;
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientPassword() {
            return clientPassword;
        }

    }
}
