package com.dola.chips;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class AuthListener implements Listener {
    private final Chips plugin;

    public AuthListener(Chips plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (plugin.getAuthManager().isRegistered(p.getUniqueId())) {
            p.sendMessage(plugin.getConfig().getString("messages.not-logged-in").replace('&', '§'));
        } else {
            p.sendMessage(plugin.getConfig().getString("messages.not-registered").replace('&', '§'));
        }
        plugin.getAuthManager().setLoggedIn(p.getUniqueId(), false);
        p.teleport(plugin.getServer().getWorldSpawnLocation());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (plugin.getConfig().getBoolean("block-movement") && !plugin.getAuthManager().isLoggedIn(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (plugin.getConfig().getBoolean("block-chat") && !plugin.getAuthManager().isLoggedIn(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(plugin.getConfig().getString("messages.not-logged-in").replace('&', '§'));
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage().toLowerCase();
        if (!cmd.startsWith("/register") && !cmd.startsWith("/login") && !cmd.startsWith("/changepassword")) {
            if (plugin.getConfig().getBoolean("block-commands") && !plugin.getAuthManager().isLoggedIn(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(plugin.getConfig().getString("messages.not-logged-in").replace('&', '§'));
            }
        }
    }
                                                                                                   }
                           
