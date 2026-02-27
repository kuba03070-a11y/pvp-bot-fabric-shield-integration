package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class BotEquipment {

    public static void autoEquip(ServerPlayerEntity bot) {
        // РќР• СЌРєРёРїРёСЂСѓРµРј РѕСЂСѓР¶РёРµ РµСЃР»Рё Р±РѕС‚ РµСЃС‚!
        var utilsState = BotUtils.getState(bot.getName().getString());
        if (utilsState.isEating) {
            return;
        }
        
        BotSettings settings = BotSettings.get();
        
        if (settings.isAutoEquipArmor()) {
            equipBestArmor(bot);
        }
        if (settings.isAutoEquipWeapon()) {
            equipBestWeapon(bot);
        }
    }

    private static void equipBestArmor(ServerPlayerEntity bot) {
        equipBestForSlot(bot, EquipmentSlot.HEAD);
        equipBestForSlot(bot, EquipmentSlot.CHEST);
        equipBestForSlot(bot, EquipmentSlot.LEGS);
        equipBestForSlot(bot, EquipmentSlot.FEET);
    }

    private static void equipBestForSlot(ServerPlayerEntity bot, EquipmentSlot slot) {
        BotSettings settings = BotSettings.get();
        var inventory = bot.getInventory();
        
        // РЁР°Рі 1: РќР°Р№С‚Рё Р»СѓС‡С€СѓСЋ Р±СЂРѕРЅСЋ РґР»СЏ СЌС‚РѕРіРѕ СЃР»РѕС‚Р° (РІРєР»СЋС‡Р°СЏ С‚РµРєСѓС‰СѓСЋ СЌРєРёРїРёСЂРѕРІР°РЅРЅСѓСЋ)
        ItemStack currentArmor = bot.getEquippedStack(slot);
        double currentValue = getArmorValue(currentArmor, slot);
        
        int bestInvSlot = -1;
        double bestValue = currentValue;
        
        // РС‰РµРј Р»СѓС‡С€СѓСЋ Р±СЂРѕРЅСЋ РІ РёРЅРІРµРЅС‚Р°СЂРµ
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (!isArmorForSlot(stack.getItem(), slot)) continue;
            
            double value = getArmorValue(stack, slot);
            if (value > bestValue) {
                bestValue = value;
                bestInvSlot = i;
            }
        }
        
        // РЁР°Рі 2: Р­РєРёРїРёСЂСѓРµРј Р»СѓС‡С€СѓСЋ Р±СЂРѕРЅСЋ РµСЃР»Рё РѕРЅР° РІ РёРЅРІРµРЅС‚Р°СЂРµ
        if (bestInvSlot >= 0) {
            ItemStack newArmor = inventory.getStack(bestInvSlot).copy();
            ItemStack oldArmor = currentArmor.copy();
            
            // РќР°РґРµРІР°РµРј РЅРѕРІСѓСЋ Р±СЂРѕРЅСЋ
            bot.equipStack(slot, newArmor);
            inventory.setStack(bestInvSlot, ItemStack.EMPTY);
            
            // РЎС‚Р°СЂСѓСЋ Р±СЂРѕРЅСЋ РєР»Р°РґС‘Рј РІ РёРЅРІРµРЅС‚Р°СЂСЊ (РµСЃР»Рё Р±С‹Р»Р°)
            if (!oldArmor.isEmpty()) {
                inventory.setStack(bestInvSlot, oldArmor);
            }
            
            currentArmor = newArmor;
            currentValue = bestValue;
        }

        
        // РЁР°Рі 3: Р’С‹Р±СЂР°СЃС‹РІР°РµРј С…СѓРґС€СѓСЋ Р±СЂРѕРЅСЋ (С‚РѕР»СЊРєРѕ РµСЃР»Рё РІРєР»СЋС‡РµРЅРѕ)
        if (settings.isDropWorseArmor()) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (stack.isEmpty()) continue;
                if (!isArmorForSlot(stack.getItem(), slot)) continue;
                
                double value = getArmorValue(stack, slot);
                
                // Р’С‹Р±СЂР°СЃС‹РІР°РµРј С‚РѕР»СЊРєРѕ РµСЃР»Рё С…СѓР¶Рµ С‚РµРєСѓС‰РµР№ СЌРєРёРїРёСЂРѕРІР°РЅРЅРѕР№
                if (value < currentValue) {
                    dropItemBackward(bot, stack);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * Р’С‹Р±СЂР°СЃС‹РІР°РµС‚ РїСЂРµРґРјРµС‚ РЅР°Р·Р°Рґ РёР»Рё РІР±РѕРє РѕС‚ Р±РѕС‚Р° (РєР°Рє РЅР°Р¶Р°С‚РёРµ Q СЃ РїРѕРІРѕСЂРѕС‚РѕРј)
     */
    private static void dropItemBackward(ServerPlayerEntity bot, ItemStack stack) {
        if (stack.isEmpty()) return;
        
        BotSettings settings = BotSettings.get();
        double distance = settings.getDropDistance();
        
        // РЎРѕС…СЂР°РЅСЏРµРј С‚РµРєСѓС‰РёР№ СѓРіРѕР»
        float oldYaw = bot.getYaw();
        float oldHeadYaw = bot.getHeadYaw();
        
        // РџРѕРІРѕСЂР°С‡РёРІР°РµРј РЅР°Р·Р°Рґ РёР»Рё РІР±РѕРє (90-270 РіСЂР°РґСѓСЃРѕРІ РѕС‚ С‚РµРєСѓС‰РµРіРѕ РЅР°РїСЂР°РІР»РµРЅРёСЏ)
        float turnAngle = 90 + (float)(Math.random() * 180); // РѕС‚ 90 РґРѕ 270 РіСЂР°РґСѓСЃРѕРІ
        float newYaw = oldYaw + turnAngle;
        
        bot.setYaw(newYaw);
        bot.setHeadYaw(newYaw);
        
        // Р’С‹Р±СЂР°СЃС‹РІР°РµРј РєР°Рє РїСЂРё РЅР°Р¶Р°С‚РёРё Q (throwRandomly=false, retainOwnership=true)
        ItemEntity dropped = bot.dropItem(stack.copy(), false, true);
        
        if (dropped != null) {
            // РЎРєРѕСЂРѕСЃС‚СЊ РІ РЅР°РїСЂР°РІР»РµРЅРёРё РІР·РіР»СЏРґР°
            double yawRad = Math.toRadians(newYaw);
            double speed = 0.3 * distance;
            dropped.setVelocity(
                -Math.sin(yawRad) * speed,
                0.2,
                Math.cos(yawRad) * speed
            );
            dropped.setPickupDelay(60); // 3 СЃРµРєСѓРЅРґС‹ Р·Р°РґРµСЂР¶РєРё
        }
        
        // Р’РѕР·РІСЂР°С‰Р°РµРј СѓРіРѕР» РѕР±СЂР°С‚РЅРѕ
        bot.setYaw(oldYaw);
        bot.setHeadYaw(oldHeadYaw);
    }

    private static boolean isArmorForSlot(Item item, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> item == Items.NETHERITE_HELMET || item == Items.DIAMOND_HELMET ||
                         item == Items.IRON_HELMET || item == Items.CHAINMAIL_HELMET ||
                         item == Items.GOLDEN_HELMET || item == Items.LEATHER_HELMET ||
                         item == Items.TURTLE_HELMET;
            case CHEST -> item == Items.NETHERITE_CHESTPLATE || item == Items.DIAMOND_CHESTPLATE ||
                          item == Items.IRON_CHESTPLATE || item == Items.CHAINMAIL_CHESTPLATE ||
                          item == Items.GOLDEN_CHESTPLATE || item == Items.LEATHER_CHESTPLATE ||
                          item == Items.ELYTRA;
            case LEGS -> item == Items.NETHERITE_LEGGINGS || item == Items.DIAMOND_LEGGINGS ||
                         item == Items.IRON_LEGGINGS || item == Items.CHAINMAIL_LEGGINGS ||
                         item == Items.GOLDEN_LEGGINGS || item == Items.LEATHER_LEGGINGS;
            case FEET -> item == Items.NETHERITE_BOOTS || item == Items.DIAMOND_BOOTS ||
                         item == Items.IRON_BOOTS || item == Items.CHAINMAIL_BOOTS ||
                         item == Items.GOLDEN_BOOTS || item == Items.LEATHER_BOOTS;
            default -> false;
        };
    }

    private static double getArmorValue(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        
        if (item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE ||
            item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS) return 100;
        if (item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE ||
            item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS) return 80;
        if (item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE ||
            item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS) return 60;
        if (item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE ||
            item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS) return 50;
        if (item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE ||
            item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS) return 40;
        if (item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE ||
            item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS) return 20;
        if (item == Items.TURTLE_HELMET) return 55;
        if (item == Items.ELYTRA) return 10;
        
        return 0;
    }


    private static void equipBestWeapon(ServerPlayerEntity bot) {
        BotSettings settings = BotSettings.get();
        var inventory = bot.getInventory();
        
        // РЁР°Рі 1: РќР°Р№С‚Рё Р»СѓС‡С€РµРµ РѕСЂСѓР¶РёРµ СЃ СѓС‡С‘С‚РѕРј preferSword
        int bestSlotIndex = -1;
        double bestScore = 0;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            double score = getWeaponScore(stack, settings.isPreferSword());
            if (score > bestScore) {
                bestScore = score;
                bestSlotIndex = i;
            }
        }
        
        // РЁР°Рі 2: Р­РєРёРїРёСЂСѓРµРј Р»СѓС‡С€РµРµ РѕСЂСѓР¶РёРµ
        if (bestSlotIndex >= 0) {
            if (bestSlotIndex >= 9) {
                // РџРµСЂРµРјРµС‰Р°РµРј РІ С…РѕС‚Р±Р°СЂ СЃР»РѕС‚ 0
                ItemStack weapon = inventory.getStack(bestSlotIndex);
                ItemStack slot0 = inventory.getStack(0);
                inventory.setStack(bestSlotIndex, slot0);
                inventory.setStack(0, weapon);
                bestSlotIndex = 0;
            }
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, bestSlotIndex);
        }
        
        // РЁР°Рі 3: Р’С‹Р±СЂР°СЃС‹РІР°РµРј С…СѓРґС€РµРµ РѕСЂСѓР¶РёРµ
        if (settings.isDropWorseWeapons() && bestScore > 0) {
            for (int i = 0; i < 36; i++) {
                if (i == bestSlotIndex) continue;
                
                ItemStack stack = inventory.getStack(i);
                if (stack.isEmpty()) continue;
                
                double score = getWeaponScore(stack, settings.isPreferSword());
                if (score > 0 && score < bestScore) {
                    dropItemBackward(bot, stack);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * РџРѕР»СѓС‡РёС‚СЊ "РѕС‡РєРё" РѕСЂСѓР¶РёСЏ СЃ СѓС‡С‘С‚РѕРј preferSword
     * Р•СЃР»Рё preferSword = true, РјРµС‡Рё РїРѕР»СѓС‡Р°СЋС‚ Р±РѕРЅСѓСЃ +5 Рє РѕС‡РєР°Рј
     */
    private static double getWeaponScore(ItemStack stack, boolean preferSword) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        
        double baseDamage = getWeaponDamage(stack);
        if (baseDamage == 0) return 0;
        
        // Р•СЃР»Рё РїСЂРµРґРїРѕС‡РёС‚Р°РµРј РјРµС‡ - РґР°С‘Рј РјРµС‡Р°Рј Р±РѕРЅСѓСЃ
        if (preferSword && isSword(item)) {
            return baseDamage + 5; // РњРµС‡ РІСЃРµРіРґР° Р±СѓРґРµС‚ РІС‹Р±СЂР°РЅ РµСЃР»Рё РµСЃС‚СЊ
        }
        
        return baseDamage;
    }
    
    /**
     * РџСЂРѕРІРµСЂСЏРµС‚, СЏРІР»СЏРµС‚СЃСЏ Р»Рё РїСЂРµРґРјРµС‚ РјРµС‡РѕРј
     */
    private static boolean isSword(Item item) {
        return item == Items.NETHERITE_SWORD || 
               item == Items.DIAMOND_SWORD || 
               item == Items.IRON_SWORD || 
               item == Items.GOLDEN_SWORD || 
               item == Items.STONE_SWORD || 
               item == Items.WOODEN_SWORD;
    }

    private static double getWeaponDamage(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        
        if (item == Items.NETHERITE_SWORD) return 8;
        if (item == Items.NETHERITE_AXE) return 10;
        if (item == Items.DIAMOND_SWORD) return 7;
        if (item == Items.DIAMOND_AXE) return 9;
        if (item == Items.IRON_SWORD) return 6;
        if (item == Items.IRON_AXE) return 9;
        if (item == Items.STONE_SWORD) return 5;
        if (item == Items.STONE_AXE) return 9;
        if (item == Items.GOLDEN_SWORD) return 4;
        if (item == Items.GOLDEN_AXE) return 7;
        if (item == Items.WOODEN_SWORD) return 4;
        if (item == Items.WOODEN_AXE) return 7;
        if (item == Items.TRIDENT) return 9;
        if (item == Items.MACE) return 6;
        
        return 0;
    }
}
