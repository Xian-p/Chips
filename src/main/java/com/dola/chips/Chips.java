package me.chips;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    
    private final HashMap<UUID, Boolean> loggedInPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        createUserDataFile();
        
        // We only need the Executor for logout. 
        // Login/Register are handled in AuthListener to hide them from console logs.
        getCommand("logout").setExecutor(new AuthCommands(this));
        
        // Register Security & Command Interceptor
        getServer().getPluginManager().registerEvents(new AuthListener(this), this);

        // Start the 10-second reminder task
        startReminderTask();

        getLogger().info("Chips Auth loaded for Purpur 1.21.11");
    }

    @Override
    public void onDisable() {
        saveUserData();
        // Clean up effects
        for (Player p : getServer().getOnlinePlayers()) {
            removeBlindness(p);
        }
    }

    // --- 10 Second Reminder Task ---
    private void startReminderTask() {
        // Run every 200 ticks (10 seconds)
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                if (!isLoggedIn(player.getUniqueId())) {
                    if (isRegistered(player.getUniqueId())) {
                        player.sendMessage(Component.text("Use /login <password>", NamedTextColor.RED));
                    } else {
                        player.sendMessage(Component.text("Use /register <password> <password>", NamedTextColor.RED));
                    }
                }
            }
        }, 0L, 200L); 
    }

    // --- Effect Management ---
    public void applyBlindness(Player player) {
        // Duration: Infinite, Amplifier: 1, Particles: Hidden
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 1, false, false));
    }

    public void removeBlindness(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
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
