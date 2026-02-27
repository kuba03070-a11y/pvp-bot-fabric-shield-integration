package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.DataResult;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BotKits {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static MinecraftServer currentServer;
    
    // РҐСЂР°РЅРµРЅРёРµ РєРёС‚РѕРІ: РёРјСЏ РєРёС‚Р° -> СЃРїРёСЃРѕРє РїСЂРµРґРјРµС‚РѕРІ (СЃР»РѕС‚ -> NBT РєР°Рє Map)
    private static final Map<String, Map<String, Object>> kitsRaw = new HashMap<>();
    
    /**
     * РРЅРёС†РёР°Р»РёР·Р°С†РёСЏ
     */
    public static void init(MinecraftServer srv) {
        currentServer = srv;
        
        // РЎРѕР·РґР°С‘Рј РїР°РїРєСѓ config/pvpbot РµСЃР»Рё РЅРµ СЃСѓС‰РµСЃС‚РІСѓРµС‚
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            // РРіРЅРѕСЂРёСЂСѓРµРј
        }
        
        configPath = configDir.resolve("kits.json");
        load();
    }
    
    /**
     * РЎРѕР·РґР°С‚СЊ РєРёС‚ РёР· РёРЅРІРµРЅС‚Р°СЂСЏ РёРіСЂРѕРєР°
     */
    public static boolean createKit(String kitName, ServerPlayerEntity player) {
        String key = kitName.toLowerCase();
        if (kitsRaw.containsKey(key)) {
            return false; // РљРёС‚ СѓР¶Рµ СЃСѓС‰РµСЃС‚РІСѓРµС‚
        }
        
        Map<String, Object> kitItems = new HashMap<>();
        var inventory = player.getInventory();
        var registryOps = RegistryOps.of(NbtOps.INSTANCE, player.getRegistryManager());
        
        // РљРѕРїРёСЂСѓРµРј РІСЃРµ РїСЂРµРґРјРµС‚С‹ РёР· РёРЅРІРµРЅС‚Р°СЂСЏ (СЃР»РѕС‚С‹ 0-35 + Р±СЂРѕРЅСЏ 36-39 + offhand 40)
        for (int i = 0; i < 41; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                // РЎРµСЂРёР°Р»РёР·СѓРµРј ItemStack РІ NBT С‡РµСЂРµР· CODEC
                DataResult<NbtElement> result = ItemStack.CODEC.encodeStart(registryOps, stack);
                if (result.result().isPresent()) {
                    NbtElement nbt = result.result().get();
                    // РљРѕРЅРІРµСЂС‚РёСЂСѓРµРј NBT РІ СЃС‚СЂРѕРєСѓ РґР»СЏ JSON
                    kitItems.put(String.valueOf(i), nbt.toString());
                }
            }
        }
        
        if (kitItems.isEmpty()) {
            return false; // РџСѓСЃС‚РѕР№ РёРЅРІРµРЅС‚Р°СЂСЊ
        }
        
        kitsRaw.put(key, kitItems);
        save();
        return true;
    }
    
    /**
     * РЈРґР°Р»РёС‚СЊ РєРёС‚
     */
    public static boolean deleteKit(String kitName) {
        boolean removed = kitsRaw.remove(kitName.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }
    
    /**
     * РџРѕР»СѓС‡РёС‚СЊ СЃРїРёСЃРѕРє РІСЃРµС… РєРёС‚РѕРІ
     */
    public static Set<String> getKitNames() {
        return new HashSet<>(kitsRaw.keySet());
    }
    
    /**
     * РџСЂРѕРІРµСЂРёС‚СЊ СЃСѓС‰РµСЃС‚РІСѓРµС‚ Р»Рё РєРёС‚
     */
    public static boolean kitExists(String kitName) {
        return kitsRaw.containsKey(kitName.toLowerCase());
    }
    
    /**
     * РџРѕР»СѓС‡РёС‚СЊ РїСЂРµРґРјРµС‚С‹ РєРёС‚Р°
     */
    public static Map<Integer, ItemStack> getKitItems(String kitName, ServerPlayerEntity player) {
        Map<String, Object> data = kitsRaw.get(kitName.toLowerCase());
        if (data == null) return null;
        
        var registryOps = RegistryOps.of(NbtOps.INSTANCE, player.getRegistryManager());
        Map<Integer, ItemStack> items = new HashMap<>();
        
        for (var entry : data.entrySet()) {
            try {
                int slot = Integer.parseInt(entry.getKey());
                String nbtString = entry.getValue().toString();
                
                // РџР°СЂСЃРёРј NBT СЃС‚СЂРѕРєСѓ
                NbtCompound nbt = net.minecraft.nbt.StringNbtReader.readCompound(nbtString);
                
                // Р”РµСЃРµСЂРёР°Р»РёР·СѓРµРј ItemStack С‡РµСЂРµР· CODEC
                DataResult<ItemStack> result = ItemStack.CODEC.parse(registryOps, nbt);
                if (result.result().isPresent()) {
                    items.put(slot, result.result().get());
                }
            } catch (Exception e) {
                System.out.println("[PVP_BOT] Failed to parse kit item: " + e.getMessage());
            }
        }
        return items;
    }
    
    /**
     * Р’С‹РґР°С‚СЊ РєРёС‚ Р±РѕС‚Сѓ
     */
    public static boolean giveKit(String kitName, ServerPlayerEntity bot) {
        Map<Integer, ItemStack> kitItems = getKitItems(kitName, bot);
        if (kitItems == null) return false;
        
        var inventory = bot.getInventory();
        
        // РћС‡РёС‰Р°РµРј РёРЅРІРµРЅС‚Р°СЂСЊ Р±РѕС‚Р°
        inventory.clear();
        
        // Р’С‹РґР°С‘Рј РїСЂРµРґРјРµС‚С‹
        for (var entry : kitItems.entrySet()) {
            int slot = entry.getKey();
            ItemStack stack = entry.getValue();
            inventory.setStack(slot, stack.copy());
        }
        
        return true;
    }
    
    /**
     * Р’С‹РґР°С‚СЊ РєРёС‚ РІСЃРµРј Р±РѕС‚Р°Рј С„СЂР°РєС†РёРё
     */
    public static int giveKitToFaction(String kitName, String factionName) {
        if (!kitExists(kitName)) return -1;
        
        Set<String> members = BotFaction.getMembers(factionName);
        if (members == null || members.isEmpty()) return 0;
        
        return members.size();
    }
    
    // ============ РЎРѕС…СЂР°РЅРµРЅРёРµ/Р·Р°РіСЂСѓР·РєР° ============
    
    private static void load() {
        if (configPath == null || !Files.exists(configPath)) return;
        
        try (Reader reader = Files.newBufferedReader(configPath)) {
            Map<String, Map<String, Object>> loaded = GSON.fromJson(
                reader, 
                new TypeToken<Map<String, Map<String, Object>>>(){}.getType()
            );
            if (loaded != null) {
                kitsRaw.clear();
                kitsRaw.putAll(loaded);
            }
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to load kits: " + e.getMessage());
        }
    }
    
    private static void save() {
        if (configPath == null) return;
        
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(kitsRaw, writer);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to save kits: " + e.getMessage());
        }
    }
}
