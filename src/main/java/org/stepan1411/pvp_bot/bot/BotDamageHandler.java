package org.stepan1411.pvp_bot.bot;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BotDamageHandler {
    
    public static void register() {

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

            if (entity instanceof ServerPlayerEntity player) {
                String playerName = player.getName().getString();
                

                if (BotManager.getAllBots().contains(playerName)) {

                    try {
                        Entity attacker = source.getAttacker();
                        boolean cancelled = org.stepan1411.pvp_bot.api.BotAPIIntegration.fireDamageEvent(player, attacker, amount);
                        if (cancelled) {
                            return false;
                        }
                    } catch (Exception e) {
                        System.err.println("[PVP_BOT_API] Error firing damage event: " + e.getMessage());
                    }
                    

                    BotCombat.onBotDamaged(player, source);
                }
            }
            

            return true;
        });
    }
}
