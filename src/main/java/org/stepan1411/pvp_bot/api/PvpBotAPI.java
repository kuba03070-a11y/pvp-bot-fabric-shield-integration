package org.stepan1411.pvp_bot.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotSettings;
import org.stepan1411.pvp_bot.api.event.BotEventManager;

import java.util.Set;


public class PvpBotAPI {
    
    private static final String API_VERSION = "1.0.0";
    
    
    public static String getApiVersion() {
        return API_VERSION;
    }
    
    
    public static Set<String> getAllBots() {
        return BotManager.getAllBots();
    }
    
    
    public static ServerPlayerEntity getBot(MinecraftServer server, String name) {
        return BotManager.getBot(server, name);
    }
    
    
    public static boolean isBot(String playerName) {
        return BotManager.getAllBots().contains(playerName);
    }
    
    
    public static int getBotCount() {
        return BotManager.getBotCount();
    }
    
    
    public static BotSettings getBotSettings() {
        return BotSettings.get();
    }
    
    
    public static BotEventManager getEventManager() {
        return BotEventManager.getInstance();
    }
    
    
    public static int getTotalBotsSpawned() {
        return BotManager.getBotsSpawnedTotal();
    }
    
    
    public static int getTotalBotsKilled() {
        return BotManager.getBotsKilledTotal();
    }
}
