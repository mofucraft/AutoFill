package language;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.YamlUtil.getFirstPathList;

public class LanguageUtil {
    private static Map<String, Map<String, String>> languageMap;
    private static final String UNDEFINED_WORD = "undefined";

    private LanguageUtil() {}

    public static List<String> getLanguageList(){
        return new ArrayList<>(languageMap.keySet());
    }

    public static String getWord(String language,String path){
        String word = languageMap.get(language).get(path);
        return word!=null?word:UNDEFINED_WORD;
    }

    public static String getReplacedWord(String language, String path, Map<String, String> variables){
        String word = languageMap.get(language).get(path);
        return getReplacedWord(word!=null?word:UNDEFINED_WORD,variables);
    }

    public static String getReplacedWord(String word, Map<String, String> variables){
        if(variables == null) return word;
        for(String variable : variables.keySet()){
            word = word.replaceAll("%" + variable + "%",variables.get(variable));
        }
        return word.replaceAll("%%","%");
    }

    public static void refreshLanguage(Plugin plugin){
        if(languageMap != null) languageMap.clear();
        languageMap = new HashMap<>();
        File languageFolder = new File(plugin.getDataFolder(), File.separator + "language");
        if(languageFolder == null) return;
        for(File languageFile : languageFolder.listFiles()){
            if(languageFile.isFile() && languageFile.getAbsolutePath().contains(".lang.yml")){
                if(languageFile.toPath().getFileName().toString().replace(".lang.yml","").length() == 2){
                    FileConfiguration languageYaml = YamlConfiguration.loadConfiguration(languageFile);
                    Map<String, String> _LanguageMap = new HashMap<>();
                    for(String path: getFirstPathList(languageFile)){
                        System.out.println(path + ":" + languageYaml.getObject(path,String.class));
                        if(languageYaml.getObject(path, String.class) != null) _LanguageMap.put(path, languageYaml.getObject(path,String.class));
                    }
                    languageMap.put(languageFile.toPath().getFileName().toString().replace(".lang.yml",""),_LanguageMap);
                }
            }
        }
    }
}
