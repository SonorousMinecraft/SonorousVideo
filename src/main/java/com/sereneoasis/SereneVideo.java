package com.sereneoasis;

import com.sereneoasis.command.SerenityCommand;
import com.sereneoasis.config.FileManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/***
 * The main class of the plugin
 */
public class SereneVideo extends JavaPlugin {

    public static SereneVideo plugin;

    private static FileManager fileManager;

    /***
     * Returns the class used to manage files
     * @return the file manager
     */
    public static FileManager getFileManager() {
        return fileManager;
    }


    @Override
    public void onEnable() {
        plugin = this;
        fileManager = new FileManager();

        this.getCommand("SereneVideo").setExecutor(new SerenityCommand());

    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "SereneVideo was disabled successfully.");
    }

}