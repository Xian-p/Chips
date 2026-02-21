package com.dola.chips;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.UUID;

public final class AuthListener implements Listener {

    private final Chips plugin;

    public AuthListener(Chips plugin) {
        this.plugin = plugin;
    }

    private AuthManager auth() {
        return plugin.getAuthManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        auth().setLoggedIn(uuid, false);

        if (auth().isRegistered(uuid)) {
            p.sendMessage("Please login using /login <password>");
        } else {
            p.sendMessage("Please register using /register <password> <confirmPassword>");
        }

        Location spawn = getDefaultSpawn(p);
        p.teleport(spawn);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        auth().setLoggedIn(event.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        if (auth().isLoggedIn(uuid)) return;

        // Only cancel if they actually changed block position (prevents jitter from head movement)
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if (!auth().isLoggedIn(p.getUniqueId())) {
            event.setCancelled(true);
            p.sendMessage("You must login first.");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if (auth().isLoggedIn(p.getUniqueId())) return;

        String msg = event.getMessage().toLowerCase(Locale.ROOT);

        // Allow only auth commands before login
        if (msg.startsWith("/login") || msg.startsWith("/register")) {
            return;
        }

        event.setCancelled(true);
        p.sendMessage("You must login first. Use /login or /register.");
    }

    private Location getDefaultSpawn(Player p) {
        if (!Bukkit.getWorlds().isEmpty()) {
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        return p.getLocation();
    }
                               }
