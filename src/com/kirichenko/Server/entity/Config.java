package com.kirichenko.Server.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

    public static int PORT;
    public static String DRIVER;
    public static String URL;
    public static String USER;
    public static String PASSWORD;
    public static String HOST;

    private static final String PropertiesFile = "Server-client.properties";
    private static String Separator = System.getProperty("file.separator");
    private static Properties properties = new Properties();

    static {
        File currentDir = new File (".");
        try {
            String FilePath = currentDir.getCanonicalPath() + Separator + PropertiesFile;
            properties.load(new FileInputStream(FilePath));
            PORT = Integer.parseInt(properties.getProperty("port"));
            DRIVER = properties.getProperty("driver");
            URL = properties.getProperty("url");
            USER = properties.getProperty("user");
            PASSWORD = properties.getProperty("password");
            HOST = properties.getProperty("host");
        } catch (FileNotFoundException e) {
            System.out.println("File Server-client.properties not found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
