package net.mineslam.teams.team;

import net.mineslam.teams.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Team {

    private String teamID;
    private UUID teamLeader;
    private int teamKills;
    private int teamDeaths;
    private boolean friendlyFire;
    private Map<UUID, Boolean> teamMembers;
    private Map<UUID, Long> teamInvites = new HashMap<>();

    public Team(String teamID, UUID teamLeader) {
        this.teamID = teamID;
        this.teamLeader = teamLeader;
        this.teamKills = 0;
        this.teamDeaths = 0;
        this.friendlyFire = false;
        this.teamMembers = new HashMap<>();
        teamMembers.put(teamLeader, true);
    }

    public Team(String teamID, UUID teamLeader, int teamKills, int teamDeaths, boolean friendlyFire, Map<UUID, Boolean> teamMembers) {
        this.teamID = teamID;
        this.teamLeader = teamLeader;
        this.teamKills = teamKills;
        this.teamDeaths = teamDeaths;
        this.friendlyFire = friendlyFire;
        this.teamMembers = teamMembers;
    }

    public String getTeamID() {
        return teamID;
    }

    public boolean isTeamLeader(Player player) {
        return teamLeader.equals(player.getUniqueId());
    }

    public int getTeamKills() {
        return teamKills;
    }

    public void incrementKills() {
        teamKills += 1;
    }

    public int getTeamDeaths() {
        return teamDeaths;
    }

    public void incrementDeaths() {
        teamDeaths += 1;
    }

    public boolean hasFriendlyFire() {
        return friendlyFire;
    }

    public void toggleFriendlyFire() {
        friendlyFire = !friendlyFire;
    }

    public boolean isFull() {
        return teamMembers.size() >= Settings.General.MAX_MEMBERS.getInt();
    }

    public Map<UUID, Boolean> getTeamMembers() {
        return teamMembers;
    }

    public boolean hasMember(UUID player) {
        return teamMembers.containsKey(player);
    }

    public void addMember(Player player) {
        teamMembers.put(player.getUniqueId(), false);
        if (teamInvites.containsKey(player.getUniqueId())) {
            teamInvites.remove(player.getUniqueId());
        }
    }

    public void addMember(UUID player, boolean promoted) {
        teamMembers.put(player, promoted);
    }

    public void removeMember(UUID player) {
        teamMembers.remove(player);
    }

    public boolean isPromoted(UUID player) {
        return teamLeader.equals(player) || teamMembers.get(player);
    }

    public void setPromoted(UUID player, boolean promoted) {
        teamMembers.put(player, promoted);
    }

    public boolean isInvited(Player player) {
        return teamInvites.containsKey(player.getUniqueId()) &&
                System.currentTimeMillis() - teamInvites.get(player.getUniqueId()) < Settings.General.INVITE_EXPIRE.getInt() * 1000;
    }

    public void invitePlayer(Player player) {
        teamInvites.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean notify(String message) {
        String msg = ChatColor.translateAlternateColorCodes('&', "&a[&fMineslamTeams&a] " + message);
        for (UUID uuid : teamMembers.keySet()) {
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPlayer(uuid).sendMessage(msg);
            }
        }
        return true;
    }

    public Object[] serialize() {
        Object[] output = new Object[5];
        output[0] = teamID;
        output[1] = teamLeader.toString();
        output[2] = teamKills;
        output[3] = teamDeaths;
        output[4] = friendlyFire ? 1 : 0;
        return output;
    }
}
