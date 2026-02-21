package me.yourname.chips;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.setLoggedIn(player.getUniqueId(), false);
        if (plugin.isRegistered(player.getUniqueId())) {
            player.sendMessage(ChatColor.GOLD + "Please use /login <password>");
        } else {
            player.sendMessage(ChatColor.GOLD + "Please use /register <password> <confirm>");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeSession(event.getPlayer().getUniqueId());
    }

    private boolean shouldBlock(Player player) {
        return !plugin.isLoggedIn(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (shouldBlock(event.getPlayer())) {
            if (event.getFrom().getX() != event.getTo().getX() || 
                event.getFrom().getZ() != event.getTo().getZ() || 
                event.getFrom().getY() != event.getTo().getY()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (shouldBlock(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Login required.");
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (shouldBlock(event.getPlayer())) {
            String msg = event.getMessage().toLowerCase();
            if (!msg.startsWith("/login") && !msg.startsWith("/register")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Login required.");
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
        if (event.getWhoClicked() instanceof Player && shouldBlock((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (shouldBlock(event.getPlayer())) event.setCancelled(true);
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && shouldBlock((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && shouldBlock((Player) event.getDamager())) {
            event.setCancelled(true);
        }
    }
}
