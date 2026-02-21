package me.yourname.chips;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommands implements CommandExecutor {

    private final Chips plugin;

    public AuthCommands(Chips plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("register")) {
            if (plugin.isRegistered(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Already registered.");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage(ChatColor.RED + "/register <password> <confirm>");
                return true;
            }
            if (!args[0].equals(args[1])) {
                player.sendMessage(ChatColor.RED + "Passwords do not match.");
                return true;
            }
            plugin.registerUser(player.getUniqueId(), args[0]);
            plugin.setLoggedIn(player.getUniqueId(), true);
            player.sendMessage(ChatColor.GREEN + "Registered & Logged in.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("login")) {
            if (plugin.isLoggedIn(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Already logged in.");
                return true;
            }
            if (!plugin.isRegistered(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Not registered.");
                return true;
            }
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "/login <password>");
                return true;
            }
            if (plugin.checkPassword(player.getUniqueId(), args[0])) {
                plugin.setLoggedIn(player.getUniqueId(), true);
                player.sendMessage(ChatColor.GREEN + "Login successful.");
            } else {
                player.sendMessage(ChatColor.RED + "Wrong password.");
            }
            return true;
        }
        return false;
    }
                                  }
