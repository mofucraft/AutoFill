package database;

import java.util.UUID;

public class PlayerStatus {
    private UUID uuid;
    private String name;
    private String usingLanguage;
    private int maxThread;

    public PlayerStatus(UUID uuid, String name, String usingLanguage, int maxThread){
        this.uuid = uuid;
        this.name = name;
        this.usingLanguage = usingLanguage;
        this.maxThread = maxThread;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsingLanguage() {
        return usingLanguage;
    }

    public void setUsingLanguage(String usingLanguage) {
        this.usingLanguage = usingLanguage;
    }

    public int getMaxThread() {
        return maxThread;
    }

    public void setMaxThread(int maxThread) {
        this.maxThread = maxThread;
    }
}
