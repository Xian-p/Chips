package me.chips;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AuthCommands implements CommandExecutor {

    private final Chips plugin;

    public AuthCommands(Chips plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use auth commands.", NamedTextColor.RED));
            return true;
        }

        UUID uuid = player.getUniqueId();
        String cmd = command.getName().toLowerCase();

        // REGISTER
        if (cmd.equals("register")) {
            if (plugin.isRegistered(uuid)) {
                player.sendMessage(Component.text("Already registered! Use /login <password>", NamedTextColor.RED));
                return true;
            }
            if (args.length != 2) {
                player.sendMessage(Component.text("Usage: /register <password> <confirm>", NamedTextColor.RED));
                return true;
            }
            if (!args[0].equals(args[1])) {
                player.sendMessage(Component.text("Passwords do not match.", NamedTextColor.RED));
                return true;
            }
            plugin.registerUser(uuid, args[0]);
            plugin.setLoggedIn(uuid, true);
            player.sendMessage(Component.text("Successfully registered & logged in!", NamedTextColor.GREEN));
            return true;
        }

        // LOGIN
        if (cmd.equals("login")) {
            if (plugin.isLoggedIn(uuid)) {
                player.sendMessage(Component.text("Already logged in.", NamedTextColor.RED));
                return true;
            }
            if (!plugin.isRegistered(uuid)) {
                player.sendMessage(Component.text("Not registered! Use /register <pass> <confirm>", NamedTextColor.RED));
                return true;
            }
            if (args.length != 1) {
                player.sendMessage(Component.text("Usage: /login <password>", NamedTextColor.RED));
                return true;
            }
            if (plugin.checkPassword(uuid, args[0])) {
                plugin.setLoggedIn(uuid, true);
                player.sendMessage(Component.text("Login successful!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Incorrect password.", NamedTextColor.RED));
            }
            return true;
        }

        // LOGOUT
        if (cmd.equals("logout")) {
            if (!plugin.isLoggedIn(uuid)) {
                player.sendMessage(Component.text("You are not logged in.", NamedTextColor.RED));
                return true;
            }
            plugin.setLoggedIn(uuid, false);
            player.sendMessage(Component.text("Logged out.", NamedTextColor.GREEN));
            return true;
        }
        
        // CHANGE PASSWORD
        if (cmd.equals("changepassword")) {
            if (!plugin.isLoggedIn(uuid)) {
                player.sendMessage(Component.text("You must be logged in to change password.", NamedTextColor.RED));
                return true;
            }
            if (args.length != 2) {
                player.sendMessage(Component.text("Usage: /changepassword <old> <new>", NamedTextColor.RED));
                return true;
            }
            if (plugin.checkPassword(uuid, args[0])) {
                plugin.changePassword(uuid, args[1]);
                player.sendMessage(Component.text("Password updated!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Old password is incorrect.", NamedTextColor.RED));
            }
            return true;
        }

        return false;
    }
}
