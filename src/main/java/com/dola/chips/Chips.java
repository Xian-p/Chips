package me.chips;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.UUID;

public class Chips extends JavaPlugin {

    private static Chips instance;
    private File userDataFile;
    private FileConfiguration userData;
    
    // Stores login status in RAM
    private final HashMap<UUID, Boolean> loggedInPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        
        // 1. Create the 'Chips' directory and data.yml
        createUserDataFile();
        
        // 2. Register Commands
        getCommand("register").setExecutor(new AuthCommands(this));
        getCommand("login").setExecutor(new AuthCommands(this));
        getCommand("logout").setExecutor(new AuthCommands(this));
        getCommand("changepassword").setExecutor(new AuthCommands(this));
        
        // 3. Register Security Listeners
        getServer().getPluginManager().registerEvents(new AuthListener(this), this);

        getLogger().info("Chips Auth loaded for Purpur 1.21.11");
    }

    @Override
    public void onDisable() {
        saveUserData();
    }

    // --- Session API ---
    public boolean isLoggedIn(UUID uuid) {
        return loggedInPlayers.getOrDefault(uuid, false);
    }

    public void setLoggedIn(UUID uuid, boolean status) {
        loggedInPlayers.put(uuid, status);
    }
    
    public void removeSession(UUID uuid) {
        loggedInPlayers.remove(uuid);
    }

    // --- Auth Database API ---
    public boolean isRegistered(UUID uuid) {
        return getUserData().contains(uuid.toString() + ".password");
    }

    public void registerUser(UUID uuid, String password) {
        getUserData().set(uuid.toString() + ".password", hashPassword(password));
        saveUserData();
    }

    public boolean checkPassword(UUID uuid, String password) {
        String storedHash = getUserData().getString(uuid.toString() + ".password");
        return storedHash != null && storedHash.equals(hashPassword(password));
    }
    
    public void changePassword(UUID uuid, String newPassword) {
        getUserData().set(uuid.toString() + ".password", hashPassword(newPassword));
        saveUserData();
    }

    // --- File System ---
    private void createUserDataFile() {
        // This ensures the folder is plugins/Chips
        userDataFile = new File(getDataFolder(), "data.yml");
        if (!userDataFile.exists()) {
            userDataFile.getParentFile().mkdirs();
            try {
                userDataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create data.yml!");
                e.printStackTrace();
            }
        }
        userData = YamlConfiguration.loadConfiguration(userDataFile);
    }

    public FileConfiguration getUserData() {
        return userData;
    }

    public void saveUserData() {
        try {
            userData.save(userDataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save data.yml!");
            e.printStackTrace();
        }
    }

    // --- SHA-256 Hashing ---
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
