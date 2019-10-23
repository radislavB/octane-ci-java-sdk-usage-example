package com.mf;

import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;

import java.io.Console;
import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static String INSTANCE_ID = "9d8ee555-3660-469e-91e5-d7c7e9443966";
    public static String SERVER_URL = "http://localhost:8080";
    public static String SPACE_ID = "1001";
    public static String CLIENT_ID = "sa@nga";
    public static String CLIENT_PASSWORD = "Welcome1";

    public static void main(String[] args) {
        System.setProperty("octaneAllowedStorage", CiServices.getAllowedStorageFile().getAbsolutePath() + File.separator);

        OctaneConfiguration octaneConfiguration = new OctaneConfiguration(INSTANCE_ID, SERVER_URL, SPACE_ID);
        octaneConfiguration.setClient(CLIENT_ID);
        octaneConfiguration.setSecret(CLIENT_PASSWORD);

        OctaneSDK.addClient(octaneConfiguration, CiServices.class);

        /*Console cns = System.console();
        while (true) {
            if (cns != null) {
                System.out.println("Your input : ");

                String line = cns.readLine();

            }
        }*/
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
