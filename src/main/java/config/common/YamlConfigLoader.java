package config.common;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class YamlConfigLoader {
    private File configFile;
    private YamlConfiguration config;

    public YamlConfigLoader(Plugin plugin){
        File configFolder = plugin.getDataFolder();
        this.configFile = new File(configFolder, File.separator + "config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public YamlConfigLoader(File filePath){
        this.configFile = filePath;
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public <T> T getValue(String name, Class<T> clazz, T def) throws IOException {
        if(config.getObject(name, clazz) == null){
            setValue(name, def);
        }
        return config.getObject(name, clazz, def);
    }

    public List<?> getListValue(String name, List<?> def) throws IOException {
        if(config.getList(name, def) == def){
            setListValue(name, def);
        }
        return config.getList(name, def);
    }

    public <T> void setValue(String name, T value) throws IOException {
        config.set(name, value);
        config.save(configFile);
    }

    public void setListValue(String name, List<?> value) throws IOException {
        config.set(name, value);
        config.save(configFile);
    }

    public void setComments(String name, ArrayList<String> comments) throws IOException {
        config.setComments(name, comments);
        config.save(configFile);
    }

    public void setInlineComments(String name, ArrayList<String> comments) throws IOException {
        config.setInlineComments(name, comments);
        config.save(configFile);
    }
}
