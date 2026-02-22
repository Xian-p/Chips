package me.chips;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.UUID;

public class AuthListener implements Listener {

    private final Chips plugin;

    public AuthListener(Chips plugin) {
        this.plugin = plugin;
    }

    // --- COMMAND INTERCEPTOR ---
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage(); // e.g. "/login password"
        String[] args = message.split(" ");
        String cmd = args[0].toLowerCase();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 1. HANDLE LOGIN
        if (cmd.equals("/login")) {
            event.setCancelled(true); // Stop standard processing

            if (plugin.isLoggedIn(uuid)) {
                player.sendMessage(Component.text("Already logged in.", NamedTextColor.RED));
                return;
            }
            if (!plugin.isRegistered(uuid)) {
                player.sendMessage(Component.text("Not registered! Use /register <password> <password>", NamedTextColor.RED));
                return;
            }
            if (args.length != 2) {
                player.sendMessage(Component.text("Usage: /login <password>", NamedTextColor.RED));
                return;
            }
            if (plugin.checkPassword(uuid, args[1])) {
                plugin.setLoggedIn(uuid, true);
                plugin.removeBlindness(player);
                player.sendMessage(Component.text("Login successful!", NamedTextColor.GREEN));
                plugin.getLogger().info(player.getName() + " logged in."); // Safe Console Log
            } else {
                player.sendMessage(Component.text("Incorrect password.", NamedTextColor.RED));
            }
        }

        // 2. HANDLE REGISTER
        else if (cmd.equals("/register")) {
            event.setCancelled(true); 

            if (plugin.isRegistered(uuid)) {
                player.sendMessage(Component.text("Already registered! Use /login <password>", NamedTextColor.RED));
                return;
            }
            if (args.length != 3) {
                player.sendMessage(Component.text("Usage: /register <password> <password>", NamedTextColor.RED));
                return;
            }
            if (!args[1].equals(args[2])) {
                player.sendMessage(Component.text("Passwords do not match.", NamedTextColor.RED));
                return;
            }
            plugin.registerUser(uuid, args[1]);
            plugin.setLoggedIn(uuid, true);
            plugin.removeBlindness(player);
            player.sendMessage(Component.text("Successfully registered & logged in!", NamedTextColor.GREEN));
            plugin.getLogger().info(player.getName() + " registered."); // Safe Console Log
        }

        // 3. HANDLE CHANGE PASSWORD
        else if (cmd.equals("/changepassword")) {
            event.setCancelled(true);

            if (!plugin.isLoggedIn(uuid)) {
                player.sendMessage(Component.text("You must be logged in.", NamedTextColor.RED));
                return;
            }
            if (args.length != 3) {
                player.sendMessage(Component.text("Usage: /changepassword <old> <new>", NamedTextColor.RED));
                return;
            }
            if (plugin.checkPassword(uuid, args[1])) {
                plugin.changePassword(uuid, args[2]);
                player.sendMessage(Component.text("Password updated!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Old password is incorrect.", NamedTextColor.RED));
            }
        }
        
        // 4. BLOCK OTHER COMMANDS
        else if (shouldBlock(player)) {
            if (!cmd.equals("/l") && !cmd.equals("/reg")) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Please login first!", NamedTextColor.RED));
            }
        }
    }

    // --- STANDARD SECURITY ---
    private boolean shouldBlock(Player player) {
        return !plugin.isLoggedIn(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.setLoggedIn(player.getUniqueId(), false); 
        plugin.applyBlindness(player); 

        if (plugin.isRegistered(player.getUniqueId())) {
            player.sendMessage(Component.text("Welcome back! Please /login <password>", NamedTextColor.GOLD));
        } else {
            player.sendMessage(Component.text("Welcome! Please /register <password> <password>", NamedTextColor.GOLD));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeSession(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (shouldBlock(event.getPlayer())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setTo(from);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (shouldBlock(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Please login first!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (shouldBlock(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (shouldBlock(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (shouldBlock(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && shouldBlock(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (shouldBlock(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && shouldBlock(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && shouldBlock(player)) {
            event.setCancelled(true);
        }
    }
}
