package com.dola.chips;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

public final class AuthCommand implements CommandExecutor {

    private final Chips plugin;

    public AuthCommand(Chips plugin) {
        this.plugin = plugin;
    }

    private AuthManager auth() {
        return plugin.getAuthManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String name = cmd.getName().toLowerCase(Locale.ROOT);

        // Player-only commands
        if ((name.equals("register") || name.equals("login") || name.equals("changepassword"))
                && !(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        switch (name) {
            case "register":
                return handleRegister((Player) sender, args);

            case "login":
                return handleLogin((Player) sender, args);

            case "changepassword":
                return handleChangePassword((Player) sender, args);

            case "resetpassword":
                return handleResetPassword(sender, args);

            default:
                return false;
        }
    }

    private boolean handleRegister(Player p, String[] args) {
        if (args.length != 2) {
            p.sendMessage("Usage: /register <password> <confirmPassword>");
            return true;
        }

        UUID uuid = p.getUniqueId();
        if (auth().isRegistered(uuid)) {
            p.sendMessage("You are already registered.");
            return true;
        }

        boolean ok = auth().registerPlayer(uuid, args[0], args[1]);
        if (ok) {
            auth().setLoggedIn(uuid, true);
            p.sendMessage("Registered successfully.");
        } else {
            p.sendMessage("Register failed. Passwords must match and be at least "
                    + auth().getMinPasswordLength() + " characters.");
        }
        return true;
    }

    private boolean handleLogin(Player p, String[] args) {
        if (args.length != 1) {
            p.sendMessage("Usage: /login <password>");
            return true;
        }

        UUID uuid = p.getUniqueId();
        if (!auth().isRegistered(uuid)) {
            p.sendMessage("You are not registered. Use /register first.");
            return true;
        }

        boolean ok = auth().validateLogin(uuid, args[0]);
        if (ok) {
            auth().setLoggedIn(uuid, true);
            p.sendMessage("Logged in successfully.");
        } else {
            p.sendMessage("Incorrect password.");
        }
        return true;
    }

    private boolean handleChangePassword(Player p, String[] args) {
        if (args.length != 3) {
            p.sendMessage("Usage: /changepassword <oldPassword> <newPassword> <confirmNewPassword>");
            return true;
        }

        UUID uuid = p.getUniqueId();
        if (!auth().isRegistered(uuid)) {
            p.sendMessage("You are not registered.");
            return true;
        }

        boolean ok = auth().changePassword(uuid, args[0], args[1], args[2]);
        if (ok) {
            p.sendMessage("Password changed successfully.");
        } else {
            p.sendMessage("Password change failed. Check old password, confirmation, and minimum length ("
                    + auth().getMinPasswordLength() + ").");
        }
        return true;
    }

    private boolean handleResetPassword(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chips.admin")) {
            sender.sendMessage("You don't have permission to do that.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /resetpassword <player> <newPassword>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = target.getUniqueId();

        if (!auth().isRegistered(uuid)) {
            sender.sendMessage("That player is not registered.");
            return true;
        }

        boolean ok = auth().resetPassword(uuid, args[1]);
        sender.sendMessage(ok
                ? "Password reset for " + args[0] + "."
                : "Reset failed (minimum length is " + auth().getMinPasswordLength() + ").");
        return true;
    }
                }
