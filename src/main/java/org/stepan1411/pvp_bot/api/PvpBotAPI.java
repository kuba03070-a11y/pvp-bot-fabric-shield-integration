package org.stepan1411.pvp_bot.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotSettings;
import org.stepan1411.pvp_bot.api.event.BotEventManager;

import java.util.Set;

/**
 * Main API class for PVP Bot Fabric mod
 * Use this class to interact with bots from your addon
 * 
 * @author Stepan1411
 * @version 1.0.0
 */
public class PvpBotAPI {
    
    private static final String API_VERSION = "1.0.0";
    
    /**
     * Get the API version
     * @return API version string
     */
    public static String getApiVersion() {
        return API_VERSION;
    }
    
    /**
     * Get all bot names currently active on the server
     * @return Set of bot names
     */
    public static Set<String> getAllBots() {
        return BotManager.getAllBots();
    }
    
    /**
     * Get bot entity by name
     * @param server Minecraft server instance
     * @param name Bot name
     * @return ServerPlayerEntity or null if not found
     */
    public static ServerPlayerEntity getBot(MinecraftServer server, String name) {
        return BotManager.getBot(server, name);
    }
    
    /**
     * Check if player is a bot
     * @param playerName Player name to check
     * @return true if player is a bot
     */
    public static boolean isBot(String playerName) {
        return BotManager.getAllBots().contains(playerName);
    }
    
    /**
     * Get current bot count
     * @return Number of active bots
     */
    public static int getBotCount() {
        return BotManager.getBotCount();
    }
    
    /**
     * Get bot settings instance
     * @return BotSettings singleton
     */
    public static BotSettings getBotSettings() {
        return BotSettings.get();
    }
    
    /**
     * Get event manager for registering custom handlers
     * @return BotEventManager instance
     */
    public static BotEventManager getEventManager() {
        return BotEventManager.getInstance();
    }
    
    /**
     * Get total bots spawned (statistics)
     * @return Total spawned bots count
     */
    public static int getTotalBotsSpawned() {
        return BotManager.getBotsSpawnedTotal();
    }
    
    /**
     * Get total bots killed (statistics)
     * @return Total killed bots count
     */
    public static int getTotalBotsKilled() {
        return BotManager.getBotsKilledTotal();
    }
}
