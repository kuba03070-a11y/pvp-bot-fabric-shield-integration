package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BotManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> bots = new HashSet<>();
    private static final Map<String, BotData> botDataMap = new HashMap<>();
    private static Path savePath;
    private static boolean initialized = false;
    
    // РЎС‚Р°С‚РёСЃС‚РёРєР°
    private static int botsSpawnedTotal = 0;
    private static int botsKilledTotal = 0;
    
    /**
     * Р”Р°РЅРЅС‹Рµ Р±РѕС‚Р° РґР»СЏ СЃРѕС…СЂР°РЅРµРЅРёСЏ
     */
    public static class BotData {
        public String name;
        public double x, y, z;
        public float yaw, pitch;
        public String dimension; // minecraft:overworld, minecraft:the_nether, minecraft:the_end
        public String gamemode; // survival, creative, adventure, spectator
        
        public BotData() {}
        
        public BotData(ServerPlayerEntity bot) {
            this.name = bot.getName().getString();
            this.x = bot.getX();
            this.y = bot.getY();
            this.z = bot.getZ();
            this.yaw = bot.getYaw();
            this.pitch = bot.getPitch();
            this.dimension = bot.getEntityWorld().getRegistryKey().getValue().toString();
            this.gamemode = bot.interactionManager.getGameMode().asString();
        }
    }
    
    /**
     * РљР»Р°СЃСЃ РґР»СЏ СЃРѕС…СЂР°РЅРµРЅРёСЏ СЃС‚Р°С‚РёСЃС‚РёРєРё
     */
    public static class StatsData {
        public int botsSpawnedTotal = 0;
        public int botsKilledTotal = 0;
    }

    /**
     * РРЅРёС†РёР°Р»РёР·Р°С†РёСЏ - Р·Р°РіСЂСѓР·РєР° СЃРѕС…СЂР°РЅС‘РЅРЅС‹С… Р±РѕС‚РѕРІ
     */
    public static void init(MinecraftServer server) {
        if (initialized) return;
        
        // РЎРѕР·РґР°С‘Рј РїР°РїРєСѓ config/pvpbot РµСЃР»Рё РЅРµ СЃСѓС‰РµСЃС‚РІСѓРµС‚
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to create config directory: " + e.getMessage());
        }
        
        savePath = configDir.resolve("bots.json");
        loadBots();
        loadStats();
        
        // Р РµСЃРїР°РІРЅРёРј СЃРѕС…СЂР°РЅС‘РЅРЅС‹С… Р±РѕС‚РѕРІ С‚РѕР»СЊРєРѕ РµСЃР»Рё РІРєР»СЋС‡РµРЅР° РЅР°СЃС‚СЂРѕР№РєР° botsRelogs
        BotSettings settings = BotSettings.get();
        if (settings.isBotsRelogs() && !botDataMap.isEmpty()) {
            System.out.println("[PVP_BOT] Restoring " + botDataMap.size() + " bots...");
            Map<String, BotData> botsToRestore = new HashMap<>(botDataMap);
            bots.clear();
            botDataMap.clear();
            
            // Р—Р°РїСѓСЃРєР°РµРј СЂРµСЃРїР°РІРЅ СЃ Р·Р°РґРµСЂР¶РєРѕР№
            server.execute(() -> restoreBotsDelayed(server, botsToRestore, 0));
        } else if (!settings.isBotsRelogs()) {
            // Р•СЃР»Рё СЂРµР»РѕРіРё РІС‹РєР»СЋС‡РµРЅС‹ - РѕС‡РёС‰Р°РµРј СЃРїРёСЃРѕРє
            bots.clear();
            botDataMap.clear();
            saveBots();
        }
        
        initialized = true;
    }
    
    private static void restoreBotsDelayed(MinecraftServer server, Map<String, BotData> botsToRestore, int index) {
        restoreBotsDelayedWithRetry(server, botsToRestore, index, 0);
    }
    
    private static void restoreBotsDelayedWithRetry(MinecraftServer server, Map<String, BotData> botsToRestore, int index, int retryCount) {
        final int MAX_RETRIES = 2; // РњР°РєСЃРёРјСѓРј 2 РїРѕРІС‚РѕСЂРЅС‹Рµ РїРѕРїС‹С‚РєРё (РІСЃРµРіРѕ 3 РїРѕРїС‹С‚РєРё)
        
        if (index >= botsToRestore.size()) {
            System.out.println("[PVP_BOT] Restored " + bots.size() + " bots");
            return;
        }
        
        String[] names = botsToRestore.keySet().toArray(new String[0]);
        if (index < names.length) {
            String name = names[index];
            BotData data = botsToRestore.get(name);
            
            // РџСЂРѕРІРµСЂСЏРµРј РЅРµ РѕРЅР»Р°Р№РЅ Р»Рё СѓР¶Рµ СЂРµР°Р»СЊРЅС‹Р№ РёРіСЂРѕРє СЃ С‚Р°РєРёРј РЅРёРєРѕРј
            ServerPlayerEntity existingPlayer = server.getPlayerManager().getPlayer(name);
            if (existingPlayer != null && !bots.contains(name)) {
                System.out.println("[PVP_BOT] Skipping bot '" + name + "': real player with this name is online");
                // РџРµСЂРµС…РѕРґРёРј Рє СЃР»РµРґСѓСЋС‰РµРјСѓ Р±РѕС‚Сѓ
                final int nextIndex = index + 1;
                server.execute(() -> restoreBotsDelayedWithRetry(server, botsToRestore, nextIndex, 0));
                return;
            }
            
            // РЎРїР°РІРЅРёРј Р±РѕС‚Р° СЃ РїРѕР·РёС†РёРµР№ Рё РёР·РјРµСЂРµРЅРёРµРј
            var dispatcher = server.getCommandManager().getDispatcher();
            boolean success = false;
            
            try {
                // Р¤РѕСЂРјР°С‚: player NAME spawn at X Y Z facing YAW PITCH in DIMENSION in GAMEMODE
                String command = String.format(java.util.Locale.US,
                    "player %s spawn at %.2f %.2f %.2f facing %.2f %.2f in %s in %s",
                    name, data.x, data.y, data.z, data.yaw, data.pitch, data.dimension, data.gamemode
                );
                if (retryCount == 0) {
                    System.out.println("[PVP_BOT] Restoring bot: " + name);
                } else {
                    System.out.println("[PVP_BOT] Retry #" + retryCount + " for bot: " + name);
                }
                dispatcher.execute(command, server.getCommandSource());
                bots.add(name);
                botDataMap.put(name, data);
                success = true;
                System.out.println("[PVP_BOT] вњ“ Successfully restored bot: " + name);
            } catch (Exception e) {
                // РџСЂРѕР±СѓРµРј СѓРїСЂРѕС‰С‘РЅРЅСѓСЋ РєРѕРјР°РЅРґСѓ
                try {
                    String simpleCommand = String.format(java.util.Locale.US,
                        "player %s spawn at %.2f %.2f %.2f",
                        name, data.x, data.y, data.z
                    );
                    dispatcher.execute(simpleCommand, server.getCommandSource());
                    bots.add(name);
                    botDataMap.put(name, data);
                    success = true;
                    System.out.println("[PVP_BOT] вњ“ Restored bot with simple command: " + name);
                } catch (Exception e2) {
                    if (retryCount < MAX_RETRIES) {
                        System.out.println("[PVP_BOT] вљ  Failed to restore bot '" + name + "', will retry... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                    } else {
                        System.out.println("[PVP_BOT] вњ— Failed to restore bot '" + name + "' after " + (MAX_RETRIES + 1) + " attempts: " + e2.getMessage());
                    }
                }
            }
            
            // Р•СЃР»Рё РЅРµ СѓРґР°Р»РѕСЃСЊ Рё РµСЃС‚СЊ РїРѕРїС‹С‚РєРё - РїРѕРІС‚РѕСЂСЏРµРј С‡РµСЂРµР· 20 С‚РёРєРѕРІ
            if (!success && retryCount < MAX_RETRIES) {
                final int currentRetry = retryCount + 1;
                server.execute(() -> {
                    final int[] delay = {0};
                    server.execute(new Runnable() {
                        @Override
                        public void run() {
                            delay[0]++;
                            if (delay[0] < 20) { // 20 С‚РёРєРѕРІ = 1 СЃРµРєСѓРЅРґР°
                                server.execute(this);
                            } else {
                                restoreBotsDelayedWithRetry(server, botsToRestore, index, currentRetry);
                            }
                        }
                    });
                });
            } else {
                // РџРµСЂРµС…РѕРґРёРј Рє СЃР»РµРґСѓСЋС‰РµРјСѓ Р±РѕС‚Сѓ С‡РµСЂРµР· 10 С‚РёРєРѕРІ
                final int nextIndex = index + 1;
                server.execute(() -> {
                    final int[] delay = {0};
                    server.execute(new Runnable() {
                        @Override
                        public void run() {
                            delay[0]++;
                            if (delay[0] < 10) {
                                server.execute(this);
                            } else {
                                restoreBotsDelayedWithRetry(server, botsToRestore, nextIndex, 0);
                            }
                        }
                    });
                });
            }
        }
    }
    
    /**
     * РћР±РЅРѕРІР»РµРЅРёРµ РґР°РЅРЅС‹С… РІСЃРµС… Р±РѕС‚РѕРІ РїРµСЂРµРґ СЃРѕС…СЂР°РЅРµРЅРёРµРј
     * РЎРѕС…СЂР°РЅСЏРµРј РґР°РЅРЅС‹Рµ С‚РѕР»СЊРєРѕ Р¶РёРІС‹С… Р±РѕС‚РѕРІ, РјС‘СЂС‚РІС‹Рµ СЃРѕС…СЂР°РЅСЏСЋС‚ РїРѕСЃР»РµРґРЅСЋСЋ РїРѕР·РёС†РёСЋ
     */
    public static void updateBotData(MinecraftServer server) {
        int updated = 0;
        int skipped = 0;
        int missing = 0;
        for (String name : bots) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);
            if (bot != null && bot.isAlive()) {
                // РћР±РЅРѕРІР»СЏРµРј РґР°РЅРЅС‹Рµ Р¶РёРІРѕРіРѕ Р±РѕС‚Р°
                botDataMap.put(name, new BotData(bot));
                updated++;
            } else if (!botDataMap.containsKey(name)) {
                // Р‘РѕС‚ РІ СЃРїРёСЃРєРµ РЅРѕ РЅРµС‚ РґР°РЅРЅС‹С… - СЌС‚Рѕ РїСЂРѕР±Р»РµРјР°!
                // РЈРґР°Р»СЏРµРј С‚Р°РєРѕРіРѕ Р±РѕС‚Р° РёР· СЃРїРёСЃРєР°
                missing++;
                System.out.println("[PVP_BOT] WARNING: Bot " + name + " in list but has no data!");
            } else {
                // Р‘РѕС‚ РЅРµ Р·Р°РіСЂСѓР¶РµРЅ РёР»Рё РјС‘СЂС‚РІ - СЃРѕС…СЂР°РЅСЏРµРј СЃС‚Р°СЂС‹Рµ РґР°РЅРЅС‹Рµ
                skipped++;
            }
        }
        System.out.println("[PVP_BOT] Updated bot data: " + updated + " updated, " + skipped + " skipped, " + missing + " missing, " + bots.size() + " total in list, " + botDataMap.size() + " in data map");
    }
    
    /**
     * РЎРѕС…СЂР°РЅРµРЅРёРµ СЃРїРёСЃРєР° Р±РѕС‚РѕРІ
     */
    public static void saveBots() {
        if (savePath == null) return;
        
        try (Writer writer = Files.newBufferedWriter(savePath)) {
            GSON.toJson(botDataMap, writer);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to save bots: " + e.getMessage());
        }
    }
    
    /**
     * Р—Р°РіСЂСѓР·РєР° СЃРїРёСЃРєР° Р±РѕС‚РѕРІ
     */
    private static void loadBots() {
        if (savePath == null || !Files.exists(savePath)) return;
        
        try (Reader reader = Files.newBufferedReader(savePath)) {
            Map<String, BotData> loaded = GSON.fromJson(reader, new TypeToken<Map<String, BotData>>(){}.getType());
            if (loaded != null) {
                botDataMap.putAll(loaded);
                bots.addAll(loaded.keySet());
            }
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to load bots: " + e.getMessage());
        }
    }
    
    /**
     * РЎР±СЂРѕСЃ РїСЂРё РІС‹С…РѕРґРµ РёР· РјРёСЂР°
     */
    public static void reset(MinecraftServer server) {
        updateBotData(server);
        saveBots();
        initialized = false;
    }

    public static boolean spawnBot(MinecraftServer server, String name, ServerCommandSource source) {
        // РџСЂРѕРІРµСЂСЏРµРј, СЃСѓС‰РµСЃС‚РІСѓРµС‚ Р»Рё СѓР¶Рµ РёРіСЂРѕРє СЃ С‚Р°РєРёРј РёРјРµРЅРµРј РЅР° СЃРµСЂРІРµСЂРµ
        ServerPlayerEntity existingPlayer = server.getPlayerManager().getPlayer(name);
        if (existingPlayer != null && existingPlayer.isAlive()) {
            // Р‘РѕС‚ СѓР¶Рµ СЃСѓС‰РµСЃС‚РІСѓРµС‚ Рё Р¶РёРІ
            if (!bots.contains(name)) {
                bots.add(name); // Р”РѕР±Р°РІР»СЏРµРј РІ СЃРїРёСЃРѕРє РµСЃР»Рё РЅРµ Р±С‹Р»Рѕ
                botDataMap.put(name, new BotData(existingPlayer));
                saveBots();
                System.out.println("[PVP_BOT] Added existing bot to list: " + name);
            }
            return false;
        }

        // Execute Carpet's /player command - spawn in survival mode
        var dispatcher = server.getCommandManager().getDispatcher();
        try {
            // Spawn bot in survival mode using Carpet syntax
            dispatcher.execute("player " + name + " spawn in survival", source);
        } catch (Exception e) {
            // Try alternative method if first fails
            try {
                dispatcher.execute("player " + name + " spawn", source);
                // Force gamemode change after spawn
                dispatcher.execute("gamemode survival " + name, server.getCommandSource());
            } catch (Exception e2) {
                // Р”Р°Р¶Рµ РµСЃР»Рё РєРѕРјР°РЅРґР° РІС‹Р±СЂРѕСЃРёР»Р° РёСЃРєР»СЋС‡РµРЅРёРµ, РїСЂРѕРІРµСЂРёРј РїРѕСЏРІРёР»СЃСЏ Р»Рё Р±РѕС‚
            }
        }
        
        // РџСЂРѕРІРµСЂСЏРµРј РїРѕСЏРІРёР»СЃСЏ Р»Рё Р±РѕС‚ РЅР° СЃРµСЂРІРµСЂРµ (РЅРµР·Р°РІРёСЃРёРјРѕ РѕС‚ СЂРµР·СѓР»СЊС‚Р°С‚Р° РєРѕРјР°РЅРґС‹)
        // Р”Р°С‘Рј РЅРµР±РѕР»СЊС€СѓСЋ Р·Р°РґРµСЂР¶РєСѓ С‡РµСЂРµР· execute
        server.execute(() -> {
            ServerPlayerEntity newBot = server.getPlayerManager().getPlayer(name);
            if (newBot != null && !bots.contains(name)) {
                bots.add(name);
                botDataMap.put(name, new BotData(newBot));
                incrementBotsSpawned(); // РЈРІРµР»РёС‡РёРІР°РµРј СЃС‡РµС‚С‡РёРє
                saveBots();
                System.out.println("[PVP_BOT] Added bot to list (delayed): " + name);
            } else if (newBot != null && bots.contains(name)) {
                // Р‘РѕС‚ СѓР¶Рµ РІ СЃРїРёСЃРєРµ, РЅРѕ РѕР±РЅРѕРІРёРј РґР°РЅРЅС‹Рµ
                botDataMap.put(name, new BotData(newBot));
                saveBots();
                System.out.println("[PVP_BOT] Updated bot data (delayed): " + name);
            }
        });
        
        // Р”РѕР±Р°РІР»СЏРµРј РІ СЃРїРёСЃРѕРє СЃСЂР°Р·Сѓ (РЅР° СЃР»СѓС‡Р°Р№ РµСЃР»Рё Р±РѕС‚ СѓР¶Рµ РїРѕСЏРІРёР»СЃСЏ)
        ServerPlayerEntity newBot = server.getPlayerManager().getPlayer(name);
        if (newBot != null) {
            if (!bots.contains(name)) {
                bots.add(name);
                botDataMap.put(name, new BotData(newBot));
                incrementBotsSpawned(); // РЈРІРµР»РёС‡РёРІР°РµРј СЃС‡РµС‚С‡РёРє
                saveBots();
                System.out.println("[PVP_BOT] Added bot to list (immediate): " + name);
            }
            return true;
        }
        
        // Р‘РѕС‚ РµС‰С‘ РЅРµ РїРѕСЏРІРёР»СЃСЏ, РґРѕР±Р°РІРёРј РёРјСЏ РІ СЃРїРёСЃРѕРє СЃ РґРµС„РѕР»С‚РЅС‹РјРё РґР°РЅРЅС‹РјРё
        if (!bots.contains(name)) {
            bots.add(name);
            // РЎРѕР·РґР°С‘Рј РґРµС„РѕР»С‚РЅС‹Рµ РґР°РЅРЅС‹Рµ (РїРѕР·РёС†РёСЏ РёРіСЂРѕРєР° РєРѕС‚РѕСЂС‹Р№ СЃРїР°РІРЅРёС‚)
            BotData defaultData = new BotData();
            defaultData.name = name;
            defaultData.x = source.getPosition().x;
            defaultData.y = source.getPosition().y;
            defaultData.z = source.getPosition().z;
            defaultData.yaw = source.getRotation().y;
            defaultData.pitch = source.getRotation().x;
            defaultData.dimension = source.getWorld().getRegistryKey().getValue().toString();
            defaultData.gamemode = "survival";
            botDataMap.put(name, defaultData);
            incrementBotsSpawned(); // РЈРІРµР»РёС‡РёРІР°РµРј СЃС‡РµС‚С‡РёРє
            saveBots();
            System.out.println("[PVP_BOT] Added bot to list (default data): " + name);
        }
        
        return true;
    }

    public static boolean removeBot(MinecraftServer server, String name, ServerCommandSource source) {
        // РЈРґР°Р»СЏРµРј РёР· СЃРїРёСЃРєР° РІ Р»СЋР±РѕРј СЃР»СѓС‡Р°Рµ
        boolean wasInList = bots.remove(name);
        botDataMap.remove(name); // РЈРґР°Р»СЏРµРј РґР°РЅРЅС‹Рµ Р±РѕС‚Р°
        saveBots();
        
        // РћС‡РёС‰Р°РµРј РІСЃРµ СЃРѕСЃС‚РѕСЏРЅРёСЏ Р±РѕС‚Р°
        BotCombat.removeState(name);
        BotUtils.removeState(name);
        BotNavigation.resetIdle(name);

        String command = "player " + name + " kill";
        var dispatcher = server.getCommandManager().getDispatcher();
        try {
            dispatcher.execute(command, source);
        } catch (Exception e) {
            // Ignore
        }
        
        // РЈР±СЂР°Р»Рё РјРіРЅРѕРІРµРЅРЅСѓСЋ РѕС‚РїСЂР°РІРєСѓ - СЃС‚Р°С‚РёСЃС‚РёРєР° РѕС‚РїСЂР°РІРёС‚СЃСЏ С‡РµСЂРµР· 30 СЃРµРєСѓРЅРґ
        
        return wasInList;
    }

    public static ServerPlayerEntity getBot(MinecraftServer server, String name) {
        return server.getPlayerManager().getPlayer(name);
    }

    public static void removeAllBots(MinecraftServer server, ServerCommandSource source) {
        var dispatcher = server.getCommandManager().getDispatcher();
        for (String name : new HashSet<>(bots)) {
            // РћС‡РёС‰Р°РµРј РІСЃРµ СЃРѕСЃС‚РѕСЏРЅРёСЏ Р±РѕС‚Р°
            BotCombat.removeState(name);
            BotUtils.removeState(name);
            BotNavigation.resetIdle(name);
            
            String command = "player " + name + " kill";
            try {
                dispatcher.execute(command, source);
            } catch (Exception e) {
                // Ignore
            }
        }
        bots.clear();
        botDataMap.clear(); // РћС‡РёС‰Р°РµРј РґР°РЅРЅС‹Рµ РІСЃРµС… Р±РѕС‚РѕРІ
        saveBots();
        
        // РЈР±СЂР°Р»Рё РјРіРЅРѕРІРµРЅРЅСѓСЋ РѕС‚РїСЂР°РІРєСѓ - СЃС‚Р°С‚РёСЃС‚РёРєР° РѕС‚РїСЂР°РІРёС‚СЃСЏ С‡РµСЂРµР· 30 СЃРµРєСѓРЅРґ
    }

    public static int getBotCount() {
        return bots.size();
    }

    public static Set<String> getAllBots() {
        return new HashSet<>(bots);
    }
    
    /**
     * РџСЂРѕРІРµСЂСЏРµС‚ Р¶РёРІ Р»Рё Р±РѕС‚ - СѓРґР°Р»СЏРµС‚ РјС‘СЂС‚РІС‹С… РёР· СЃРїРёСЃРєР°
     * РќР• СѓРґР°Р»СЏРµС‚ Р±РѕС‚РѕРІ РєРѕС‚РѕСЂС‹Рµ РїСЂРѕСЃС‚Рѕ РЅРµ Р·Р°РіСЂСѓР¶РµРЅС‹ (bot == null)
     */
    public static void cleanupDeadBots(MinecraftServer server) {
        boolean changed = false;
        for (String name : new HashSet<>(bots)) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);
            
            // Р•СЃР»Рё Р±РѕС‚ РЅРµ РЅР°Р№РґРµРЅ (bot == null) - РќР• СѓРґР°Р»СЏРµРј РµРіРѕ!
            // РћРЅ РјРѕР¶РµС‚ Р±С‹С‚СЊ РїСЂРѕСЃС‚Рѕ РІС‹РіСЂСѓР¶РµРЅ РёР· РїР°РјСЏС‚Рё (РґР°Р»РµРєРѕ РѕС‚ РёРіСЂРѕРєР° РІ СЃРёРЅРіР»РїР»РµРµСЂРµ)
            if (bot == null) {
                continue; // РџСЂРѕРїСѓСЃРєР°РµРј, РЅРµ СѓРґР°Р»СЏРµРј
            }
            
            // РЈРґР°Р»СЏРµРј С‚РѕР»СЊРєРѕ РµСЃР»Рё Р±РѕС‚ СЃСѓС‰РµСЃС‚РІСѓРµС‚ РќРћ РјС‘СЂС‚РІ
            boolean isDead = !bot.isAlive() || bot.getHealth() <= 0 || bot.isDead();
            if (isDead) {
                // РЈРґР°Р»СЏРµРј РјС‘СЂС‚РІРѕРіРѕ Р±РѕС‚Р° РёР· СЃРїРёСЃРєР°
                bots.remove(name);
                botDataMap.remove(name);
                BotCombat.removeState(name);
                BotUtils.removeState(name);
                BotNavigation.resetIdle(name);
                incrementBotsKilled(); // РЈРІРµР»РёС‡РёРІР°РµРј СЃС‡РµС‚С‡РёРє СѓР±РёС‚С‹С…
                changed = true;
                System.out.println("[PVP_BOT] Removed dead bot: " + name);
            }
        }
        if (changed) {
            saveBots();
        }
    }
    
    /**
     * РЎРёРЅС…СЂРѕРЅРёР·РёСЂСѓРµС‚ СЃРїРёСЃРѕРє Р±РѕС‚РѕРІ СЃ СЂРµР°Р»СЊРЅС‹РјРё Carpet Р±РѕС‚Р°РјРё РЅР° СЃРµСЂРІРµСЂРµ
     * Р”РѕР±Р°РІР»СЏРµС‚ Р±РѕС‚РѕРІ РєРѕС‚РѕСЂС‹Рµ РµСЃС‚СЊ РЅР° СЃРµСЂРІРµСЂРµ РЅРѕ РЅРµС‚ РІ СЃРїРёСЃРєРµ
     */
    public static void syncBots(MinecraftServer server) {
        boolean changed = false;
        // РџСЂРѕРІРµСЂСЏРµРј РІСЃРµС… РёРіСЂРѕРєРѕРІ РЅР° СЃРµСЂРІРµСЂРµ
        for (var player : server.getPlayerManager().getPlayerList()) {
            String name = player.getName().getString();
            
            // РџСЂРѕРїСѓСЃРєР°РµРј РµСЃР»Рё СѓР¶Рµ РІ СЃРїРёСЃРєРµ
            if (bots.contains(name)) continue;
            
            // Carpet Р±РѕС‚С‹ РёРјРµСЋС‚ РєР»Р°СЃСЃ carpet.patches.EntityPlayerMPFake
            String className = player.getClass().getName();
            boolean isFakePlayer = className.contains("EntityPlayerMPFake") || 
                                   className.contains("FakePlayer") ||
                                   className.contains("fake") ||
                                   className.contains("Fake");
            
            if (isFakePlayer) {
                bots.add(name);
                botDataMap.put(name, new BotData(player));
                changed = true;
                System.out.println("[PVP_BOT] Synced Carpet bot: " + name);
            }
        }
        if (changed) {
            saveBots();
        }
    }
    
    /**
     * РЎРёРЅС…СЂРѕРЅРёР·РёСЂСѓРµС‚ РєРѕРЅРєСЂРµС‚РЅРѕРіРѕ Р±РѕС‚Р° РїРѕ РёРјРµРЅРё
     * Р”РѕР±Р°РІР»СЏРµС‚ Р±РѕС‚Р° РІ СЃРїРёСЃРѕРє РµСЃР»Рё РѕРЅ fake player Рё РµРіРѕ РЅРµС‚ РІ СЃРїРёСЃРєРµ
     */
    public static boolean syncBot(MinecraftServer server, String name) {
        // РџСЂРѕРІРµСЂСЏРµРј РµСЃР»Рё СѓР¶Рµ РІ СЃРїРёСЃРєРµ
        if (bots.contains(name)) {
            return false;
        }
        
        // РС‰РµРј РёРіСЂРѕРєР° РЅР° СЃРµСЂРІРµСЂРµ
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(name);
        if (player == null) {
            return false;
        }
        
        // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ СЌС‚Рѕ fake player
        String className = player.getClass().getName();
        boolean isFakePlayer = className.contains("EntityPlayerMPFake") || 
                               className.contains("FakePlayer") ||
                               className.contains("fake") ||
                               className.contains("Fake");
        
        if (isFakePlayer) {
            bots.add(name);
            botDataMap.put(name, new BotData(player));
            incrementBotsSpawned();
            saveBots();
            System.out.println("[PVP_BOT] Synced bot: " + name);
            return true;
        }
        
        return false;
    }
    
    /**
     * РЈРІРµР»РёС‡РёРІР°РµС‚ СЃС‡РµС‚С‡РёРє Р·Р°СЃРїР°РІРЅРµРЅРЅС‹С… Р±РѕС‚РѕРІ
     */
    public static void incrementBotsSpawned() {
        botsSpawnedTotal++;
        saveStats();
        // РЈР±СЂР°Р»Рё РјРіРЅРѕРІРµРЅРЅСѓСЋ РѕС‚РїСЂР°РІРєСѓ - СЃС‚Р°С‚РёСЃС‚РёРєР° РѕС‚РїСЂР°РІРёС‚СЃСЏ С‡РµСЂРµР· 30 СЃРµРєСѓРЅРґ
    }
    
    /**
     * РЈРІРµР»РёС‡РёРІР°РµС‚ СЃС‡РµС‚С‡РёРє СѓР±РёС‚С‹С… Р±РѕС‚РѕРІ
     */
    public static void incrementBotsKilled() {
        botsKilledTotal++;
        saveStats();
        // РЈР±СЂР°Р»Рё РјРіРЅРѕРІРµРЅРЅСѓСЋ РѕС‚РїСЂР°РІРєСѓ - СЃС‚Р°С‚РёСЃС‚РёРєР° РѕС‚РїСЂР°РІРёС‚СЃСЏ С‡РµСЂРµР· 30 СЃРµРєСѓРЅРґ
    }
    
    /**
     * Р’РѕР·РІСЂР°С‰Р°РµС‚ РѕР±С‰РµРµ РєРѕР»РёС‡РµСЃС‚РІРѕ Р·Р°СЃРїР°РІРЅРµРЅРЅС‹С… Р±РѕС‚РѕРІ
     */
    public static int getBotsSpawnedTotal() {
        return botsSpawnedTotal;
    }
    
    /**
     * Р’РѕР·РІСЂР°С‰Р°РµС‚ РѕР±С‰РµРµ РєРѕР»РёС‡РµСЃС‚РІРѕ СѓР±РёС‚С‹С… Р±РѕС‚РѕРІ
     */
    public static int getBotsKilledTotal() {
        return botsKilledTotal;
    }
    
    /**
     * РЎРѕС…СЂР°РЅРµРЅРёРµ СЃС‚Р°С‚РёСЃС‚РёРєРё
     */
    private static void saveStats() {
        if (savePath == null) return;
        
        Path statsPath = savePath.getParent().resolve("stats.json");
        try (Writer writer = Files.newBufferedWriter(statsPath)) {
            StatsData stats = new StatsData();
            stats.botsSpawnedTotal = botsSpawnedTotal;
            stats.botsKilledTotal = botsKilledTotal;
            GSON.toJson(stats, writer);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to save stats: " + e.getMessage());
        }
    }
    
    /**
     * Р—Р°РіСЂСѓР·РєР° СЃС‚Р°С‚РёСЃС‚РёРєРё
     */
    private static void loadStats() {
        if (savePath == null) return;
        
        Path statsPath = savePath.getParent().resolve("stats.json");
        if (!Files.exists(statsPath)) return;
        
        try (Reader reader = Files.newBufferedReader(statsPath)) {
            StatsData stats = GSON.fromJson(reader, StatsData.class);
            if (stats != null) {
                botsSpawnedTotal = stats.botsSpawnedTotal;
                botsKilledTotal = stats.botsKilledTotal;
            }
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to load stats: " + e.getMessage());
        }
    }
}
