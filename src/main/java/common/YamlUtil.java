package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class YamlUtil {
    private YamlUtil() {}

    //Yamlファイル内の最初の段落の項目をすべて取得する
    public static List<String> getFirstPathList(File yamlFile){
        List<String> list = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(yamlFile));
            String line = br.readLine();
            while (line != null) {
                if(!line.isEmpty() && (line.charAt(0) != ' ' && line.charAt(0) != '\t' && line.charAt(0) != '#')){
                    list.add(line.substring(0,line.indexOf(":")).replace(":",""));
                }
                line = br.readLine();
            }
            br.close();
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
