package net.mineslam.teams;

import net.mineslam.teams.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {

    private MineslamTeams plugin;

    public PlayerListener(MineslamTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Team team = plugin.getTeamManager().getPlayerTeam(e.getEntity());
        if (team != null) {
            team.incrementDeaths();
        }
        if (e.getEntity().getKiller() != null) {
            Team team2 = plugin.getTeamManager().getPlayerTeam(e.getEntity().getKiller());
            if (team2 != null) {
                team2.incrementKills();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || (!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Projectile))) {
            return;
        }
        if (e.getDamager() instanceof Projectile && !(((Projectile) e.getDamager()).getShooter() instanceof Player)) {
            return;
        }
        Team team1 = plugin.getTeamManager().getPlayerTeam((Player) e.getEntity());
        Team team2 = plugin.getTeamManager().getPlayerTeam((Player) (e.getDamager() instanceof Projectile ? ((Projectile) e.getDamager()).getShooter() : e.getDamager()));
        if (team1 != null && team2 != null && team1.getTeamID().equals(team2.getTeamID())) {
            if (!team1.hasFriendlyFire()) {
                ((Player) e.getDamager()).sendMessage(ChatColor.RED + "Friendly fire is disabled on your Team!");
                e.setCancelled(true);
            }
        }
    }
}
