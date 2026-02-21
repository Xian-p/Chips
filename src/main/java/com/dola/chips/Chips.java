package com.dola.chips;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Chips extends JavaPlugin {

    private AuthManager authManager;

    // Used for PersistentDataContainer key in AuthManager
    public static final String NAMESPACED_KEY = "chips_auth";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        authManager = new AuthManager(this);

        // Events
        getServer().getPluginManager().registerEvents(new AuthListener(this), this);

        // Commands (one executor instance for all)
        AuthCommand authCommand = new AuthCommand(this);

        registerExecutor("register", authCommand);
        registerExecutor("login", authCommand);
        registerExecutor("changepassword", authCommand);
        registerExecutor("resetpassword", authCommand);

        getLogger().info("Chips Auth enabled!");
    }

    @Override
    public void onDisable() {
        if (authManager != null) {
            authManager.saveData();
        }
        getLogger().info("Chips Auth disabled!");
    }

    private void registerExecutor(String commandName, AuthCommand executor) {
        PluginCommand pc = getCommand(commandName);
        if (pc != null) {
            pc.setExecutor(executor);
        } else {
            getLogger().warning("Command '" + commandName + "' not found in plugin.yml");
        }
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
        }
