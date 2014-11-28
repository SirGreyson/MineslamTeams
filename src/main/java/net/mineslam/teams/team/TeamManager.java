package net.mineslam.teams.team;

import net.mineslam.teams.MineslamTeams;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class TeamManager {

    private MineslamTeams plugin;
    private Map<String, Team> loadedTeams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public TeamManager(MineslamTeams plugin) {
        this.plugin = plugin;
        plugin.getCommand("team").setExecutor(new TeamCommand(this));
    }

    public void loadTeams() {
        try {
            //Get all of the Teams in the team_data Table
            ResultSet res = plugin.getDataManager().querySQL("SELECT * FROM team_data;");
            while (res.next()) {
                String teamID = res.getString("team_id");
                OfflinePlayer leader = Bukkit.getOfflinePlayer(UUID.fromString(res.getString("leader")));
                //Check that the Team leader has been on in the last 30 days; and if so load Team data
                if (leader != null || (System.currentTimeMillis() - leader.getLastPlayed()) * 1000 < 2592000) {
                    Team team = new Team(teamID, leader.getUniqueId(), res.getInt("kills"), res.getInt("deaths"), res.getBoolean("friendly_fire"), new HashMap<UUID, Boolean>());
                    //Load Team Members from player_data Table
                    ResultSet mRes = plugin.getDataManager().querySQL("SELECT * FROM player_data WHERE team_id = '" + teamID + "';");
                    while (mRes.next()) {
                        team.addMember(UUID.fromString(mRes.getString("player_id")), mRes.getBoolean("promoted"));
                    }
                    loadedTeams.put(teamID, team);
                    //Otherwise, remove this Team from the Database
                } else {
                    plugin.getDataManager().updateSQL("DELETE FROM team_data WHERE team_id = '" + teamID + "';");
                    //Delete Team Members from player_data
                    plugin.getDataManager().updateSQL("DELETE FROM player_data WHERE team_id = '" + teamID + "';");
                    plugin.getLogger().info("Deleted row for Team with ID " + teamID);
                }
            }
            //Print success message
            plugin.getLogger().info("Successfully loaded " + loadedTeams.size() + " Teams from MySQL Database!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not load Team data from MySQL Database: " + e.getMessage());
        }
    }

    public void saveTeams() {
        for (Team team : loadedTeams.values()) {
            Object[] values = team.serialize();
            try {
                //Save Team to team_date Table
                plugin.getDataManager().updateSQL("INSERT INTO team_data (team_id, leader, kills, deaths, friendly_fire) VALUES (" +
                        "'" + values[0] + "', " +
                        "'" + values[1] + "', " +
                        "'" + values[2] + "', " +
                        "'" + values[3] + "', " +
                        "'" + values[4] + "') " +
                        "ON DUPLICATE KEY UPDATE " +
                        "team_id = VALUES(team_id), leader = VALUES(leader), kills = VALUES(kills), deaths = VALUES(deaths), friendly_fire = VALUES(friendly_fire);");
                //Save members to player_data Table
                for (UUID member : team.getTeamMembers().keySet()) {
                    plugin.getDataManager().updateSQL("INSERT INTO player_data (player_id, team_id, promoted) VALUES (" +
                            "'" + member.toString() + "', " +
                            "'" + team.getTeamID() + "', " +
                            "'" + (team.isPromoted(member) ? 1 : 0) + "') " +
                            "ON DUPLICATE KEY UPDATE " +
                            "player_id = VALUES(player_id), team_id = VALUES(team_id), promoted = VALUES(promoted);");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error! Could not save Team to MySQL Database: " + e.getMessage());
            }
        }
    }

    public boolean teamExists(String teamID) {
        return loadedTeams.containsKey(teamID);
    }

    public Team getTeam(String teamID) {
        return loadedTeams.get(teamID);
    }

    public Team getPlayerTeam(Player player) {
        for (Team team : loadedTeams.values()) {
            if (team.hasMember(player.getUniqueId())) {
                return team;
            }
        }
        return null;
    }

    public void createTeam(String teamID, Player player) {
        loadedTeams.put(teamID, new Team(teamID, player.getUniqueId()));
    }

    public void disbandTeam(Team team) {
        team.notify("Your Team leader has disbanded your Team");
        try {
            //Delete Team from team_data Table
            plugin.getDataManager().updateSQL("DELETE FROM team_data WHERE team_id = '" + team.getTeamID() + "';");
            //Delete members from player_data Table
            plugin.getDataManager().updateSQL("DELETE FROM player_data WHERE team_id = '" + team.getTeamID() + "';");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not remove Team from MySQL Database: " + e.getMessage());
        }
        loadedTeams.remove(team.getTeamID());
    }

    public void removeTeamMember(Team team, UUID member) {
        try {
            plugin.getDataManager().updateSQL("DELETE FROM player_data WHERE player_id = '" + member.toString() + "';");
            team.removeMember(member);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not remove Team member: " + e.getMessage());
        }
    }
}
