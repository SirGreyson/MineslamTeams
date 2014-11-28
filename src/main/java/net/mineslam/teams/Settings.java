package net.mineslam.teams;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {

    private static FileConfiguration config;

    public static void setConfig(MineslamTeams plugin) {
        plugin.saveDefaultConfig();
        Settings.config = plugin.getConfig();
    }

    public enum General {

        MAX_MEMBERS, INVITE_EXPIRE;

        public int getInt() {
            return config.getInt(name());
        }
    }

    public enum MySQL {

        HOST, PORT, USERNAME, DATABASE, PASSWORD;

        public String getString() {
            return config.getString("mySQL." + name());
        }
    }
}
