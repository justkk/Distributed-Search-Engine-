package edu.upenn.cis455.mapreduce;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Utils {

    public static void addMap(Map<String, String> parentMap, Map<String, String> childMap) {

        childMap.entrySet().forEach(stringStringEntry -> {
            parentMap.put(stringStringEntry.getKey(), stringStringEntry.getValue());
        });
    }

    public static void createDirectory(String folderPath) {

        File dir = new File(folderPath);

        if (!dir.exists()) {
            try{
                dir.mkdirs();
            }
            catch(SecurityException se){
                //throw new RuntimeException(" Directory Creation Failed");
            }
        }
    }

    public static void checkDirectory(String folderPath) {

        File dir = new File(folderPath);
        if (!dir.exists()) {
            throw new RuntimeException(" Directory Creation Failed");
        }
    }

    public static void deleteDirectory(String folderPath) {
        File dir = new File(folderPath);
        try {
            if (dir.exists()) {
                delete(dir);
            }
        } catch (Exception e) {
            //
        }
    }

    public static void delete(File file)
            throws IOException {

        if(file.isDirectory()){
            if(file.list().length==0){
                file.delete();
            }else{
                String files[] = file.list();
                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if(file.list().length==0){
                    file.delete();
                }
            }
        }else{
            file.delete();
        }
    }


}
