package com.dola.chips;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class AuthManager {
    private final Chips plugin;
    private final File dataFile;
    private final HashMap<UUID, String> playerPasswords = new HashMap<>();
    private final HashMap<UUID, Boolean> loggedInPlayers = new HashMap<>();
    private final int minPasswordLength;

    public AuthManager(Chips plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        this.minPasswordLength = plugin.getConfig().getInt("password-min-length", 8);
        loadData();
    }

    public String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public boolean validatePassword(String hash, String password) {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
    }

    public void saveToPDC(Player p, String hash) {
        p.getPersistentDataContainer().set(
                plugin.getServer().getNamespacedKey(Chips.NAMESPACED_KEY),
                PersistentDataType.STRING,
                hash
        );
    }

    private void loadData() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            playerPasswords.put(uuid, config.getString(uuidStr));
        }
    }

    public void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        playerPasswords.forEach((uuid, hash) -> config.set(uuid.toString(), hash));
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }

    public boolean isRegistered(UUID uuid) {
        return playerPasswords.containsKey(uuid);
    }

    public boolean registerPlayer(UUID uuid, String password, String confirmPassword) {
        if (isRegistered(uuid)) return false;
        if (!password.equals(confirmPassword)) return false;
        if (password.length() < minPasswordLength) return false;

        playerPasswords.put(uuid, hashPassword(password));
        saveData();
        return true;
    }

    public boolean validateLogin(UUID uuid, String password) {
        if (!isRegistered(uuid)) return false;
        return validatePassword(playerPasswords.get(uuid), password);
    }

    public boolean changePassword(UUID uuid, String oldPassword, String newPassword, String confirmNewPassword) {
        if (!isRegistered(uuid)) return false;
        if (!validatePassword(playerPasswords.get(uuid), oldPassword)) return false;
        if (!newPassword.equals(confirmNewPassword)) return false;
        if (newPassword.length() < minPasswordLength) return false;

        playerPasswords.put(uuid, hashPassword(newPassword));
        saveData();
        return true;
    }

    public boolean resetPassword(UUID uuid, String newPassword) {
        if (!isRegistered(uuid)) return false;
        if (newPassword.length() < minPasswordLength) return false;

        playerPasswords.put(uuid, hashPassword(newPassword));
        saveData();
        return true;
    }

    public void setLoggedIn(UUID uuid, boolean loggedIn) {
        loggedInPlayers.put(uuid, loggedIn);
    }

    public boolean isLoggedIn(UUID uuid) {
        return loggedInPlayers.getOrDefault(uuid, false);
    }

    public int getMinPasswordLength() {
        return minPasswordLength;
    }
          }
          
