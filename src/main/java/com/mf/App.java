package com.mf;

import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static String INSTANCE_ID = "9d8ee555-3660-469e-91e5-d7c7e9443966";
    public static String OCTANE_SERVER_URL = "https://demo.almoctane.com";
    public static String SPACE_ID = "1001";
    public static String CLIENT_ID = "sa@nga";
    public static String CLIENT_PASSWORD = "Welcome1";

    private static Logger logger;

    public static void main(String[] args) {
        System.setProperty("octaneAllowedStorage", CiServices.getAllowedStorageFile().getAbsolutePath() + File.separator);
        logger = LogManager.getLogger(App.class);
        logger.info("*****************************************************************");
        logger.info("*********************Starting CI SDK Demo************************");
        logger.info("*****************************************************************");

        OctaneConfiguration octaneConfiguration = new OctaneConfiguration(INSTANCE_ID, OCTANE_SERVER_URL, SPACE_ID);
        octaneConfiguration.setClient(CLIENT_ID);
        octaneConfiguration.setSecret(CLIENT_PASSWORD);

        OctaneSDK.addClient(octaneConfiguration, CiServices.class);

        try {
            System.out.println("Ci SKD Demo  is started.");
            System.out.println("Logs are available in : " + CiServices.getAllowedStorageFile().getAbsolutePath() + "\\nga\\logs");

            System.out.println("Please (1)add pipeline in ALM Octane, (2) run the pipeline from ALM Octane ");
            System.in.read();
        } catch (IOException e) {
            System.out.println("Exception occurred" + e.getMessage());

        }
        System.out.println("Ci SKD Demo  is stopped.");
    }
}
