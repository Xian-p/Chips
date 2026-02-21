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

public class AuthListener implements Listener {

    private final Chips plugin;

    public AuthListener(Chips plugin) {
        this.plugin = plugin;
    }

    private boolean shouldBlock(Player player) {
        return !plugin.isLoggedIn(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.setLoggedIn(player.getUniqueId(), false); // Always require login on join

        if (plugin.isRegistered(player.getUniqueId())) {
            player.sendMessage(Component.text("Welcome back! Please /login <password>", NamedTextColor.GOLD));
        } else {
            player.sendMessage(Component.text("Welcome! Please /register <password> <confirm>", NamedTextColor.GOLD));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeSession(event.getPlayer().getUniqueId());
    }

    // --- Blockers ---

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (shouldBlock(event.getPlayer())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            
            // Allow rotating head (Yaw/Pitch) but prevent XYZ movement
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
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (shouldBlock(event.getPlayer())) {
            String msg = event.getMessage().toLowerCase();
            // Whitelist specific commands
            if (!msg.startsWith("/login") && !msg.startsWith("/register") && !msg.startsWith("/l") && !msg.startsWith("/reg")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("Please login first!", NamedTextColor.RED));
            }
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
