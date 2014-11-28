package net.mineslam.teams.team;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    private TeamManager teamManager;

    public TeamCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return msg(sender, "This command cannot be run from the console!");
        }
        if (args.length < 1) {
            return msg(sender, "&cInvalid arguments! Try &f/team help &cfor help");
        }
        Player player = (Player) sender;

        //Help Sub-Command
        if (args[0].equalsIgnoreCase("help")) {
            return msg(sender, "&aUse &f/team &a+ one of the following:" +
                    "\n&fcreate <name> &a- create a new Team with the given name" +
                    "\n&fdisband " + (sender.isOp() || sender.hasPermission("teams.admin") ? "<name> " : "") + " &a- disband your Team" +
                    "\n&fstats [name] &a- view stats for your own or a specific Team" +
                    "\n&fjoin <name> &a- join a Team you have been invited to" +
                    "\n&fleave &a- leave your current Team" +
                    "\n&fff &a- view/toggle friendly fire status for your Team" +
                    "\n&finvite <player> &a- invite a player to join your Team" +
                    "\n&fkick <player> &a- kick a member from your Team" +
                    "\n&fpromote <player> &a- promote a member on your Team");
        }
        //Team Creation Sub-Command
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length != 2) {
                return msg(sender, "&cInvalid arguments! Syntax: &f/team create <name>");
            }
            if (teamManager.getPlayerTeam(player) != null) {
                return msg(sender, "&cYou are already on a Team! Type &f/team leave &cto leave");
            }
            if (teamManager.teamExists(args[1])) {
                return msg(sender, "&cSorry, there is already a Team with that name!");
            }
            if (!StringUtils.isAlpha(args[1])) {
                return msg(sender, "&cSorry, your Team name can only contain letters!");
            }
            if (args[1].length() > 16) {
                return msg(sender, "&cYour Team name must be under 16 characters!");
            }
            teamManager.createTeam(args[1], player);
            return msg(sender, "&aYou have successfully created a new Team! Use &f/team invite <player> &ato invite new members");
        }
        //Team Disbanding Sub-Command
        if (args[0].equalsIgnoreCase("disband") || args[0].equalsIgnoreCase("delete")) {
            //Admin/Staff Disband
            if (args.length == 2) {
                if (!sender.isOp() && !sender.hasPermission("teams.admin")) {
                    return msg(sender, "&cYou do not have permission to use this command!");
                }
                if (!teamManager.teamExists(args[1])) {
                    return msg(sender, "&cThere is no Team with that ID");
                }
                Team team = teamManager.getTeam(args[1]);
                teamManager.disbandTeam(team);
                return msg(sender, "&aYou have successfully disbanded the Team with an ID of &f" + team.getTeamID());
            }
            //Team Leader Disband
            Team team = teamManager.getPlayerTeam(player);
            if (team == null) {
                return msg(sender, "&cYou are not currently on a Team!");
            }
            if (!team.isTeamLeader(player)) {
                return msg(sender, "&cYou cannot disband your Team because you are not the Team leader!");
            }
            teamManager.disbandTeam(team);
            return msg(sender, "&aYou have successfully disbanded your Team!");
        }
        //Team Statistics Sub-Command
        if (args[0].equalsIgnoreCase("stats")) {
            //Specific Team Stats
            if (args.length == 2) {
                if (!teamManager.teamExists(args[1])) {
                    return msg(sender, "&cThere is no Team with that ID");
                }
                Team team = teamManager.getTeam(args[1]);
                return msg(sender, "&aStatistics for Team &f" + team.getTeamID() + "&a:" +
                        "\n&aKills: &c" + team.getTeamKills() +
                        "\n&aDeaths: &c" + team.getTeamDeaths() +
                        "\n&aK/D Ratio: &c" +
                        (team.getTeamDeaths() < 1 ? "0.0" : String.valueOf(team.getTeamKills() / team.getTeamDeaths())));
            }
            //Sender Team Stats
            Team team = teamManager.getPlayerTeam(player);
            if (team == null) {
                return msg(sender, "&cYou are not currently on a Team!");
            }
            return msg(sender, "&aStatistics for Team &f" + team.getTeamID() + "&a:" +
                    "\n&aKills: &c" + team.getTeamKills() +
                    "\n&aDeaths: &c" + team.getTeamDeaths() +
                    "\n&aK/D Ratio: &c" +
                    (team.getTeamDeaths() < 1 ? "0.0" : String.valueOf(team.getTeamKills() / team.getTeamDeaths())));
        }
        //Team Friendly-Fire Sub-Command
        if (args[0].equalsIgnoreCase("friendlyfire") || args[0].equalsIgnoreCase("ff")) {
            Team team = teamManager.getPlayerTeam(player);
            if (team == null) {
                return msg(sender, "&cYou are not currently on a Team!");
            }
            if (!team.isPromoted(player.getUniqueId())) {
                return msg(sender, "&aFriendly fire is currently " + (team.hasFriendlyFire() ? "&fENABLED" : "&cDISABLED") + " &afor your Team");
            }
            team.toggleFriendlyFire();
            return team.notify("&aFriendly fire is now " + (team.hasFriendlyFire() ? "&fENABLED" : "&cDISABLED") + " &afor your Team");
        }
        //Team Member Kick Sub-Command
        if (args[0].equalsIgnoreCase("kick")) {
            if (args.length != 2) {
                return msg(sender, "&cInvalid arguments! Syntax: &f/team kick <player>");
            }
            Team team = teamManager.getPlayerTeam(player);
            if (team == null) {
                return msg(sender, "&cYou are not currently on a Team!");
            }
            if (!team.isTeamLeader(player)) {
                return msg(sender, "&cOnly the leader of your Team can kick members!");
            }
            OfflinePlayer member = Bukkit.getOfflinePlayer(args[1]);
            if (member == null || !team.hasMember(member.getUniqueId())) {
                return msg(sender, "&cThere is no member on your Team with that name!");
            }
            teamManager.removeTeamMember(team, member.getUniqueId());
            if (member.isOnline()) {
                msg(member.getPlayer(), "&cYou have been kicked from your Team!");
            }
            return team.notify("&f" + member.getName() + " &ahas been kicked from your Team!");
        }
        //Team Leaving Sub-Command
        if (args[0].equalsIgnoreCase("leave")) {
            Team team = teamManager.getPlayerTeam(player);
            if (team == null) {
                return msg(sender, "&cYou are not currently on a Team!");
            }
            if (team.isTeamLeader(player)) {
                return msg(sender, "&cYou cannot leave your Team because you are the leader! Use &f/team disband &cto disband your Team");
            }
            teamManager.removeTeamMember(team, player.getUniqueId());
            team.notify("&f" + player.getName() + " &ahas left your Team");
            return msg(sender, "&aYou have successfully left your Team!");
        }
        //Team Invitation Sub-Command
        if (args[0].equalsIgnoreCase("invite")) {
            if (args.length != 2) {
                return msg(sender, "&cInvalid arguments! Syntax: &f/team invite <player>");
            }
            Team team = teamManager.getPlayerTeam(player);
            if (team == null) {
                return msg(sender, "&cYou are not currently on a Team!");
            }
            if (!team.isPromoted(player.getUniqueId())) {
                return msg(sender, "&cOnly promoted members and the Team leader can invite new members");
            }
            if (args[1].equalsIgnoreCase(player.getName())) {
                return msg(sender, "&cYou cannot invite yourself to your Team!");
            }
            Player invited = Bukkit.getPlayer(args[1]);
            if (invited == null) {
                return msg(sender, "&cThere is no online player with that name!");
            }
            if (team.isInvited(invited)) {
                return msg(sender, "&cThat player has already been invited to your Team!");
            }
            if (team.hasMember(invited.getUniqueId())) {
                return msg(sender, "&cThat player is already on your Team!");
            }
            if (team.isFull()) {
                return msg(sender, "&cYour Team already has the maximum number of players!");
            }
            team.invitePlayer(invited);
            msg(invited, "&f" + player.getName() + " &ahas invited you to join their Team! Type &f/team join " + team.getTeamID() + " &ato accept");
            return msg(sender, "&aYou have successfully invited &f" + invited.getName() + " &ato join your Team!");
        }
        //Team Joining Sub-Command
        if (args[0].equalsIgnoreCase("join")) {
            if (args.length != 2) {
                return msg(sender, "&cInvalid arguments! Syntax: &f/team join <team>");
            }
            if (teamManager.getPlayerTeam(player) != null) {
                return msg(sender, "&cYou are already on a Team! Type &f/team leave &cto leave");
            }
            if (!teamManager.teamExists(args[1])) {
                return msg(sender, "&cThere is no Team with that ID!");
            }
            Team team = teamManager.getTeam(args[1]);
            if (!team.isInvited(player)) {
                return msg(sender, "&cYou do not have a pending invite to join this Team!");
            }
            if (team.isFull()) {
                return msg(sender, "&cSorry, this Team is currently full");
            }
            team.notify("&f" + player.getName() + " &ahas joined your Team!");
            team.addMember(player);
            return msg(sender, "&aYou have successfully joined the Team with an ID of &f" + team.getTeamID());
        }
        //Team Member Promotion Sub-Command
        if (args[0].equalsIgnoreCase("promote")) {
            if (args.length != 2) {
                return msg(sender, "&cInvalid arguments! Syntax: &f/team promote <player>");
            }
            Team team = teamManager.getPlayerTeam(player);
            if (team == null) {
                return msg(sender, "&cYou are not currently on a Team!");
            }
            if (!team.isTeamLeader(player)) {
                return msg(sender, "&cOnly the Team leader can promote members!");
            }
            OfflinePlayer member = Bukkit.getOfflinePlayer(args[1]);
            if (member == null || !team.hasMember(member.getUniqueId())) {
                return msg(sender, "&cThere is no member on your Team with that name!");
            }
            if (team.isPromoted(member.getUniqueId())) {
                return msg(sender, "&cThat member has already been promoted!");
            }
            team.setPromoted(member.getUniqueId(), true);
            return team.notify("&f" + member.getName() + " &ahas been promoted!");
        }
        //Invalid Sub-Command Message
        return msg(sender, "&cInvalid arguments! Try &f/team help &cfor help");
    }

    private boolean msg(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&fMineslamTeams&a] " + message));
        return true;
    }
}
