package net.mineslam.teams;

import net.mineslam.teams.team.TeamManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MineslamTeams extends JavaPlugin {

    private DataManager dataManager;
    private TeamManager teamManager;

    public void onEnable() {
        Settings.setConfig(this);
        getDataManager().setupConnection();
        getTeamManager().loadTeams();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().info("has been enabled");
    }

    public void onDisable() {
        getTeamManager().saveTeams();
        getDataManager().closeConnection();
        getLogger().info("has been disabled");
    }

    public DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = new DataManager(this);
        }
        return dataManager;
    }

    public TeamManager getTeamManager() {
        if (teamManager == null) {
            teamManager = new TeamManager(this);
        }
        return teamManager;
    }
}
