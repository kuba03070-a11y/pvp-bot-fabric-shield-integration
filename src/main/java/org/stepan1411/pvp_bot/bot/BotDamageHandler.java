package org.stepan1411.pvp_bot.bot;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BotDamageHandler {
    
    public static void register() {
        // Р РµРіРёСЃС‚СЂРёСЂСѓРµРј РѕР±СЂР°Р±РѕС‚С‡РёРє СѓСЂРѕРЅР° С‡РµСЂРµР· Fabric API
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // РџСЂРѕРІРµСЂСЏРµРј, СЏРІР»СЏРµС‚СЃСЏ Р»Рё СЌС‚Рѕ ServerPlayerEntity
            if (entity instanceof ServerPlayerEntity player) {
                String playerName = player.getName().getString();
                
                // РџСЂРѕРІРµСЂСЏРµРј, СЏРІР»СЏРµС‚СЃСЏ Р»Рё СЌС‚РѕС‚ РёРіСЂРѕРє РЅР°С€РёРј Р±РѕС‚РѕРј
                if (BotManager.getAllBots().contains(playerName)) {
                    // Р’С‹Р·С‹РІР°РµРј РѕР±СЂР°Р±РѕС‚С‡РёРє Р±РѕСЏ
                    BotCombat.onBotDamaged(player, source);
                }
            }
            
            // Р’РѕР·РІСЂР°С‰Р°РµРј true С‡С‚РѕР±С‹ СѓСЂРѕРЅ РїСЂРѕС€С‘Р»
            return true;
        });
    }
}
