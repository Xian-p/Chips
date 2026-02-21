package com.dola.chips;

import org.bukkit.plugin.java.JavaPlugin;

public final class Chips extends JavaPlugin {
    private AuthManager authManager;
    public static final String NAMESPACED_KEY = "chips_auth";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        authManager = new AuthManager(this);
        
        getServer().getPluginManager().registerEvents(new AuthListener(this), this);
        getCommand("register").setExecutor(new AuthCommand(this));
        getCommand("login").setExecutor(new AuthCommand(this));
        getCommand("changepassword").setExecutor(new AuthCommand(this));
        getCommand("resetpassword").setExecutor(new AuthCommand(this));
        
        getLogger().info("Chips Auth enabled!");
    }

    @Override
    public void onDisable() {
        authManager.saveData();
        getLogger().info("Chips Auth disabled!");
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
  }

