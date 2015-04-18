package nl.revolution.sdr.services.config;

public class ConfigService {

    private ConfigService() {

    }

    private static ConfigService instance;

    public static ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }

    public String getDatabaseHost() {
        return "localhost";
    }

    public int getHttpPort() {
        return 1090;
    }

    public String getDump1090Url() {
        return "http://192.168.2.150:8080/data.json";
    }

}
