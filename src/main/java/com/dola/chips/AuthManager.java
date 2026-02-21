package com.dola.chips;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthManager {

    private final Chips plugin;
    private final File dataFile;
    private YamlConfiguration data;

    private final int minPasswordLength;

    private final NamespacedKey passwordKey;

    private final Map<UUID, Boolean> loggedIn = new ConcurrentHashMap<>();

    public AuthManager(Chips plugin) {
        this.plugin = plugin;

        this.minPasswordLength = plugin.getConfig().getInt("password-min-length", 8);

        if (!plugin.getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdirs();
        }

        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);

        this.passwordKey = new NamespacedKey(plugin, Chips.NAMESPACED_KEY);
    }

    public int getMinPasswordLength() {
        return minPasswordLength;
    }

    public boolean isLoggedIn(UUID uuid) {
        return loggedIn.getOrDefault(uuid, false);
    }

    public void setLoggedIn(UUID uuid, boolean value) {
        loggedIn.put(uuid, value);
    }

    public boolean isRegistered(UUID uuid) {
        return data.contains(path(uuid) + ".pw");
    }

    public boolean registerPlayer(UUID uuid, String password, String confirmPassword) {
        if (isRegistered(uuid)) return false;
        if (password == null || confirmPassword == null) return false;
        if (!password.equals(confirmPassword)) return false;
        if (password.length() < minPasswordLength) return false;

        String stored = makeStoredPassword(password);
        data.set(path(uuid) + ".pw", stored);
        saveData();
        return true;
    }

    public boolean validateLogin(UUID uuid, String password) {
        if (!isRegistered(uuid)) return false;
        String stored = data.getString(path(uuid) + ".pw");
        return verifyPassword(password, stored);
    }

    public boolean changePassword(UUID uuid, String oldPassword, String newPassword, String confirmNewPassword) {
        if (!isRegistered(uuid)) return false;
        if (!verifyPassword(oldPassword, data.getString(path(uuid) + ".pw"))) return false;

        if (newPassword == null || confirmNewPassword == null) return false;
        if (!newPassword.equals(confirmNewPassword)) return false;
        if (newPassword.length() < minPasswordLength) return false;

        data.set(path(uuid) + ".pw", makeStoredPassword(newPassword));
        saveData();
        return true;
    }

    public boolean resetPassword(UUID uuid, String newPassword) {
        if (!isRegistered(uuid)) return false;
        if (newPassword == null) return false;
        if (newPassword.length() < minPasswordLength) return false;

        data.set(path(uuid) + ".pw", makeStoredPassword(newPassword));
        saveData();
        loggedIn.put(uuid, false);
        return true;
    }

    public void savePDC(Player p, String storedPasswordValue) {
        // Optional: stores the value on the player too (not required for file auth)
        p.getPersistentDataContainer().set(passwordKey, PersistentDataType.STRING, storedPasswordValue);
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save players.yml: " + e.getMessage());
        }
    }

    private String path(UUID uuid) {
        return "players." + uuid;
    }

    // Stored format: base64(salt) + ":" + base64(sha256(salt + password))
    private String makeStoredPassword(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        byte[] digest = sha256(concat(salt, password.getBytes(StandardCharsets.UTF_8)));

        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String digB64 = Base64.getEncoder().encodeToString(digest);
        return saltB64 + ":" + digB64;
    }

    private boolean verifyPassword(String password, String stored) {
        if (password == null || stored == null) return false;
        String[] parts = stored.split(":", 2);
        if (parts.length != 2) return false;

        byte[] salt;
        byte[] expected;
        try {
            salt = Base64.getDecoder().decode(parts[0]);
            expected = Base64.getDecoder().decode(parts[1]);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        byte[] actual = sha256(concat(salt, password.getBytes(StandardCharsets.UTF_8)));
        return MessageDigest.isEqual(expected, actual);
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
        }
