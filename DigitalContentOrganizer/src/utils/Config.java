package utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class Config {
    private static boolean isLoaded = false;
    private static Properties properties = new Properties();

    public static String connectionString; 

    public static void load() throws IOException {
        if(!isLoaded) forceLoad();
    }
    
    public static void forceLoad() throws IOException {
        FileReader reader = new FileReader("connection_settings.txt");
        properties.load(reader);
        reader.close();

        connectionString = properties.getProperty("connectionString");
        isLoaded = true;
    }
}
