package me.chips;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

        String cmd = command.getName().toLowerCase();

        // LOGOUT (Safe to be here)
        if (cmd.equals("logout")) {
            if (!plugin.isLoggedIn(player.getUniqueId())) {
                player.sendMessage(Component.text("You are not logged in.", NamedTextColor.RED));
                return true;
            }
            plugin.setLoggedIn(player.getUniqueId(), false);
            plugin.applyBlindness(player); // Re-apply blindness
            player.sendMessage(Component.text("Logged out.", NamedTextColor.GREEN));
            return true;
        }

        return false;
    }
}
