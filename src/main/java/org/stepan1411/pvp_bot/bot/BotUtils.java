package org.stepan1411.pvp_bot.bot;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.HashMap;
import java.util.Map;

public class BotUtils {
    
    private static final Map<String, BotState> botStates = new HashMap<>();
    
    public static class BotState {
        public int shieldCooldown = 0;
        public int eatCooldown = 0;
        public boolean isBlocking = false;
        public boolean isEating = false;
        public int eatingTicks = 0;
        public int windChargeCooldown = 0;
        public int eatingSlot = -1; // РЎР»РѕС‚ СЃ РµРґРѕР№ РєРѕС‚РѕСЂСѓСЋ РµРґРёРј
        public int potionCooldown = 0; // РљСѓР»РґР°СѓРЅ РЅР° Р·РµР»СЊСЏ
        public int buffPotionCooldown = 0; // РљСѓР»РґР°СѓРЅ РЅР° Р±Р°С„С„РѕРІС‹Рµ Р·РµР»СЊСЏ
        public boolean isThrowingPotion = false; // Р‘СЂРѕСЃР°РµРј Р·РµР»СЊРµ - РЅРµ СЃРјРѕС‚СЂРµС‚СЊ РЅР° С†РµР»СЊ
        public int throwingPotionTicks = 0;
        public boolean isMending = false; // Р§РёРЅРёРјСЃСЏ - РЅРµ СЃРјРѕС‚СЂРµС‚СЊ РЅР° С†РµР»СЊ, СѓР±РµРіР°С‚СЊ
        public int mendingCooldown = 0; // РљСѓР»РґР°СѓРЅ РјРµР¶РґСѓ Р±СЂРѕСЃРєР°РјРё XP Р±СѓС‚С‹Р»РѕРє
        public int xpBottlesThrown = 0; // РЎРєРѕР»СЊРєРѕ Р±СѓС‚С‹Р»РѕРє СѓР¶Рµ Р±СЂРѕСЃРёР»Рё
        public int xpBottlesNeeded = 0; // РЎРєРѕР»СЊРєРѕ Р±СѓС‚С‹Р»РѕРє РЅСѓР¶РЅРѕ Р±СЂРѕСЃРёС‚СЊ
        public java.util.List<Integer> potionsToThrow = new java.util.ArrayList<>(); // РћС‡РµСЂРµРґСЊ Р·РµР»РёР№ РґР»СЏ Р±СЂРѕСЃРєР°
        public ItemStack savedOffhandItem = ItemStack.EMPTY; // РЎРѕС…СЂР°РЅС‘РЅРЅС‹Р№ РїСЂРµРґРјРµС‚ РёР· offhand (С‚РѕС‚РµРј) РїРµСЂРµРґ Р±Р»РѕРєРёСЂРѕРІРєРѕР№
    }
    
    public static BotState getState(String botName) {
        return botStates.computeIfAbsent(botName, k -> new BotState());
    }
    
    public static void removeState(String botName) {
        botStates.remove(botName);
    }
    
    /**
     * РћР±РЅРѕРІР»РµРЅРёРµ СѓС‚РёР»РёС‚ Р±РѕС‚Р°
     */
    public static void update(ServerPlayerEntity bot, MinecraftServer server) {
        BotSettings settings = BotSettings.get();
        BotState state = getState(bot.getName().getString());
        
        // РЈРјРµРЅСЊС€Р°РµРј РєСѓР»РґР°СѓРЅС‹
        if (state.shieldCooldown > 0) state.shieldCooldown--;
        if (state.eatCooldown > 0) state.eatCooldown--;
        if (state.windChargeCooldown > 0) state.windChargeCooldown--;
        if (state.potionCooldown > 0) state.potionCooldown--;
        if (state.buffPotionCooldown > 0) state.buffPotionCooldown--;
        if (state.mendingCooldown > 0) state.mendingCooldown--;
        
        // РџР РРћР РРўР•Рў 1: РђРІС‚Рѕ-СЂРµРјРѕРЅС‚ Р±СЂРѕРЅРё (РµСЃР»Рё РЅСѓР¶РЅРѕ - РѕС‚СЃС‚СѓРїР°РµРј Рё С‡РёРЅРёРјСЃСЏ)
        if (settings.isAutoMendEnabled()) {
            boolean needsMending = handleAutoMend(bot, state, settings, server);
            if (needsMending) {
                return; // Р§РёРЅРёРјСЃСЏ - РЅРµ РґРµР»Р°РµРј РЅРёС‡РµРіРѕ РґСЂСѓРіРѕРіРѕ
            }
        }
        
        // РћР±СЂР°Р±РѕС‚РєР° Р±СЂРѕСЃРєР° Р·РµР»СЊСЏ - СЃРјРѕС‚СЂРёРј РІРЅРёР· Рё Р±СЂРѕСЃР°РµРј
        if (state.isThrowingPotion) {
            state.throwingPotionTicks++;
            // РџСЂРёРЅСѓРґРёС‚РµР»СЊРЅРѕ СЃРјРѕС‚СЂРёРј РІРЅРёР·
            bot.setPitch(90);
            
            if (state.throwingPotionTicks == 2) {
                // Р‘СЂРѕСЃР°РµРј РЅР° 2-Р№ С‚РёРє
                executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
            }
            if (state.throwingPotionTicks >= 5) {
                // РџСЂРѕРІРµСЂСЏРµРј РµСЃС‚СЊ Р»Рё РµС‰С‘ Р·РµР»СЊСЏ РІ РѕС‡РµСЂРµРґРё
                if (!state.potionsToThrow.isEmpty()) {
                    int nextSlot = state.potionsToThrow.remove(0);
                    var inventory = bot.getInventory();
                    
                    // РџРµСЂРµРјРµС‰Р°РµРј РІ С…РѕС‚Р±Р°СЂ РµСЃР»Рё РЅСѓР¶РЅРѕ
                    if (nextSlot >= 9) {
                        ItemStack potion = inventory.getStack(nextSlot);
                        ItemStack current = inventory.getStack(8);
                        inventory.setStack(nextSlot, current);
                        inventory.setStack(8, potion);
                        nextSlot = 8;
                    }
                    
                    org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, nextSlot);
                    state.throwingPotionTicks = 0; // РЎР±СЂР°СЃС‹РІР°РµРј РґР»СЏ СЃР»РµРґСѓСЋС‰РµРіРѕ Р·РµР»СЊСЏ
                } else {
                    // РћС‡РµСЂРµРґСЊ РїСѓСЃС‚Р° - Р·Р°РєР°РЅС‡РёРІР°РµРј
                    state.isThrowingPotion = false;
                    state.throwingPotionTicks = 0;
                }
            }
            return; // РќРµ РґРµР»Р°РµРј РЅРёС‡РµРіРѕ РґСЂСѓРіРѕРіРѕ РїРѕРєР° Р±СЂРѕСЃР°РµРј
        }
        
        // РџР»Р°РІР°РЅРёРµ
        handleSwimming(bot);
        
        // РђРІС‚Рѕ-С‚РѕС‚РµРј
        if (settings.isAutoTotemEnabled()) {
            handleAutoTotem(bot);
        }
        
        // РђРІС‚Рѕ-Р±Р°С„С„С‹ (Р·РµР»СЊСЏ СЃРёР»С‹, СЃРєРѕСЂРѕСЃС‚Рё, РѕРіРЅРµСЃС‚РѕР№РєРѕСЃС‚Рё) РєРѕРіРґР° РІ Р±РѕСЋ
        if (settings.isAutoPotionEnabled() && !state.isEating) {
            handleAutoBuffPotions(bot, state, server);
        }
        
        // РђРІС‚Рѕ-РµРґР° (РїСЂРёРѕСЂРёС‚РµС‚ РЅР°Рґ С‰РёС‚РѕРј)
        // Р’СЃРµРіРґР° РѕР±СЂР°Р±Р°С‚С‹РІР°РµРј РµСЃР»Рё СѓР¶Рµ РµРґРёРј, РёР»Рё РµСЃР»Рё РЅРµ Р±Р»РѕРєРёСЂСѓРµРј
        if (settings.isAutoEatEnabled() && (state.isEating || !state.isBlocking)) {
            handleAutoEat(bot, state, settings, server);
        }
        
        // РђРІС‚Рѕ-С‰РёС‚ (С‚РѕР»СЊРєРѕ РµСЃР»Рё РЅРµ РµРґРёРј)
        if (settings.isAutoShieldEnabled() && !state.isEating) {
            handleAutoShield(bot, state, settings, server);
        }
    }
    
    /**
     * РџР»Р°РІР°РЅРёРµ - Р±РѕС‚ РїР»С‹РІС‘С‚ РІРІРµСЂС… РєРѕРіРґР° РІ РІРѕРґРµ
     */
    private static void handleSwimming(ServerPlayerEntity bot) {
        if (bot.isTouchingWater() || bot.isSubmergedInWater()) {
            bot.setSwimming(true);
            
            // РџР»С‹РІС‘Рј РІРІРµСЂС… СЃРёР»СЊРЅРµРµ
            if (bot.isSubmergedInWater()) {
                bot.addVelocity(0, 0.08, 0); // РЎРёР»СЊРЅРµРµ РїР»С‹РІС‘Рј РІРІРµСЂС…
                bot.setSprinting(true); // РЎРїСЂРёРЅС‚ РІ РІРѕРґРµ = Р±С‹СЃС‚СЂРѕРµ РїР»Р°РІР°РЅРёРµ
            } else if (bot.isTouchingWater()) {
                bot.addVelocity(0, 0.04, 0); // Р”РµСЂР¶РёРјСЃСЏ РЅР° РїРѕРІРµСЂС…РЅРѕСЃС‚Рё
            }
            
            // РџСЂС‹РіР°РµРј РµСЃР»Рё РЅР° РїРѕРІРµСЂС…РЅРѕСЃС‚Рё РІРѕРґС‹
            if (bot.isOnGround() && bot.isTouchingWater()) {
                bot.jump();
            }
        }
    }
    
    /**
     * РђРІС‚Рѕ-С‚РѕС‚РµРј
     * РќР• Р·Р°РїСѓСЃРєР°РµС‚СЃСЏ РєРѕРіРґР° Р±РѕС‚ Р±Р»РѕРєРёСЂСѓРµС‚ С‰РёС‚РѕРј
     */
    private static void handleAutoTotem(ServerPlayerEntity bot) {
        BotState state = getState(bot.getName().getString());
        
        // РќР• РјРµРЅСЏРµРј offhand РєРѕРіРґР° Р±РѕС‚ Р±Р»РѕРєРёСЂСѓРµС‚ С‰РёС‚РѕРј
        if (state.isBlocking) {
            return;
        }
        
        var inventory = bot.getInventory();
        ItemStack offhand = inventory.getStack(40);
        
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                inventory.setStack(i, offhand.copy());
                inventory.setStack(40, stack.copy());
                return;
            }
        }
    }
    
    /**
     * РђРІС‚Рѕ-Р±Р°С„С„С‹ - Р·РµР»СЊСЏ СЃРёР»С‹, СЃРєРѕСЂРѕСЃС‚Рё, РѕРіРЅРµСЃС‚РѕР№РєРѕСЃС‚Рё
     * РСЃРїРѕР»СЊР·СѓСЋС‚СЃСЏ РєРѕРіРґР° Р±РѕС‚ РІ Р±РѕСЋ Рё СЌС„С„РµРєС‚ РѕС‚СЃСѓС‚СЃС‚РІСѓРµС‚ РёР»Рё Р·Р°РєР°РЅС‡РёРІР°РµС‚СЃСЏ
     * Р‘СЂРѕСЃР°РµС‚ Р’РЎР• РЅСѓР¶РЅС‹Рµ Р·РµР»СЊСЏ СЃСЂР°Р·Сѓ
     */
    private static void handleAutoBuffPotions(ServerPlayerEntity bot, BotState state, MinecraftServer server) {
        // РџСЂРѕРІРµСЂСЏРµРј РІ Р±РѕСЋ Р»Рё Р±РѕС‚
        var combatState = BotCombat.getState(bot.getName().getString());
        if (combatState.target == null) return; // РќРµ РІ Р±РѕСЋ
        
        if (state.buffPotionCooldown > 0) return; // РљСѓР»РґР°СѓРЅ
        if (state.isThrowingPotion) return; // РЈР¶Рµ Р±СЂРѕСЃР°РµРј
        
        var inventory = bot.getInventory();
        
        // РЎРѕР±РёСЂР°РµРј РІСЃРµ РЅСѓР¶РЅС‹Рµ Р·РµР»СЊСЏ РІ РѕС‡РµСЂРµРґСЊ
        java.util.List<Integer> potionsToUse = new java.util.ArrayList<>();
        
        // РџСЂРѕРІРµСЂСЏРµРј РЅСѓР¶РЅС‹ Р»Рё Р±Р°С„С„С‹ (СЌС„С„РµРєС‚ РѕС‚СЃСѓС‚СЃС‚РІСѓРµС‚ РёР»Рё Р·Р°РєР°РЅС‡РёРІР°РµС‚СЃСЏ < 5 СЃРµРє)
        boolean needStrength = !hasEffect(bot, StatusEffects.STRENGTH, 100);
        boolean needSpeed = !hasEffect(bot, StatusEffects.SPEED, 100);
        boolean needFireResist = !hasEffect(bot, StatusEffects.FIRE_RESISTANCE, 100);
        
        if (needStrength) {
            int slot = findSplashBuffPotion(inventory, "strength");
            if (slot >= 0) potionsToUse.add(slot);
        }
        
        if (needSpeed) {
            int slot = findSplashBuffPotion(inventory, "swiftness");
            if (slot < 0) slot = findSplashBuffPotion(inventory, "speed");
            if (slot >= 0) potionsToUse.add(slot);
        }
        
        if (needFireResist) {
            int slot = findSplashBuffPotion(inventory, "fire_resistance");
            if (slot >= 0) potionsToUse.add(slot);
        }
        
        // Р•СЃР»Рё РµСЃС‚СЊ Р·РµР»СЊСЏ РґР»СЏ Р±СЂРѕСЃРєР° - РЅР°С‡РёРЅР°РµРј
        if (!potionsToUse.isEmpty()) {
            // Р‘РµСЂС‘Рј РїРµСЂРІРѕРµ Р·РµР»СЊРµ
            int firstSlot = potionsToUse.remove(0);
            
            // РџРµСЂРµРјРµС‰Р°РµРј РІ С…РѕС‚Р±Р°СЂ РµСЃР»Рё РЅСѓР¶РЅРѕ
            if (firstSlot >= 9) {
                ItemStack potion = inventory.getStack(firstSlot);
                ItemStack current = inventory.getStack(8);
                inventory.setStack(firstSlot, current);
                inventory.setStack(8, potion);
                firstSlot = 8;
            }
            
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, firstSlot);
            
            // РЎРѕС…СЂР°РЅСЏРµРј РѕСЃС‚Р°Р»СЊРЅС‹Рµ Р·РµР»СЊСЏ РІ РѕС‡РµСЂРµРґСЊ
            state.potionsToThrow.clear();
            state.potionsToThrow.addAll(potionsToUse);
            
            // РќР°С‡РёРЅР°РµРј Р±СЂРѕСЃРѕРє
            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.buffPotionCooldown = 100; // РљСѓР»РґР°СѓРЅ РїРѕСЃР»Рµ РІСЃРµС… Р±Р°С„С„РѕРІ (5 СЃРµРє)
        }
    }
    
    /**
     * РС‰РµС‚ Р’Р—Р Р«Р’РќРћР• Р±Р°С„С„РѕРІРѕРµ Р·РµР»СЊРµ РїРѕ С‚РёРїСѓ (РґР»СЏ Р±СЂРѕСЃРєР° РїРѕРґ СЃРµР±СЏ)
     */
    private static int findSplashBuffPotion(net.minecraft.entity.player.PlayerInventory inventory, String effectName) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            // РўРѕР»СЊРєРѕ РІР·СЂС‹РІРЅС‹Рµ Р·РµР»СЊСЏ РґР»СЏ Р±СЂРѕСЃРєР°
            if (!(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem)) {
                continue;
            }
            
            var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents == null) continue;
            
            // РџСЂРѕРІРµСЂСЏРµРј РїРѕ ID Р·РµР»СЊСЏ
            var potion = potionContents.potion();
            if (potion.isPresent()) {
                String potionName = potion.get().getIdAsString().toLowerCase();
                if (potionName.contains(effectName)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * РџСЂРѕРІРµСЂСЏРµС‚ РµСЃС‚СЊ Р»Рё СЌС„С„РµРєС‚ СЃ РјРёРЅРёРјР°Р»СЊРЅРѕР№ РґР»РёС‚РµР»СЊРЅРѕСЃС‚СЊСЋ
     */
    private static boolean hasEffect(ServerPlayerEntity bot, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect, int minDuration) {
        var instance = bot.getStatusEffect(effect);
        if (instance == null) return false;
        return instance.getDuration() > minDuration;
    }
    
    /**
     * РС‰РµС‚ Р±Р°С„С„РѕРІРѕРµ Р·РµР»СЊРµ РїРѕ С‚РёРїСѓ
     */
    private static int findBuffPotion(net.minecraft.entity.player.PlayerInventory inventory, String effectName) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            if (!(item instanceof PotionItem) && !(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem)) {
                continue;
            }
            
            var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents == null) continue;
            
            // РџСЂРѕРІРµСЂСЏРµРј РїРѕ ID Р·РµР»СЊСЏ
            var potion = potionContents.potion();
            if (potion.isPresent()) {
                String potionName = potion.get().getIdAsString().toLowerCase();
                if (potionName.contains(effectName)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * РСЃРїРѕР»СЊР·СѓРµС‚ Р±Р°С„С„РѕРІРѕРµ Р·РµР»СЊРµ
     */
    private static boolean useBuffPotion(ServerPlayerEntity bot, BotState state, int slot, MinecraftServer server) {
        var inventory = bot.getInventory();
        ItemStack potionStack = inventory.getStack(slot);
        Item potionItem = potionStack.getItem();
        
        // РџРµСЂРµРјРµС‰Р°РµРј РІ С…РѕС‚Р±Р°СЂ РµСЃР»Рё РЅСѓР¶РЅРѕ
        if (slot >= 9) {
            ItemStack current = inventory.getStack(8);
            inventory.setStack(slot, current);
            inventory.setStack(8, potionStack);
            slot = 8;
        }
        
        // РџРµСЂРµРєР»СЋС‡Р°РµРј СЃР»РѕС‚
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, slot);
        
        if (potionItem instanceof SplashPotionItem || potionItem instanceof LingeringPotionItem) {
            // Р’Р·СЂС‹РІРЅРѕРµ Р·РµР»СЊРµ - РЅР°С‡РёРЅР°РµРј РїСЂРѕС†РµСЃСЃ Р±СЂРѕСЃРєР° РїРѕРґ СЃРµР±СЏ
            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.buffPotionCooldown = 15;
            return true;
        } else if (potionItem instanceof PotionItem) {
            // РћР±С‹С‡РЅРѕРµ Р·РµР»СЊРµ - РїСЊС‘Рј
            state.isEating = true;
            state.eatingTicks = 0;
            state.eatingSlot = slot;
            state.buffPotionCooldown = 10;
            bot.setCurrentHand(Hand.MAIN_HAND);
            return true;
        }
        
        return false;
    }
    
    /**
     * РђРІС‚Рѕ-РµРґР° СЃ РёСЃРїРѕР»СЊР·РѕРІР°РЅРёРµРј РєРѕРјР°РЅРґ Carpet
     */
    private static void handleAutoEat(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        // РџСЂРѕРІРµСЂСЏРµРј РІРєР»СЋС‡РµРЅР° Р»Рё Р°РІС‚Рѕ-РµРґР°
        if (!settings.isAutoEatEnabled()) {
            if (state.isEating) {
                executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
                state.isEating = false;
                state.eatingTicks = 0;
                state.eatingSlot = -1;
            }
            return;
        }
        
        int hunger = bot.getHungerManager().getFoodLevel();
        float health = bot.getHealth();
        float maxHealth = bot.getMaxHealth();
        
        boolean needFood = hunger <= settings.getMinHungerToEat();
        boolean needHealth = health <= maxHealth * 0.5f; // РњРµРЅСЊС€Рµ 50% HP
        boolean criticalHealth = health <= maxHealth * 0.3f; // РњРµРЅСЊС€Рµ 30% HP - СЃСЂРѕС‡РЅРѕ РµСЃС‚СЊ!
        
        // РџСЂРѕРІРµСЂСЏРµРј РѕС‚СЃС‚СѓРїР°РµС‚ Р»Рё Р±РѕС‚ (РёР· BotCombat)
        var combatState = BotCombat.getState(bot.getName().getString());
        boolean isRetreating = combatState.isRetreating;
        
        if (state.isEating) {
            state.eatingTicks++;
            
            // РџР РРќРЈР”РРўР•Р›Р¬РќРћ РґРµСЂР¶РёРј СЃР»РѕС‚ СЃ РµРґРѕР№
            if (state.eatingSlot >= 0 && state.eatingSlot < 9) {
                org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(bot.getInventory(), state.eatingSlot);
            }
            
            // Р”РµСЂР¶РёРј РџРљРњ РЅР°Р¶Р°С‚С‹Рј РЅР°РїСЂСЏРјСѓСЋ (РЅРµ С‡РµСЂРµР· Carpet - СЌС‚Рѕ РЅРµ СЃР±СЂР°СЃС‹РІР°РµС‚ РїСЂРѕРіСЂРµСЃСЃ)
            ItemStack foodStack = bot.getMainHandStack();
            if (foodStack.getItem().getComponents().get(DataComponentTypes.FOOD) != null) {
                // РСЃРїРѕР»СЊР·СѓРµРј РїСЂРµРґРјРµС‚ РЅР°РїСЂСЏРјСѓСЋ РєР°Р¶РґС‹Р№ С‚РёРє
                bot.setCurrentHand(Hand.MAIN_HAND);
            }
            
            // Р•РґР° Р·Р°РЅРёРјР°РµС‚ ~32 С‚РёРєР°, РЅРѕ СЃ СѓС‡С‘С‚РѕРј Р·Р°РґРµСЂР¶РµРє Р¶РґС‘Рј 80 С‚РёРєРѕРІ (4 СЃРµРє)
            if (state.eatingTicks >= 80) {
                executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
                state.isEating = false;
                state.eatingTicks = 0;
                state.eatingSlot = -1;
                state.eatCooldown = 10;
                
                // Р•СЃР»Рё РІСЃС‘ РµС‰С‘ РЅСѓР¶РЅРѕ РµСЃС‚СЊ - РїСЂРѕРґРѕР»Р¶Р°РµРј СЃСЂР°Р·Сѓ
                hunger = bot.getHungerManager().getFoodLevel();
                health = bot.getHealth();
                if (health <= maxHealth * 0.5f || hunger < 18) {
                    state.eatCooldown = 0;
                }
            }
            return;
        }
        
        // Р—РѕР»РѕС‚С‹Рµ СЏР±Р»РѕРєРё РјРѕР¶РЅРѕ РµСЃС‚СЊ РїСЂРё Р»СЋР±РѕРј РіРѕР»РѕРґРµ (РѕРЅРё РґР°СЋС‚ СЌС„С„РµРєС‚С‹)
        // Р•РґРёРј РµСЃР»Рё: РєСЂРёС‚РёС‡РµСЃРєРѕРµ HP, РёР»Рё РЅРёР·РєРѕРµ HP (< 50%), РёР»Рё РїСЂРѕСЃС‚Рѕ РіРѕР»РѕРґРЅС‹
        // РќРµ Р¶РґС‘Рј isRetreating - РµРґРёРј СЃСЂР°Р·Сѓ РєРѕРіРґР° РЅСѓР¶РЅРѕ Р»РµС‡РёС‚СЊСЃСЏ
        boolean shouldEat = criticalHealth || needHealth || needFood;
        
        // РўР°РєР¶Рµ РµРґРёРј Р·РѕР»РѕС‚РѕРµ СЏР±Р»РѕРєРѕ РµСЃР»Рё HP < 50% РґР°Р¶Рµ РїСЂРё РїРѕР»РЅРѕРј РіРѕР»РѕРґРµ
        boolean shouldEatGoldenApple = needHealth && hasGoldenApple(bot.getInventory());
        
        if ((shouldEat || shouldEatGoldenApple) && state.eatCooldown <= 0 && !state.isBlocking) {
            int foodSlot = findBestFood(bot.getInventory(), needHealth || criticalHealth);
            if (foodSlot >= 0) {
                var inventory = bot.getInventory();
                
                // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ СЌС‚Рѕ РґРµР№СЃС‚РІРёС‚РµР»СЊРЅРѕ РµРґР°
                ItemStack foodStack = inventory.getStack(foodSlot);
                if (!foodStack.isEmpty() && foodStack.getItem().getComponents().get(DataComponentTypes.FOOD) != null) {
                    // РџРµСЂРµРјРµС‰Р°РµРј РµРґСѓ РІ С…РѕС‚Р±Р°СЂ СЃР»РѕС‚ 8 (РїРѕСЃР»РµРґРЅРёР№)
                    if (foodSlot >= 9) {
                        ItemStack food = inventory.getStack(foodSlot);
                        ItemStack current = inventory.getStack(8);
                        inventory.setStack(foodSlot, current);
                        inventory.setStack(8, food);
                        foodSlot = 8;
                    }
                    
                    state.eatingSlot = foodSlot;
                    
                    // Switch to food slot
                    org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, foodSlot);
                    
                    // Start eating using HeroBot command for proper slowdown
                    executeCommand(server, bot, "player " + bot.getName().getString() + " use continuous");
                    state.isEating = true;
                    state.eatingTicks = 0;
                }
            }
        }
    }
    
    /**
     * РџСЂРѕРІРµСЂСЏРµС‚ РµСЃС‚СЊ Р»Рё Р·РѕР»РѕС‚РѕРµ СЏР±Р»РѕРєРѕ РІ РёРЅРІРµРЅС‚Р°СЂРµ
     */
    private static boolean hasGoldenApple(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            Item item = inventory.getStack(i).getItem();
            if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
                return true;
            }
        }
        return false;
    }

    
    private static int findBestFood(net.minecraft.entity.player.PlayerInventory inventory, boolean preferGoldenApple) {
        int bestSlot = -1;
        int bestValue = 0;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            int value = getFoodValue(stack.getItem(), preferGoldenApple);
            if (value > bestValue) {
                bestValue = value;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
    
    private static int getFoodValue(Item item, boolean preferGoldenApple) {
        if (preferGoldenApple) {
            if (item == Items.ENCHANTED_GOLDEN_APPLE) return 100;
            if (item == Items.GOLDEN_APPLE) return 90;
        }
        if (item == Items.GOLDEN_CARROT) return 80;
        if (item == Items.COOKED_BEEF) return 70;
        if (item == Items.COOKED_PORKCHOP) return 70;
        if (item == Items.COOKED_MUTTON) return 65;
        if (item == Items.COOKED_SALMON) return 60;
        if (item == Items.COOKED_COD) return 55;
        if (item == Items.COOKED_CHICKEN) return 50;
        if (item == Items.BREAD) return 45;
        if (item == Items.BAKED_POTATO) return 45;
        if (item == Items.APPLE) return 30;
        if (item == Items.CARROT) return 25;
        
        var foodComponent = item.getComponents().get(DataComponentTypes.FOOD);
        if (foodComponent != null) {
            return foodComponent.nutrition() * 5;
        }
        return 0;
    }
    
    /**
     * РђРІС‚Рѕ-С‰РёС‚ - Р±Р»РѕРєРёСЂСѓРµС‚ С‚РѕР»СЊРєРѕ РєРѕРіРґР° РІСЂР°Рі РѕС‡РµРЅСЊ Р±Р»РёР·РєРѕ Рё Р°С‚Р°РєСѓРµС‚
     * РЈС‡РёС‚С‹РІР°РµС‚ РїСЂРёРѕСЂРёС‚РµС‚ С‚РѕС‚РµРјР°
     */
    private static void handleAutoShield(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        var inventory = bot.getInventory();
        int shieldSlot = findShield(inventory);
        if (shieldSlot < 0) {
            state.isBlocking = false;
            return;
        }
        
        // Р•СЃР»Рё РІРєР»СЋС‡РµРЅ РїСЂРёРѕСЂРёС‚РµС‚ С‚РѕС‚РµРјР° - РЅРµ Р·Р°РјРµРЅСЏРµРј С‚РѕС‚РµРј РЅР° С‰РёС‚
        if (settings.isTotemPriority()) {
            ItemStack offhand = inventory.getStack(40);
            if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {
                // РўРѕС‚РµРј РІ offhand - РЅРµ Р±Р»РѕРєРёСЂСѓРµРј
                if (state.isBlocking) {
                    stopBlocking(bot, state, server);
                }
                return;
            }
        }
        
        var combatState = BotCombat.getState(bot.getName().getString());
        var target = combatState.target;
        
        if (target == null || state.shieldCooldown > 0) {
            if (state.isBlocking) {
                stopBlocking(bot, state, server);
            }
            return;
        }
        
        double distance = bot.distanceTo(target);
        boolean isRetreating = combatState.isRetreating;
        float health = bot.getHealth();
        float maxHealth = bot.getMaxHealth();
        boolean lowHealth = health <= maxHealth * 0.3f;
        
        // Р‘Р»РѕРєРёСЂСѓРµРј РµСЃР»Рё:
        // 1. Р’СЂР°Рі Р±Р»РёР·РєРѕ Рё Р°С‚Р°РєСѓРµС‚
        // 2. РћС‚СЃС‚СѓРїР°РµРј СЃ РЅРёР·РєРёРј HP
        boolean shouldBlock = false;
        
        if (distance <= 4.0) {
            // Р‘Р»РѕРєРёСЂСѓРµРј РµСЃР»Рё РІСЂР°Рі Р°С‚Р°РєСѓРµС‚
            if (target instanceof PlayerEntity player && player.handSwinging) {
                shouldBlock = true;
            }
            // РР»Рё РµСЃР»Рё РѕС‚СЃС‚СѓРїР°РµРј СЃ РЅРёР·РєРёРј HP
            if (isRetreating && lowHealth) {
                shouldBlock = true;
            }
        }
        
        // РќРµ Р±Р»РѕРєРёСЂСѓРµРј РµСЃР»Рё РµРґРёРј
        if (state.isEating) {
            shouldBlock = false;
        }
        
        if (shouldBlock && !state.isBlocking) {
            startBlocking(bot, state, shieldSlot, server);
            state.shieldCooldown = 30; // Р‘Р»РѕРєРёСЂСѓРµРј РјР°РєСЃРёРјСѓРј 1.5 СЃРµРєСѓРЅРґС‹
        } else if (!shouldBlock && state.isBlocking) {
            stopBlocking(bot, state, server);
        }
    }
    
    private static void startBlocking(ServerPlayerEntity bot, BotState state, int shieldSlot, MinecraftServer server) {
        var inventory = bot.getInventory();
        
        if (shieldSlot != 40) {
            ItemStack shield = inventory.getStack(shieldSlot);
            ItemStack offhand = inventory.getStack(40);
            
            // РЎРѕС…СЂР°РЅСЏРµРј С‡С‚Рѕ Р±С‹Р»Рѕ РІ offhand (РѕР±С‹С‡РЅРѕ С‚РѕС‚РµРј)
            state.savedOffhandItem = offhand.copy();
            
            // РњРµРЅСЏРµРј РјРµСЃС‚Р°РјРё С‰РёС‚ Рё offhand
            inventory.setStack(shieldSlot, offhand);
            inventory.setStack(40, shield);
        }
        
        // Р‘Р»РѕРєРёСЂСѓРµРј С‡РµСЂРµР· Carpet
        executeCommand(server, bot, "player " + bot.getName().getString() + " use continuous");
        state.isBlocking = true;
    }
    
    private static void stopBlocking(ServerPlayerEntity bot, BotState state, MinecraftServer server) {
        executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
        state.isBlocking = false;
        
        var inventory = bot.getInventory();
        ItemStack currentOffhand = inventory.getStack(40);
        
        // Р•СЃР»Рё РІ offhand С‰РёС‚ Рё Сѓ РЅР°СЃ РµСЃС‚СЊ СЃРѕС…СЂР°РЅС‘РЅРЅС‹Р№ РїСЂРµРґРјРµС‚ (С‚РѕС‚РµРј)
        if (currentOffhand.getItem() == Items.SHIELD && !state.savedOffhandItem.isEmpty()) {
            // РС‰РµРј СЃРІРѕР±РѕРґРЅС‹Р№ СЃР»РѕС‚ РґР»СЏ С‰РёС‚Р°
            int emptySlot = -1;
            for (int i = 0; i < 36; i++) {
                if (inventory.getStack(i).isEmpty()) {
                    emptySlot = i;
                    break;
                }
            }
            
            if (emptySlot >= 0) {
                // Р’РѕР·РІСЂР°С‰Р°РµРј С‰РёС‚ РІ РёРЅРІРµРЅС‚Р°СЂСЊ
                inventory.setStack(emptySlot, currentOffhand.copy());
                // Р’РѕР·РІСЂР°С‰Р°РµРј СЃРѕС…СЂР°РЅС‘РЅРЅС‹Р№ РїСЂРµРґРјРµС‚ (С‚РѕС‚РµРј) РІ offhand
                inventory.setStack(40, state.savedOffhandItem.copy());
            }
            
            // РћС‡РёС‰Р°РµРј СЃРѕС…СЂР°РЅС‘РЅРЅС‹Р№ РїСЂРµРґРјРµС‚
            state.savedOffhandItem = ItemStack.EMPTY;
        }
    }
    
    private static int findShield(net.minecraft.entity.player.PlayerInventory inventory) {
        if (inventory.getStack(40).getItem() == Items.SHIELD) return 40;
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.SHIELD) return i;
        }
        return -1;
    }
    
    /**
     * РСЃРїРѕР»СЊР·РѕРІР°С‚СЊ Wind Charge С‡РµСЂРµР· РєРѕРјР°РЅРґСѓ Carpet
     */
    public static void useWindCharge(ServerPlayerEntity bot, MinecraftServer server) {
        BotState state = getState(bot.getName().getString());
        if (state.windChargeCooldown > 0) return;
        if (state.isEating) return; // РќРµ РїСЂРµСЂС‹РІР°РµРј РµРґСѓ
        
        var inventory = bot.getInventory();
        int slot = findWindCharge(inventory);
        if (slot < 0) return;
        
        // РџРµСЂРµРјРµС‰Р°РµРј РІ С…РѕС‚Р±Р°СЂ
        if (slot >= 9) {
            ItemStack wc = inventory.getStack(slot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(slot, current);
            inventory.setStack(0, wc);
            slot = 0;
        }
        
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, slot);
        
        // РЎРјРѕС‚СЂРёРј РІРЅРёР·
        bot.setPitch(90);
        
        // РСЃРїРѕР»СЊР·СѓРµРј С‡РµСЂРµР· Carpet
        executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
        
        state.windChargeCooldown = 20; // 1 СЃРµРєСѓРЅРґР° РєСѓР»РґР°СѓРЅ
    }
    
    private static int findWindCharge(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.WIND_CHARGE) return i;
        }
        return -1;
    }
    
    /**
     * РЎР±РёС‚СЊ С‰РёС‚ С‚РѕРїРѕСЂРѕРј
     */
    public static boolean tryDisableShield(ServerPlayerEntity bot, Entity target) {
        // РќРµ РїСЂРµСЂС‹РІР°РµРј РµРґСѓ
        BotState state = getState(bot.getName().getString());
        if (state.isEating) return false;
        
        if (!(target instanceof PlayerEntity player)) return false;
        if (!player.isBlocking()) return false;
        
        var inventory = bot.getInventory();
        int axeSlot = findAxe(inventory);
        if (axeSlot < 0) return false;
        
        if (axeSlot >= 9) {
            ItemStack axe = inventory.getStack(axeSlot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(axeSlot, current);
            inventory.setStack(0, axe);
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, 0);
        } else {
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, axeSlot);
        }
        return true;
    }
    
    private static int findAxe(net.minecraft.entity.player.PlayerInventory inventory) {
        int[] priorities = {-1, -1, -1, -1, -1, -1};
        for (int i = 0; i < 36; i++) {
            Item item = inventory.getStack(i).getItem();
            if (item == Items.NETHERITE_AXE) priorities[0] = i;
            else if (item == Items.DIAMOND_AXE) priorities[1] = i;
            else if (item == Items.IRON_AXE) priorities[2] = i;
            else if (item == Items.STONE_AXE) priorities[3] = i;
            else if (item == Items.GOLDEN_AXE) priorities[4] = i;
            else if (item == Items.WOODEN_AXE) priorities[5] = i;
        }
        for (int slot : priorities) {
            if (slot >= 0) return slot;
        }
        return -1;
    }
    
    /**
     * Р’С‹РїРѕР»РЅРёС‚СЊ РєРѕРјР°РЅРґСѓ Carpet
     */
    private static void executeCommand(MinecraftServer server, ServerPlayerEntity bot, String command) {
        try {
            server.getCommandManager().getDispatcher().execute(command, server.getCommandSource());
        } catch (Exception e) {
            // РРіРЅРѕСЂРёСЂСѓРµРј РѕС€РёР±РєРё
        }
    }
    
    /**
     * РџСЂРѕРІРµСЂСЏРµС‚ РµСЃС‚СЊ Р»Рё РµРґР° РІ РёРЅРІРµРЅС‚Р°СЂРµ
     */
    public static boolean hasFood(ServerPlayerEntity bot) {
        var inventory = bot.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                var foodComponent = stack.getItem().getComponents().get(DataComponentTypes.FOOD);
                if (foodComponent != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * РџСЂРѕР±СѓРµС‚ РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊ Р·РµР»СЊРµ РёСЃС†РµР»РµРЅРёСЏ
     * Р’РѕР·РІСЂР°С‰Р°РµС‚ true РµСЃР»Рё РЅР°С‡Р°Р» РїРёС‚СЊ/Р±СЂРѕСЃРёР» Р·РµР»СЊРµ
     */
    public static boolean tryUseHealingPotion(ServerPlayerEntity bot, MinecraftServer server) {
        BotState state = getState(bot.getName().getString());
        if (state.isEating) return false; // РЈР¶Рµ С‡С‚Рѕ-С‚Рѕ РґРµР»Р°РµРј
        if (state.potionCooldown > 0) return false; // РљСѓР»РґР°СѓРЅ РЅР° Р·РµР»СЊСЏ
        
        var inventory = bot.getInventory();
        
        // РС‰РµРј Р·РµР»СЊРµ РёСЃС†РµР»РµРЅРёСЏ (РѕР±С‹С‡РЅРѕРµ РёР»Рё РІР·СЂС‹РІРЅРѕРµ)
        int potionSlot = findHealingPotion(inventory);
        if (potionSlot < 0) return false;
        
        ItemStack potionStack = inventory.getStack(potionSlot);
        Item potionItem = potionStack.getItem();
        
        // РџРµСЂРµРјРµС‰Р°РµРј РІ С…РѕС‚Р±Р°СЂ РµСЃР»Рё РЅСѓР¶РЅРѕ
        if (potionSlot >= 9) {
            ItemStack current = inventory.getStack(8);
            inventory.setStack(potionSlot, current);
            inventory.setStack(8, potionStack);
            potionSlot = 8;
        }
        
        // РџРµСЂРµРєР»СЋС‡Р°РµРј СЃР»РѕС‚
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, potionSlot);
        
        if (potionItem instanceof SplashPotionItem || potionItem instanceof LingeringPotionItem) {
            // Р’Р·СЂС‹РІРЅРѕРµ Р·РµР»СЊРµ - РЅР°С‡РёРЅР°РµРј РїСЂРѕС†РµСЃСЃ Р±СЂРѕСЃРєР° РїРѕРґ СЃРµР±СЏ
            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.potionCooldown = 10;
            return true;
        } else if (potionItem instanceof PotionItem) {
            // РћР±С‹С‡РЅРѕРµ Р·РµР»СЊРµ - РїСЊС‘Рј
            state.isEating = true;
            state.eatingTicks = 0;
            state.eatingSlot = potionSlot;
            state.potionCooldown = 5; // 5 С‚РёРєРѕРІ РїРѕСЃР»Рµ РїРёС‚СЊСЏ
            bot.setCurrentHand(Hand.MAIN_HAND);
            return true;
        }
        
        return false;
    }
    
    /**
     * РС‰РµС‚ Р·РµР»СЊРµ РёСЃС†РµР»РµРЅРёСЏ РІ РёРЅРІРµРЅС‚Р°СЂРµ
     */
    private static int findHealingPotion(net.minecraft.entity.player.PlayerInventory inventory) {
        // РџСЂРёРѕСЂРёС‚РµС‚: РІР·СЂС‹РІРЅРѕРµ > РѕР±С‹С‡РЅРѕРµ
        int splashSlot = -1;
        int normalSlot = -1;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            
            // РџСЂРѕРІРµСЂСЏРµРј С‡С‚Рѕ СЌС‚Рѕ Р·РµР»СЊРµ РёСЃС†РµР»РµРЅРёСЏ
            if (isHealingPotion(stack)) {
                if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
                    if (splashSlot < 0) splashSlot = i;
                } else if (item instanceof PotionItem) {
                    if (normalSlot < 0) normalSlot = i;
                }
            }
        }
        
        // РџСЂРµРґРїРѕС‡РёС‚Р°РµРј РІР·СЂС‹РІРЅРѕРµ (Р±С‹СЃС‚СЂРµРµ)
        return splashSlot >= 0 ? splashSlot : normalSlot;
    }
    
    /**
     * РџСЂРѕРІРµСЂСЏРµС‚ СЏРІР»СЏРµС‚СЃСЏ Р»Рё Р·РµР»СЊРµ Р·РµР»СЊРµРј РёСЃС†РµР»РµРЅРёСЏ
     */
    private static boolean isHealingPotion(ItemStack stack) {
        var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) return false;
        
        // РџСЂРѕРІРµСЂСЏРµРј СЌС„С„РµРєС‚С‹ Р·РµР»СЊСЏ
        for (var effect : potionContents.getEffects()) {
            var effectType = effect.getEffectType().value();
            String effectName = effectType.toString().toLowerCase();
            if (effectName.contains("healing") || effectName.contains("instant_health")) {
                return true;
            }
        }
        
        // РўР°РєР¶Рµ РїСЂРѕРІРµСЂСЏРµРј РїРѕ ID Р·РµР»СЊСЏ
        var potion = potionContents.potion();
        if (potion.isPresent()) {
            String potionName = potion.get().getIdAsString().toLowerCase();
            if (potionName.contains("healing") || potionName.contains("health")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * РђРІС‚Рѕ-СЂРµРјРѕРЅС‚ Р±СЂРѕРЅРё СЃ Mending С‡РµСЂРµР· XP Р±СѓС‚С‹Р»РєРё
     * РџСЂРѕРІРµСЂСЏРµС‚ РїСЂРѕС‡РЅРѕСЃС‚СЊ Р±СЂРѕРЅРё Рё РёСЃРїРѕР»СЊР·СѓРµС‚ XP Р±СѓС‚С‹Р»РєРё РєРѕРіРґР° РЅСѓР¶РЅРѕ
     * Р’РѕР·РІСЂР°С‰Р°РµС‚ true РµСЃР»Рё Р±РѕС‚ С‡РёРЅРёС‚СЃСЏ (РЅСѓР¶РЅРѕ РѕС‚СЃС‚СѓРїР°С‚СЊ)
     */
    private static boolean handleAutoMend(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        var inventory = bot.getInventory();
        
        // РџСЂРѕРІРµСЂСЏРµРј РµСЃС‚СЊ Р»Рё XP Р±СѓС‚С‹Р»РєРё
        int xpBottleSlot = findXpBottle(inventory);
        if (xpBottleSlot < 0) {
            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false; // РќРµС‚ XP Р±СѓС‚С‹Р»РѕРє
        }
        
        // РџСЂРѕРІРµСЂСЏРµРј РєР°Р¶РґС‹Р№ СЃР»РѕС‚ Р±СЂРѕРЅРё Рё СЃС‡РёС‚Р°РµРј СЃРєРѕР»СЊРєРѕ СѓСЂРѕРЅР° РЅСѓР¶РЅРѕ РїРѕС‡РёРЅРёС‚СЊ
        int totalDamageToRepair = 0;
        int itemsNeedingRepair = 0;
        
        for (int armorSlot = 36; armorSlot < 40; armorSlot++) {
            ItemStack armorPiece = inventory.getStack(armorSlot);
            if (armorPiece.isEmpty()) continue;
            
            // РџСЂРѕРІРµСЂСЏРµРј РµСЃС‚СЊ Р»Рё Mending РЅР° Р±СЂРѕРЅРµ
            if (!hasMendingEnchantment(armorPiece)) continue;
            
            // РџСЂРѕРІРµСЂСЏРµРј РїСЂРѕС‡РЅРѕСЃС‚СЊ
            int maxDamage = armorPiece.getMaxDamage();
            int currentDamage = armorPiece.getDamage();
            double durabilityPercent = 1.0 - ((double) currentDamage / maxDamage);
            
            // Р•СЃР»Рё РїСЂРѕС‡РЅРѕСЃС‚СЊ РЅРёР¶Рµ РїРѕСЂРѕРіР° - РЅСѓР¶РµРЅ СЂРµРјРѕРЅС‚
            if (durabilityPercent < settings.getMendDurabilityThreshold()) {
                // РЎС‡РёС‚Р°РµРј СЃРєРѕР»СЊРєРѕ РЅСѓР¶РЅРѕ РїРѕС‡РёРЅРёС‚СЊ РґРѕ 90%
                int targetDamage = (int) (maxDamage * 0.1); // 90% = 10% СѓСЂРѕРЅР°
                int damageToRepair = currentDamage - targetDamage;
                if (damageToRepair > 0) {
                    totalDamageToRepair += damageToRepair;
                    itemsNeedingRepair++;
                }
            }
        }
        
        if (totalDamageToRepair <= 0) {
            // Р’СЃСЏ Р±СЂРѕРЅСЏ РїРѕС‡РёРЅРµРЅР°!
            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false;
        }
        
        // Р•СЃР»Рё С‚РѕР»СЊРєРѕ РЅР°С‡РёРЅР°РµРј С‡РёРЅРёС‚СЊСЃСЏ - СЂР°СЃСЃС‡РёС‚С‹РІР°РµРј СЃРєРѕР»СЊРєРѕ Р±СѓС‚С‹Р»РѕРє РЅСѓР¶РЅРѕ РћР”РРќ Р РђР—
        if (!state.isMending) {
            // Р РµР°Р»СЊРЅС‹Рµ РґР°РЅРЅС‹Рµ: РЅРµР·РµСЂРёС‚РѕРІС‹Р№ РЅР°РіСЂСѓРґРЅРёРє 0в†’95% = ~20 Р±СѓС‚С‹Р»РѕРє РґР»СЏ ~562 СѓСЂРѕРЅР°
            // Р­С‚Рѕ Р·РЅР°С‡РёС‚ ~28 СѓСЂРѕРЅР° РЅР° Р±СѓС‚С‹Р»РєСѓ
            // Р¤РѕСЂРјСѓР»Р°: totalDamageToRepair / 28
            state.xpBottlesNeeded = (totalDamageToRepair / 28) + 2; // +2 РґР»СЏ Р·Р°РїР°СЃР°
            if (state.xpBottlesNeeded < 5) state.xpBottlesNeeded = 5; // РњРёРЅРёРјСѓРј 5 Р±СѓС‚С‹Р»РѕРє
            state.xpBottlesThrown = 0;
        }
        
        // Р‘Р РћРќРЇ РЎР›РћРњРђРќРђ - РћРўРЎРўРЈРџРђР•Рњ Р Р§РРќРРњРЎРЇ!
        state.isMending = true;
        
        // РџРѕР»СѓС‡Р°РµРј С†РµР»СЊ РёР· BotCombat
        var combatState = BotCombat.getState(bot.getName().getString());
        Entity target = combatState.target;
        
        // РћС‚СЃС‚СѓРїР°РµРј РѕС‚ РІСЂР°РіР° РµСЃР»Рё РѕРЅ РµСЃС‚СЊ (СЃРєРѕСЂРѕСЃС‚СЊ 1.3 = Р±С‹СЃС‚СЂРѕ!)
        if (target != null) {
            BotNavigation.lookAway(bot, target);
            BotNavigation.moveAway(bot, target, 1.3); // 1.3 = Р±С‹СЃС‚СЂРѕ СЃ bhop!
        }
        
        // РџСЂРѕРІРµСЂСЏРµРј Р±СЂРѕСЃРёР»Рё Р»Рё СѓР¶Рµ РґРѕСЃС‚Р°С‚РѕС‡РЅРѕ Р±СѓС‚С‹Р»РѕРє
        if (state.xpBottlesThrown >= state.xpBottlesNeeded) {
            // Р—Р°РєРѕРЅС‡РёР»Рё Р±СЂРѕСЃР°С‚СЊ - РІС‹С…РѕРґРёРј РёР· СЂРµР¶РёРјР° РїРѕС‡РёРЅРєРё
            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false;
        }
        
        // РџРµСЂРµРјРµС‰Р°РµРј XP Р±СѓС‚С‹Р»РєСѓ РІ С…РѕС‚Р±Р°СЂ РµСЃР»Рё РЅСѓР¶РЅРѕ
        if (xpBottleSlot >= 9) {
            ItemStack xpBottle = inventory.getStack(xpBottleSlot);
            ItemStack current = inventory.getStack(8);
            inventory.setStack(xpBottleSlot, current);
            inventory.setStack(8, xpBottle);
            xpBottleSlot = 8;
        }
        
        // РџРµСЂРµРєР»СЋС‡Р°РµРј СЃР»РѕС‚ РЅР° XP Р±СѓС‚С‹Р»РєСѓ
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, xpBottleSlot);
        
        // РЎРјРѕС‚СЂРёРј РјР°РєСЃРёРјР°Р»СЊРЅРѕ РІРЅРёР·
        bot.setPitch(90);
        
        // РљРР”РђР•Рњ Р‘РЈРўР«Р›РљРЈ РљРђР–Р”Р«Р™ РўРРљ!
        executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
        
        state.xpBottlesThrown++;
        
        return true; // Р’РѕР·РІСЂР°С‰Р°РµРј true - Р±РѕС‚ С‡РёРЅРёС‚СЃСЏ
    }
    
    /**
     * РџСЂРѕРІРµСЂСЏРµС‚ РµСЃС‚СЊ Р»Рё Р·Р°С‡Р°СЂРѕРІР°РЅРёРµ Mending РЅР° РїСЂРµРґРјРµС‚Рµ
     */
    private static boolean hasMendingEnchantment(ItemStack stack) {
        var enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null) return false;
        
        // РџСЂРѕРІРµСЂСЏРµРј РІСЃРµ Р·Р°С‡Р°СЂРѕРІР°РЅРёСЏ
        for (var entry : enchantments.getEnchantments()) {
            String enchantName = entry.getIdAsString().toLowerCase();
            if (enchantName.contains("mending")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * РС‰РµС‚ XP Р±СѓС‚С‹Р»РєСѓ РІ РёРЅРІРµРЅС‚Р°СЂРµ
     */
    private static int findXpBottle(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                return i;
            }
        }
        return -1;
    }
}
