package com.dola.chips;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommand implements CommandExecutor {
    private final Chips plugin;

    public AuthCommand(Chips plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p) && !cmd.getName().equalsIgnoreCase("resetpassword")) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        switch (cmd.getName().toLowerCase()) {
            case "register":
                return handleRegister(p, args);
            case "login":
                return handleLogin(p, args);
            case "changepassword":
                return handleChangePassword(p, args);
            case "resetpassword":
                return handleResetPassword(sender, args);
            default:
                return false;
        }
    }

    private boolean handleRegister(Player p, String[] args) {
        if (args.length < 2) return false;

        if (plugin.getAuthManager().isRegistered(p.getUniqueId())) {
            p.sendMessage(plugin.getConfig().getString("messages.account-already-exists").replace('&', '§'));
            return true;
        }

        if (args[0].length() < plugin.getAuthManager().getMinPasswordLength()) {
            p.sendMessage(plugin.getConfig().getString("messages.password-too-short")
                    .replace("%length%", String.valueOf(plugin.getAuthManager().getMinPasswordLength()))
                    .replace('&', '§'));
            return true;
        }

        if (plugin.getAuthManager().registerPlayer(p.getUniqueId(), args[0], args[1])) {
            p.sendMessage(plugin.getConfig().getString("messages.register-success").replace('&', '§'));
        } else {
            p.sendMessage(plugin.getConfig().getString("messages.password-mismatch").replace('&', '§'));
        }
        return true;
    }

    private boolean handleLogin(Player p, String[] args) {
        if (args.length < 1) return false;

        if (!plugin.getAuthManager().isRegistered(p.getUniqueId())) {
            p.sendMessage(plugin.getConfig().getString("messages.not-registered").replace('&', '§'));
            return true;
        }

        if (plugin.getAuthManager().isLoggedIn(p.getUniqueId())) {
            p.sendMessage("&aYou are already logged in!".replace('&', '§'));
            return true;
        }

        if (plugin.getAuthManager().validateLogin(p.getUniqueId(), args[0])) {
            plugin.getAuthManager().setLoggedIn(p.getUniqueId(), true);
            p.sendMessage(plugin.getConfig().getString("messages.login-success").replace('&', '§'));
        } else {
            p.sendMessage("&cIncorrect password!".replace('&', '§'));
        }
        return true;
    }

    private boolean handleChangePassword(Player p, String[] args) {
        if (args.length < 3) return false;

        if (!plugin.getAuthManager().isRegistered(p.getUniqueId())) {
            p.sendMessage(plugin.getConfig().getString("messages.not-registered").replace('&', '§'));
            return true;
        }

        if (plugin.getAuthManager().changePassword(p.getUniqueId(), args[0], args[1], args[2])) {
            p.sendMessage(plugin.getConfig().getString("messages.password-changed").replace('&', '§'));
        } else {
            p.sendMessage("&cFailed to change password! Check old password or new password requirements.".replace('&', '§'));
        }
        return true;
    }

    private boolean handleResetPassword(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chips.admin")) {
            sender.sendMessage("&cNo permission!".replace('&', '§'));
            return true;
        }

        if (args.length < 2) return false;

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("&cPlayer not found!".replace('&', '§'));
            return true;
        }

        if (plugin.getAuthManager().resetPassword(target.getUniqueId(), args[1])) {
            sender.sendMessage("&aSuccessfully reset password for " + target.getName());
            target.sendMessage(plugin.getConfig().getString("messages.password-reset").replace('&', '§'));
        } else {
            sender.sendMessage("&cFailed to reset password! Check new password length.".replace('&', '§'));
        }
        return true;
    }
        }
            
