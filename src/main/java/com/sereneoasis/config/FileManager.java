package com.sereneoasis.config;

import java.io.File;

public class FileManager {

    private final File mainDir;


    public FileManager(){
        mainDir = getOrCreateDir("SereneVideo");
    }

    private static File getOrCreateDir(String name){
        File file = new File(name);
        if (!file.isDirectory()) {
            file.mkdir();
        }
        return file;
    }

    private static File getOrCreateDir(String name, File parent){
        File file = new File(parent, name);
        if (!file.isDirectory()) {
            file.mkdir();
        }
        return file;
    }
}
