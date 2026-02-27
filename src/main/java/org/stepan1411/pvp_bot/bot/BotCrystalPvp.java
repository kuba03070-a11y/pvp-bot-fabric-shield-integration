package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * РћС‚РґРµР»СЊРЅС‹Р№ РєР»Р°СЃСЃ РґР»СЏ Crystal PVP
 * РџРѕР»РЅРѕСЃС‚СЊСЋ РєРѕРЅС‚СЂРѕР»РёСЂСѓРµС‚ РґРІРёР¶РµРЅРёРµ Рё Р±РѕР№ Р±РѕС‚Р°
 */
public class BotCrystalPvp {
    
    // РЎРѕСЃС‚РѕСЏРЅРёРµ Crystal PVP РґР»СЏ РєР°Р¶РґРѕРіРѕ Р±РѕС‚Р°
    private static class CrystalState {
        int step = 0;                    // РўРµРєСѓС‰РёР№ С€Р°Рі (0=РѕР±СЃРёРґРёР°РЅ, 1=РєСЂРёСЃС‚Р°Р»Р», 2=СѓРґР°СЂ)
        BlockPos lastObsidianPos = null; // РџРѕР·РёС†РёСЏ РїРѕСЃР»РµРґРЅРµРіРѕ РѕР±СЃРёРґРёР°РЅР°
        long lastActionTime = 0;         // Р’СЂРµРјСЏ РїРѕСЃР»РµРґРЅРµРіРѕ РґРµР№СЃС‚РІРёСЏ
        int cooldownTicks = 0;           // РљСѓР»РґР°СѓРЅ РјРµР¶РґСѓ РґРµР№СЃС‚РІРёСЏРјРё
        int stuckCounter = 0;            // РЎС‡С‘С‚С‡РёРє Р·Р°СЃС‚СЂРµРІР°РЅРёСЏ РЅР° РѕРґРЅРѕРј С€Р°РіРµ
        int lastStep = -1;               // РџРѕСЃР»РµРґРЅРёР№ РІС‹РїРѕР»РЅРµРЅРЅС‹Р№ С€Р°Рі
        int crystalNotFoundCounter = 0;  // РЎС‡С‘С‚С‡РёРє "РєСЂРёСЃС‚Р°Р»Р» РЅРµ РЅР°Р№РґРµРЅ"
        int crystalPlaceFailCounter = 0; // РЎС‡С‘С‚С‡РёРє РЅРµСѓРґР°С‡РЅС‹С… РїРѕРїС‹С‚РѕРє РїРѕСЃС‚Р°РІРёС‚СЊ РєСЂРёСЃС‚Р°Р»Р»
        java.util.Set<BlockPos> triedPositions = new java.util.HashSet<>(); // РџРѕРїСЂРѕР±РѕРІР°РЅРЅС‹Рµ РїРѕР·РёС†РёРё РґР»СЏ РѕР±СЃРёРґРёР°РЅР°
        int obsidianPlaceAttempts = 0;   // РЎС‡С‘С‚С‡РёРє РїРѕРїС‹С‚РѕРє РїРѕСЃС‚Р°РІРёС‚СЊ РѕР±СЃРёРґРёР°РЅ РЅР° С‚РµРєСѓС‰РµР№ РїРѕР·РёС†РёРё
    }
    
    private static final java.util.Map<String, CrystalState> states = new java.util.HashMap<>();
    
    /**
     * РџРѕР»СѓС‡РёС‚СЊ СЃРѕСЃС‚РѕСЏРЅРёРµ Р±РѕС‚Р°
     */
    private static CrystalState getState(String botName) {
        return states.computeIfAbsent(botName, k -> new CrystalState());
    }
    
    /**
     * РџСЂРѕРІРµСЂРёС‚СЊ РјРѕР¶РµС‚ Р»Рё Р±РѕС‚ РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊ Crystal PVP
     */
    public static boolean canUseCrystalPvp(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        if (!settings.isCrystalPvpEnabled()) return false;
        
        double distance = bot.distanceTo(target);
        if (distance < 2.5 || distance > 8.0) return false;
        
        PlayerInventory inventory = bot.getInventory();
        return hasObsidian(inventory) && hasEndCrystal(inventory);
    }
    
    /**
     * Р“Р»Р°РІРЅС‹Р№ РјРµС‚РѕРґ - РІС‹РїРѕР»РЅСЏРµС‚ Crystal PVP
     * Р’РѕР·РІСЂР°С‰Р°РµС‚ true РµСЃР»Рё Р±РѕС‚ Р·Р°РЅСЏС‚ Crystal PVP (РЅРµ РЅСѓР¶РµРЅ РѕР±С‹С‡РЅС‹Р№ combat)
     */
    public static boolean doCrystalPvp(ServerPlayerEntity bot, Entity target, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        CrystalState state = getState(bot.getName().getString());
        World world = bot.getEntityWorld();
        double distance = bot.distanceTo(target);
        
        // РџСЂРѕРІРµСЂРєР° РЅР° stuck: РµСЃР»Рё Р±РѕС‚ РґРѕР»РіРѕ РЅР° РѕРґРЅРѕРј С€Р°РіРµ - СЃР±СЂР°СЃС‹РІР°РµРј
        if (state.step == state.lastStep) {
            state.stuckCounter++;
            if (state.stuckCounter > 100) { // 5 СЃРµРєСѓРЅРґ (100 С‚РёРєРѕРІ)
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK on step " + state.step + "! Resetting state.");
                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 0;
                state.stuckCounter = 0;
                return true;
            }
        } else {
            state.stuckCounter = 0;
            state.lastStep = state.step;
        }
        
        
        // РљСѓР»РґР°СѓРЅ РјРµР¶РґСѓ РґРµР№СЃС‚РІРёСЏРјРё
        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
            // Р’Рѕ РІСЂРµРјСЏ РєСѓР»РґР°СѓРЅР° - РґРµСЂР¶РёРј РґРёСЃС‚Р°РЅС†РёСЋ
            maintainDistance(bot, target, settings);
            return true;
        }
        
        // Р•СЃР»Рё РІСЂР°Рі СЃР»РёС€РєРѕРј РґР°Р»РµРєРѕ - РїРѕРґС…РѕРґРёРј
        if (distance > 8.0) {
            moveToward(bot, target, settings.getMoveSpeed());
            state.step = 0;
            state.lastObsidianPos = null;
            return true;
        }
        
        // Р•СЃР»Рё РІСЂР°Рі СЃР»РёС€РєРѕРј Р±Р»РёР·РєРѕ - РѕС‚С…РѕРґРёРј
        if (distance < 2.5) {
            moveAway(bot, target, settings.getMoveSpeed());
            return true;
        }
        
        // Р’РђР–РќРћ: Р•СЃР»Рё РµСЃС‚СЊ РєСЂРёСЃС‚Р°Р»Р» СЂСЏРґРѕРј - СЃСЂР°Р·Сѓ РїРµСЂРµС…РѕРґРёРј Рє Р°С‚Р°РєРµ
        // РќРћ С‚РѕР»СЊРєРѕ РµСЃР»Рё РЅРµС‚ РєСѓР»РґР°СѓРЅР° (С‡С‚РѕР±С‹ РЅРµ РїСЂРµСЂС‹РІР°С‚СЊ СѓСЃС‚Р°РЅРѕРІРєСѓ)
        Entity nearCrystal = findNearestEndCrystal(bot, target, 6.0);
        if (nearCrystal != null && state.step != 2 && state.cooldownTicks == 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " РѕР±РЅР°СЂСѓР¶РёР» РєСЂРёСЃС‚Р°Р»Р», РїРµСЂРµС…РѕРґ Рє С€Р°РіСѓ 2 (С‚РµРєСѓС‰РёР№ step: " + state.step + ")");
            state.step = 2;
            state.cooldownTicks = 0; // РЎСЂР°Р·Сѓ Р°С‚Р°РєСѓРµРј
        }
        
        // Р’С‹РїРѕР»РЅСЏРµРј С‚РµРєСѓС‰РёР№ С€Р°Рі
        switch (state.step) {
            case 0: // РЁР°Рі 1: РЎС‚Р°РІРёРј РѕР±СЃРёРґРёР°РЅ
                return stepPlaceObsidian(bot, target, state, server, world, settings);
                
            case 1: // РЁР°Рі 2: РЎС‚Р°РІРёРј РєСЂРёСЃС‚Р°Р»Р»
                return stepPlaceCrystal(bot, target, state, server, world, settings);
                
            case 2: // РЁР°Рі 3: Р‘СЊС‘Рј РєСЂРёСЃС‚Р°Р»Р»
                return stepAttackCrystal(bot, target, state, server, world, settings, distance);
                
            default:
                state.step = 0;
                return true;
        }
    }
    
    /**
     * РЁР°Рі 0: РЈСЃС‚Р°РЅРѕРІРєР° РѕР±СЃРёРґРёР°РЅР°
     */
    private static boolean stepPlaceObsidian(ServerPlayerEntity bot, Entity target, CrystalState state, 
                                            net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // РЎРќРђР§РђР›Рђ РїСЂРѕРІРµСЂСЏРµРј РµСЃС‚СЊ Р»Рё СѓР¶Рµ РѕР±СЃРёРґРёР°РЅ СЂСЏРґРѕРј СЃ РІСЂР°РіРѕРј (РІ СЂР°РґРёСѓСЃРµ 5 blocks)
        BlockPos existingObsidian = findExistingObsidian(bot, target, world, 5.0);
        if (existingObsidian != null) {
            double distToExisting = Math.sqrt(bot.squaredDistanceTo(existingObsidian.getX() + 0.5, existingObsidian.getY() + 0.5, existingObsidian.getZ() + 0.5));
            
            if (distToExisting <= 4.0) {
                // РћР±СЃРёРґРёР°РЅ РµСЃС‚СЊ Рё Р±РѕС‚ РјРѕР¶РµС‚ РґРѕ РЅРµРіРѕ РґРѕСЃС‚Р°С‚СЊ - РёСЃРїРѕР»СЊР·СѓРµРј РµРіРѕ!
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " using existing obsidian at " + existingObsidian);
                state.lastObsidianPos = existingObsidian;
                state.step = 1; // РЎСЂР°Р·Сѓ Рє СѓСЃС‚Р°РЅРѕРІРєРµ РєСЂРёСЃС‚Р°Р»Р»Р°
                state.cooldownTicks = 0; // Р‘РµР· РєСѓР»РґР°СѓРЅР°
                state.stuckCounter = 0; // РЎР±СЂР°СЃС‹РІР°РµРј СЃС‡С‘С‚С‡РёРє Р·Р°СЃС‚СЂРµРІР°РЅРёСЏ
                return true;
            } else {
                // РћР±СЃРёРґРёР°РЅ РµСЃС‚СЊ РЅРѕ РґР°Р»РµРєРѕ - РїРѕРґС…РѕРґРёРј Р±Р»РёР¶Рµ
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " approaching existing obsidian, distance: " + String.format("%.2f", distToExisting));
                moveToward(bot, target, settings.getMoveSpeed());
                return true;
            }
        }
        
        // РћР±СЃРёРґРёР°РЅР° СЂСЏРґРѕРј РЅРµС‚ - СЃС‚Р°РІРёРј РЅРѕРІС‹Р№
        
        // РџСЂРѕРІРµСЂСЏРµРј РЅР°Р»РёС‡РёРµ РѕР±СЃРёРґРёР°РЅР°
        int obsidianSlot = findObsidian(inventory);
        if (obsidianSlot < 0) {
            return false; // РќРµС‚ РѕР±СЃРёРґРёР°РЅР° - РІС‹С…РѕРґРёРј РёР· Crystal PVP
        }
        
        // РќР°С…РѕРґРёРј РїРѕР·РёС†РёСЋ РґР»СЏ РѕР±СЃРёРґРёР°РЅР°
        BlockPos obsidianPos = findBestObsidianPosition(bot, target, world, state.triedPositions);
        if (obsidianPos == null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " could not find obsidian position!");
            // РќРµС‚ РјРµСЃС‚Р° - СЃР±СЂР°СЃС‹РІР°РµРј РїРѕРїСЂРѕР±РѕРІР°РЅРЅС‹Рµ РїРѕР·РёС†РёРё Рё РїСЂРѕР±СѓРµРј СЃРЅРѕРІР°
            if (!state.triedPositions.isEmpty()) {
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " clearing tried positions (" + state.triedPositions.size() + " positions)");
                state.triedPositions.clear();
            }
            // Р”РµСЂР¶РёРј РґРёСЃС‚Р°РЅС†РёСЋ
            maintainDistance(bot, target, settings);
            return true;
        }
        
        // РџСЂРѕРІРµСЂСЏРµРј РґРёСЃС‚Р°РЅС†РёСЋ РґРѕ РїРѕР·РёС†РёРё
        double distToPos = Math.sqrt(bot.squaredDistanceTo(obsidianPos.getX() + 0.5, obsidianPos.getY() + 0.5, obsidianPos.getZ() + 0.5));
        
        if (distToPos > 3.0) {
            moveToward(bot, target, settings.getMoveSpeed());
            return true;
        }
        
        // РћСЃС‚Р°РЅР°РІР»РёРІР°РµРј Р±РѕС‚Р°
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // РџРµСЂРµРєР»СЋС‡Р°РµРјСЃСЏ РЅР° РѕР±СЃРёРґРёР°РЅ
        if (!selectItem(bot, obsidianSlot)) {
            return true; // РќРµ СѓРґР°Р»РѕСЃСЊ РїРµСЂРµРєР»СЋС‡РёС‚СЊ - РїРѕРїСЂРѕР±СѓРµРј РІ СЃР»РµРґСѓСЋС‰РµРј С‚РёРєРµ
        }
        
        // РЎРјРѕС‚СЂРёРј РЅР° РїРѕР·РёС†РёСЋ
        lookAt(bot, obsidianPos);
        
        // РЈРІРµР»РёС‡РёРІР°РµРј СЃС‡С‘С‚С‡РёРє РїРѕРїС‹С‚РѕРє РґР»СЏ СЌС‚РѕР№ РїРѕР·РёС†РёРё
        state.obsidianPlaceAttempts++;
        
        // Р•СЃР»Рё 3 РїРѕРїС‹С‚РєРё РЅР° РѕРґРЅРѕР№ РїРѕР·РёС†РёРё РЅРµ СѓРґР°Р»РёСЃСЊ - РїСЂРѕР±СѓРµРј РґСЂСѓРіСѓСЋ
        if (state.obsidianPlaceAttempts >= 3) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " failed to place obsidian 3 times at " + obsidianPos + ", trying different position");
            state.triedPositions.add(obsidianPos);
            state.obsidianPlaceAttempts = 0;
            state.cooldownTicks = 3;
            return true;
        }
        
        // РЎС‚Р°РІРёРј Р±Р»РѕРє С‡РµСЂРµР· Carpet РєРѕРјР°РЅРґСѓ
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " placed obsidian at " + obsidianPos + " (attempt " + state.obsidianPlaceAttempts + "/3)");
            
            // РЈСЃРїРµС€РЅРѕ РїРѕСЃС‚Р°РІРёР»Рё - РїРµСЂРµС…РѕРґРёРј Рє СЃР»РµРґСѓСЋС‰РµРјСѓ С€Р°РіСѓ
            state.lastObsidianPos = obsidianPos;
            state.step = 1;
            state.cooldownTicks = 5; // РљСѓР»РґР°СѓРЅ С‡С‚РѕР±С‹ Р±Р»РѕРє СѓСЃРїРµР» СѓСЃС‚Р°РЅРѕРІРёС‚СЊСЃСЏ
            state.stuckCounter = 0; // РЎР±СЂР°СЃС‹РІР°РµРј СЃС‡С‘С‚С‡РёРє Р·Р°СЃС‚СЂРµРІР°РЅРёСЏ
            state.obsidianPlaceAttempts = 0; // РЎР±СЂР°СЃС‹РІР°РµРј СЃС‡С‘С‚С‡РёРє РїРѕРїС‹С‚РѕРє
            
        } catch (Exception e) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " error placing obsidian: " + e.getMessage());
            // РћС€РёР±РєР° - РїРѕРїСЂРѕР±СѓРµРј СЃРЅРѕРІР°
        }
        
        // РќР• РїРѕРІРѕСЂР°С‡РёРІР°РµРј РіРѕР»РѕРІСѓ РѕР±СЂР°С‚РЅРѕ - РѕСЃС‚Р°РІР»СЏРµРј СЃРјРѕС‚СЂРµС‚СЊ РЅР° РѕР±СЃРёРґРёР°РЅ
        return true;
    }
    
    /**
     * РЁР°Рі 1: РЈСЃС‚Р°РЅРѕРІРєР° РєСЂРёСЃС‚Р°Р»Р»Р°
     */
    private static boolean stepPlaceCrystal(ServerPlayerEntity bot, Entity target, CrystalState state,
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ РѕР±СЃРёРґРёР°РЅ СѓСЃС‚Р°РЅРѕРІР»РµРЅ
        if (state.lastObsidianPos == null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " no obsidian position!");
            state.step = 0;
            return true;
        }
        
        // Р’РђР–РќРћ: РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ РЅР° РїРѕР·РёС†РёРё РґРµР№СЃС‚РІРёС‚РµР»СЊРЅРѕ РѕР±СЃРёРґРёР°РЅ
        net.minecraft.block.BlockState blockAtPos = world.getBlockState(state.lastObsidianPos);
        
        if (!blockAtPos.getBlock().toString().contains("obsidian")) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " NO obsidian at position! Returning to step 0");
            // Р”РѕР±Р°РІР»СЏРµРј СЌС‚Сѓ РїРѕР·РёС†РёСЋ РІ СЃРїРёСЃРѕРє РЅРµСѓРґР°С‡РЅС‹С…
            if (state.lastObsidianPos != null) {
                state.triedPositions.add(state.lastObsidianPos);
            }
            state.step = 0;
            state.lastObsidianPos = null;
            state.obsidianPlaceAttempts = 0;
            return true;
        }
        
        // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ РЅР°Рґ РѕР±СЃРёРґРёР°РЅРѕРј СЃРІРѕР±РѕРґРЅРѕ
        net.minecraft.block.BlockState blockAbove = world.getBlockState(state.lastObsidianPos.up());
        
        if (!blockAbove.isAir() && !blockAbove.isReplaceable()) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " NO space above obsidian! Returning to step 0");
            state.step = 0;
            state.lastObsidianPos = null;
            return true;
        }
        
        // РџСЂРѕРІРµСЂСЏРµРј РЅР°Р»РёС‡РёРµ РєСЂРёСЃС‚Р°Р»Р»Р°
        int crystalSlot = findEndCrystal(inventory);
        if (crystalSlot < 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " no crystals in inventory! Exiting Crystal PVP.");
            state.step = 0;
            state.lastObsidianPos = null;
            state.stuckCounter = 0;
            return false; // РќРµС‚ РєСЂРёСЃС‚Р°Р»Р»Р° - РІС‹С…РѕРґРёРј РёР· Crystal PVP
        }
        
        // РћСЃС‚Р°РЅР°РІР»РёРІР°РµРј Р±РѕС‚Р°
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // РџРµСЂРµРєР»СЋС‡Р°РµРјСЃСЏ РЅР° РєСЂРёСЃС‚Р°Р»Р»
        if (!selectItem(bot, crystalSlot)) {
            return true; // РќРµ СѓРґР°Р»РѕСЃСЊ РїРµСЂРµРєР»СЋС‡РёС‚СЊ - РїРѕРїСЂРѕР±СѓРµРј РІ СЃР»РµРґСѓСЋС‰РµРј С‚РёРєРµ
        }
        
        // РЎРјРѕС‚СЂРёРј РЅР° РЎРђРњ РћР‘РЎРР”РРђРќ (РЅРµ РЅР° РІРѕР·РґСѓС… РЅР°Рґ РЅРёРј!)
        // РљСЂРёСЃС‚Р°Р»Р» РјРѕР¶РЅРѕ РїРѕСЃС‚Р°РІРёС‚СЊ С‚С‹РєРЅСѓРІ РїРѕ Р›Р®Р‘РћР™ РіСЂР°РЅРё РѕР±СЃРёРґРёР°РЅР°
        lookAt(bot, state.lastObsidianPos);
        
        // Р’РђР–РќРћ: РџСЂРѕРІРµСЂСЏРµРј СЃС‡С‘С‚С‡РёРє РЅРµСѓРґР°С‡РЅС‹С… РїРѕРїС‹С‚РѕРє РџР•Р Р•Р” СѓСЃС‚Р°РЅРѕРІРєРѕР№
        if (state.crystalPlaceFailCounter >= 5) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK - crystal placement failed 5 times! Resetting state.");
            state.step = 0;
            state.lastObsidianPos = null;
            state.cooldownTicks = 5;
            state.crystalPlaceFailCounter = 0;
            return true;
        }
        
        // РЎС‚Р°РІРёРј РєСЂРёСЃС‚Р°Р»Р» С‡РµСЂРµР· Carpet РєРѕРјР°РЅРґСѓ
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " placed crystal (clicked obsidian at " + state.lastObsidianPos + ") - attempt " + (state.crystalPlaceFailCounter + 1) + "/5");
            
            // РЈРІРµР»РёС‡РёРІР°РµРј СЃС‡С‘С‚С‡РёРє РЅРµСѓРґР°С‡РЅС‹С… РїРѕРїС‹С‚РѕРє (СЃР±СЂРѕСЃРёС‚СЃСЏ РµСЃР»Рё РєСЂРёСЃС‚Р°Р»Р» РїРѕСЏРІРёС‚СЃСЏ)
            state.crystalPlaceFailCounter++;
            
            // РЈСЃРїРµС€РЅРѕ РІС‹РїРѕР»РЅРёР»Рё РєРѕРјР°РЅРґСѓ - РїРµСЂРµС…РѕРґРёРј Рє Р°С‚Р°РєРµ
            state.step = 2;
            state.cooldownTicks = 5; // РљСѓР»РґР°СѓРЅ С‡С‚РѕР±С‹ РєСЂРёСЃС‚Р°Р»Р» СѓСЃРїРµР» РїРѕСЏРІРёС‚СЊСЃСЏ
            state.stuckCounter = 0; // РЎР±СЂР°СЃС‹РІР°РµРј СЃС‡С‘С‚С‡РёРє Р·Р°СЃС‚СЂРµРІР°РЅРёСЏ
            
        } catch (Exception e) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " error placing crystal: " + e.getMessage());
            // РћС€РёР±РєР° - РІРѕР·РІСЂР°С‰Р°РµРјСЃСЏ Рє РѕР±СЃРёРґРёР°РЅСѓ
            state.step = 0;
            state.lastObsidianPos = null;
            state.crystalPlaceFailCounter = 0;
        }
        
        // РќР• РїРѕРІРѕСЂР°С‡РёРІР°РµРј РіРѕР»РѕРІСѓ РѕР±СЂР°С‚РЅРѕ - РѕСЃС‚Р°РІР»СЏРµРј СЃРјРѕС‚СЂРµС‚СЊ РЅР° РєСЂРёСЃС‚Р°Р»Р»
        return true;
    }
    
    /**
     * РЁР°Рі 2: РђС‚Р°РєР° РєСЂРёСЃС‚Р°Р»Р»Р°
     */
    private static boolean stepAttackCrystal(ServerPlayerEntity bot, Entity target, CrystalState state,
                                            net.minecraft.server.MinecraftServer server, World world, 
                                            BotSettings settings, double distance) {
        PlayerInventory inventory = bot.getInventory();
        
        // РћСЃС‚Р°РЅР°РІР»РёРІР°РµРј Р±РѕС‚Р° РІРѕ РІСЂРµРјСЏ Р°С‚Р°РєРё
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // РџРµСЂРµРєР»СЋС‡Р°РµРјСЃСЏ РЅР° РѕСЂСѓР¶РёРµ
        int weaponSlot = findMeleeWeapon(inventory);
        if (weaponSlot >= 0) {
            selectItem(bot, weaponSlot);
        }
        
        // РС‰РµРј РєСЂРёСЃС‚Р°Р»Р»
        Entity crystal = findNearestEndCrystal(bot, target, 6.0);
        
        if (crystal != null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " found crystal at distance " + bot.distanceTo(crystal));
            
            // РЎРјРѕС‚СЂРёРј РЅР° РєСЂРёСЃС‚Р°Р»Р»
            lookAtEntity(bot, crystal);
            
            // РЎР±СЂР°СЃС‹РІР°РµРј СЃС‡С‘С‚С‡РёРє "РєСЂРёСЃС‚Р°Р»Р» РЅРµ РЅР°Р№РґРµРЅ"
            state.crystalNotFoundCounter = 0;
            // РЎР±СЂР°СЃС‹РІР°РµРј СЃС‡С‘С‚С‡РёРє РЅРµСѓРґР°С‡РЅС‹С… РїРѕРїС‹С‚РѕРє СѓСЃС‚Р°РЅРѕРІРєРё (РєСЂРёСЃС‚Р°Р»Р» РїРѕСЏРІРёР»СЃСЏ!)
            state.crystalPlaceFailCounter = 0;
            
            // Р‘СЊС‘Рј РєСЂРёСЃС‚Р°Р»Р» РќРђРџР РЇРњРЈР® (РЅРµ С‡РµСЂРµР· РєРѕРјР°РЅРґСѓ)
            bot.attack(crystal);
            bot.swingHand(Hand.MAIN_HAND);
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " hit crystal!");
            
            // Р РµС€Р°РµРј С‡С‚Рѕ РґРµР»Р°С‚СЊ РґР°Р»СЊС€Рµ
            if (distance <= 4.5 && state.lastObsidianPos != null) {
                // Р’СЂР°Рі СЂСЏРґРѕРј - СЃРїР°РјРёРј РєСЂРёСЃС‚Р°Р»Р»С‹
                state.step = 1;
                state.cooldownTicks = 2;
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " moving to step 1 (new crystal)");
            } else {
                // Р’СЂР°Рі РґР°Р»РµРєРѕ - РЅР°С‡РёРЅР°РµРј Р·Р°РЅРѕРІРѕ
                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 5;
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " moving to step 0 (new obsidian)");
            }
            
        } else {
            state.crystalNotFoundCounter++;
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " DID NOT FIND crystal! (" + state.crystalNotFoundCounter + "/3)");
            
            // Р•СЃР»Рё 3 СЂР°Р·Р° РЅРµ РЅР°С€Р»Рё РєСЂРёСЃС‚Р°Р»Р» - СЃР±СЂР°СЃС‹РІР°РµРј СЃРѕСЃС‚РѕСЏРЅРёРµ
            if (state.crystalNotFoundCounter >= 3) {
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK - crystal not found 3 times! Resetting state.");
                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 5;
                state.crystalNotFoundCounter = 0;
                return true;
            }
            
            // РљСЂРёСЃС‚Р°Р»Р»Р° РЅРµС‚ - РїСЂРѕР±СѓРµРј РїРѕСЃС‚Р°РІРёС‚СЊ СЃРЅРѕРІР°
            if (state.lastObsidianPos != null) {
                state.step = 1;
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " returning to step 1 (place crystal)");
            } else {
                state.step = 0;
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " returning to step 0 (place obsidian)");
            }
            state.cooldownTicks = 3;
        }
        
        // РќР• РїРѕРІРѕСЂР°С‡РёРІР°РµРј РіРѕР»РѕРІСѓ РѕР±СЂР°С‚РЅРѕ Рє РёРіСЂРѕРєСѓ - РѕСЃС‚Р°РІР»СЏРµРј СЃРјРѕС‚СЂРµС‚СЊ РЅР° РєСЂРёСЃС‚Р°Р»Р»
        // Р“РѕР»РѕРІР° РїРѕРІРµСЂРЅС‘С‚СЃСЏ Рє РёРіСЂРѕРєСѓ С‚РѕР»СЊРєРѕ РІ СЃР»РµРґСѓСЋС‰РµРј С†РёРєР»Рµ
        
        return true;
    }
    
    /**
     * РџРѕРґРґРµСЂР¶РёРІР°С‚СЊ РѕРїС‚РёРјР°Р»СЊРЅСѓСЋ РґРёСЃС‚Р°РЅС†РёСЋ (3-5 blocks)
     */
    private static void maintainDistance(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        double distance = bot.distanceTo(target);
        
        if (distance < 3.0) {
            // РЎР»РёС€РєРѕРј Р±Р»РёР·РєРѕ - РѕС‚С…РѕРґРёРј
            moveAway(bot, target, settings.getMoveSpeed() * 0.7);
        } else if (distance > 5.5) {
            // РЎР»РёС€РєРѕРј РґР°Р»РµРєРѕ - РїРѕРґС…РѕРґРёРј
            moveToward(bot, target, settings.getMoveSpeed() * 0.7);
        } else {
            // РћРїС‚РёРјР°Р»СЊРЅР°СЏ РґРёСЃС‚Р°РЅС†РёСЏ - РїСЂРѕСЃС‚Рѕ СЃРјРѕС‚СЂРёРј РЅР° С†РµР»СЊ, РЅРµ РґРІРёРіР°РµРјСЃСЏ
            lookAtEntity(bot, target);
        }
    }
    
    /**
     * Р”РІРёРіР°С‚СЊСЃСЏ Рє С†РµР»Рё
     */
    private static void moveToward(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d direction = targetPos.subtract(botPos).normalize();
        
        bot.setVelocity(direction.x * speed, bot.getVelocity().y, direction.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    /**
     * РћС‚С…РѕРґРёС‚СЊ from target
     */
    private static void moveAway(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d direction = botPos.subtract(targetPos).normalize();
        
        bot.setVelocity(direction.x * speed, bot.getVelocity().y, direction.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    /**
     * РЎС‚СЂРµР№С„ (РґРІРёР¶РµРЅРёРµ РїРѕ РєСЂСѓРіСѓ)
     */
    private static void strafe(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d toTarget = targetPos.subtract(botPos).normalize();
        
        // РџРµСЂРїРµРЅРґРёРєСѓР»СЏСЂРЅРѕРµ РЅР°РїСЂР°РІР»РµРЅРёРµ (СЃС‚СЂРµР№С„ РІР»РµРІРѕ)
        Vec3d strafeDir = new Vec3d(-toTarget.z, 0, toTarget.x);
        
        bot.setVelocity(strafeDir.x * speed, bot.getVelocity().y, strafeDir.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    /**
     * РЎРјРѕС‚СЂРµС‚СЊ РЅР° РїРѕР·РёС†РёСЋ
     */
    private static void lookAt(ServerPlayerEntity bot, BlockPos pos) {
        Vec3d target = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3d botPos = bot.getEyePos();
        
        double dx = target.x - botPos.x;
        double dy = target.y - botPos.y;
        double dz = target.z - botPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * РЎРјРѕС‚СЂРµС‚СЊ РЅР° СЃСѓС‰РЅРѕСЃС‚СЊ
     */
    private static void lookAtEntity(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetPos.x - botPos.x;
        double dy = targetPos.y - botPos.y;
        double dz = targetPos.z - botPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Р’С‹Р±СЂР°С‚СЊ РїСЂРµРґРјРµС‚ РІ СЂСѓРєРµ
     */
    private static boolean selectItem(ServerPlayerEntity bot, int slot) {
        PlayerInventory inventory = bot.getInventory();
        
        // Р•СЃР»Рё РїСЂРµРґРјРµС‚ РІ РёРЅРІРµРЅС‚Р°СЂРµ (РЅРµ РІ С…РѕС‚Р±Р°СЂРµ) - РїРµСЂРµРјРµС‰Р°РµРј
        if (slot >= 9) {
            ItemStack item = inventory.getStack(slot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(slot, current);
            inventory.setStack(0, item);
            slot = 0;
        }
        
        // РџРµСЂРµРєР»СЋС‡Р°РµРј СЃР»РѕС‚
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, slot);
        return true;
    }
    
    /**
     * РќР°Р№С‚Рё РѕР±СЃРёРґРёР°РЅ СЂСЏРґРѕРј СЃ РІСЂР°РіРѕРј (Р»СЋР±РѕР№, РЅРµ С‚РѕР»СЊРєРѕ С‚РѕС‚ С‡С‚Рѕ РїРѕСЃС‚Р°РІРёР» Р±РѕС‚)
     */
    private static BlockPos findExistingObsidian(ServerPlayerEntity bot, Entity target, World world, double maxDistance) {
        BlockPos targetPos = target.getBlockPos();
        
        // РџСЂРѕРІРµСЂСЏРµРј Р±Р»РѕРєРё РІРѕРєСЂСѓРі РІСЂР°РіР° РІ СЂР°РґРёСѓСЃРµ maxDistance
        int radius = (int) Math.ceil(maxDistance);
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = targetPos.add(dx, dy, dz);
                    
                    // РџСЂРѕРІРµСЂСЏРµРј РґРёСЃС‚Р°РЅС†РёСЋ РѕС‚ РІСЂР°РіР°
                    double distFromTarget = Math.sqrt(target.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (distFromTarget > maxDistance) continue;
                    
                    // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ СЌС‚Рѕ РѕР±СЃРёРґРёР°РЅ
                    net.minecraft.block.BlockState blockState = world.getBlockState(pos);
                    if (!blockState.getBlock().toString().contains("obsidian")) continue;
                    
                    // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ РЅР°Рґ РѕР±СЃРёРґРёР°РЅРѕРј СЃРІРѕР±РѕРґРЅРѕ
                    net.minecraft.block.BlockState blockAbove = world.getBlockState(pos.up());
                    if (!blockAbove.isAir() && !blockAbove.isReplaceable()) continue;
                    
                    // Р’РђР–РќРћ: РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ Р‘РћРў РјРѕР¶РµС‚ РґРѕСЃС‚Р°С‚СЊ РґРѕ РѕР±СЃРёРґРёР°РЅР° (в‰¤ 4 Р±Р»РѕРєР°)
                    double distFromBot = Math.sqrt(bot.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (distFromBot > 4.0) {
                        continue;
                    }
                    
                    System.out.println("[Crystal PVP] Found suitable obsidian at " + pos + ", distance from enemy: " + String.format("%.2f", distFromTarget) + ", from bot: " + String.format("%.2f", distFromBot));
                    return pos;
                }
            }
        }
        
        return null;
    }
    
    /**
     * РќР°Р№С‚Рё Р»СѓС‡С€СѓСЋ РїРѕР·РёС†РёСЋ РґР»СЏ РѕР±СЃРёРґРёР°РЅР° - Р РЇР”РћРњ СЃ РІСЂР°РіРѕРј
     */
    private static BlockPos findBestObsidianPosition(ServerPlayerEntity bot, Entity target, World world, java.util.Set<BlockPos> triedPositions) {
        BlockPos targetPos = target.getBlockPos();
        
        // РџСЂРѕРІРµСЂСЏРµРј Р±Р»РѕРєРё РІРѕРєСЂСѓРі РІСЂР°РіР°
        BlockPos[] candidates = {
            targetPos.north(),
            targetPos.south(),
            targetPos.east(),
            targetPos.west(),
            targetPos.north().east(),
            targetPos.north().west(),
            targetPos.south().east(),
            targetPos.south().west(),
        };
        
        for (BlockPos pos : candidates) {
            // РџСЂРѕРїСѓСЃРєР°РµРј СѓР¶Рµ РїРѕРїСЂРѕР±РѕРІР°РЅРЅС‹Рµ РїРѕР·РёС†РёРё
            if (triedPositions.contains(pos)) {
                continue;
            }
            
            // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ Р±Р»РѕРє СЃРІРѕР±РѕРґРµРЅ
            if (!world.getBlockState(pos).isAir() && !world.getBlockState(pos).isReplaceable()) {
                continue;
            }
            
            // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ РЅР°Рґ Р±Р»РѕРєРѕРј РµСЃС‚СЊ РјРµСЃС‚Рѕ РґР»СЏ РєСЂРёСЃС‚Р°Р»Р»Р°
            if (!world.getBlockState(pos.up()).isAir() && !world.getBlockState(pos.up()).isReplaceable()) {
                continue;
            }
            
            // РџСЂРѕРІРµСЂСЏРµРј РґРёСЃС‚Р°РЅС†РёСЋ РґРѕ Р±РѕС‚Р°
            double dist = Math.sqrt(bot.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            
            if (dist <= 4.0) {
                System.out.println("[Crystal PVP] Found suitable position: " + pos);
                return pos;
            }
        }
        
        System.out.println("[Crystal PVP] No suitable positions found!");
        return null;
    }
    
    /**
     * РќР°Р№С‚Рё Р±Р»РёР¶Р°Р№С€РёР№ End Crystal
     */
    private static Entity findNearestEndCrystal(ServerPlayerEntity bot, Entity target, double maxDistance) {
        World world = bot.getEntityWorld();
        net.minecraft.util.math.Box searchBox = target.getBoundingBox().expand(maxDistance);
        
        Entity nearestCrystal = null;
        double nearestDist = maxDistance + 1;
        int crystalCount = 0;
        
        
        for (Entity entity : world.getOtherEntities(bot, searchBox)) {
            
            if (entity instanceof net.minecraft.entity.decoration.EndCrystalEntity) {
                crystalCount++;
                double dist = target.distanceTo(entity);
                System.out.println("[Crystal PVP] Р­С‚Рѕ РєСЂРёСЃС‚Р°Р»Р»! Р”РёСЃС‚Р°РЅС†РёСЏ from target: " + String.format("%.2f", dist));
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestCrystal = entity;
                }
            }
        }
        
        if (crystalCount > 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " found " + crystalCount + " crystals in radius " + maxDistance);
        } else {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " РќР• found РЅРё РѕРґРЅРѕРіРѕ РєСЂРёСЃС‚Р°Р»Р»Р°!");
        }
        
        return nearestCrystal;
    }
    
    /**
     * РќР°Р№С‚Рё РѕР±СЃРёРґРёР°РЅ РІ РёРЅРІРµРЅС‚Р°СЂРµ
     */
    private static int findObsidian(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.OBSIDIAN) return i;
        }
        return -1;
    }
    
    /**
     * РќР°Р№С‚Рё End Crystal РІ РёРЅРІРµРЅС‚Р°СЂРµ
     */
    private static int findEndCrystal(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.END_CRYSTAL) return i;
        }
        return -1;
    }
    
    /**
     * РќР°Р№С‚Рё РѕСЂСѓР¶РёРµ Р±Р»РёР¶РЅРµРіРѕ Р±РѕСЏ
     */
    private static int findMeleeWeapon(PlayerInventory inventory) {
        // РџСЂРёРѕСЂРёС‚РµС‚: РјРµС‡ > С‚РѕРїРѕСЂ
        int bestSlot = -1;
        int bestPriority = -1;
        
        // РС‰РµРј РІРѕ РІСЃС‘Рј РёРЅРІРµРЅС‚Р°СЂРµ (0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            int priority = -1;
            
            if (stack.getItem().toString().contains("sword")) {
                priority = 10;
            } else if (stack.getItem().toString().contains("axe")) {
                priority = 5;
            }
            
            if (priority > bestPriority) {
                bestPriority = priority;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
    
    /**
     * РџСЂРѕРІРµСЂРёС‚СЊ РЅР°Р»РёС‡РёРµ РѕР±СЃРёРґРёР°РЅР°
     */
    private static boolean hasObsidian(PlayerInventory inventory) {
        return findObsidian(inventory) >= 0;
    }
    
    /**
     * РџСЂРѕРІРµСЂРёС‚СЊ РЅР°Р»РёС‡РёРµ End Crystal
     */
    private static boolean hasEndCrystal(PlayerInventory inventory) {
        return findEndCrystal(inventory) >= 0;
    }
    
    /**
     * РЎР±СЂРѕСЃРёС‚СЊ СЃРѕСЃС‚РѕСЏРЅРёРµ Р±РѕС‚Р°
     */
    public static void reset(String botName) {
        states.remove(botName);
    }
}
