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
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Chips extends JavaPlugin {

    private static Chips instance;
    private File userDataFile;
    private FileConfiguration userData;
    
    private final HashMap<UUID, Boolean> loggedInPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        
        // --- 1. SETUP CONSOLE FILTER (Hides passwords) ---
        Logger.getLogger("").setFilter(new PasswordFilter());

        // --- 2. SETUP FILES ---
        createUserDataFile();
        
        // --- 3. REGISTER COMMANDS ---
        // We only register logout here. Login/Register are handled in Listener to be safe.
        getCommand("logout").setExecutor(new AuthCommands(this));
        
        // --- 4. REGISTER EVENTS ---
        getServer().getPluginManager().registerEvents(new AuthListener(this), this);

        // --- 5. TASKS ---
        startReminderTask();

        getLogger().info("Chips Auth loaded. Passwords are now hidden from console.");
    }

    @Override
    public void onDisable() {
        saveUserData();
        // Remove effects
        for (Player p : getServer().getOnlinePlayers()) {
            removeBlindness(p);
        }
    }

    // --- 10 Second Reminder Task ---
    private void startReminderTask() {
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
    
    // --- CONSOLE FILTER CLASS ---
    // This blocks the "issued server command" line for auth commands
    public static class PasswordFilter implements Filter {
        @Override
        public boolean isLoggable(LogRecord record) {
            String message = record.getMessage();
            if (message == null) return true;
            
            // Convert to lower case for checking
            String lower = message.toLowerCase();
            
            // If it's a command log
            if (lower.contains("issued server command")) {
                // If it contains our sensitive commands, BLOCK IT (return false)
                if (lower.contains("/login") || lower.contains("/register") || lower.contains("/changepassword")) {
                    return false;
                }
            }
            return true;
        }
    }
}
