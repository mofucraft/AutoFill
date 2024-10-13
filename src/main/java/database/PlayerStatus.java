package database;

import java.util.UUID;

public class PlayerStatus {
    private UUID uuid;
    private String name;
    private int length;
    private String usingLanguage;

    public PlayerStatus(UUID uuid, String name, int length, String usingLanguage){
        this.uuid = uuid;
        this.name = name;
        this.length = length;
        this.usingLanguage = usingLanguage;
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

    public int getLength() { return length; }

    public void setLength(int length) { this.length = length; }

    public String getUsingLanguage() {
        return usingLanguage;
    }

    public void setUsingLanguage(String usingLanguage) {
        this.usingLanguage = usingLanguage;
    }
}
