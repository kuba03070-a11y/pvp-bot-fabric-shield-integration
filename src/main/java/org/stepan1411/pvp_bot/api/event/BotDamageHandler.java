package org.stepan1411.pvp_bot.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handler for bot damage events
 */
@FunctionalInterface
public interface BotDamageHandler {
    /**
     * Called when a bot receives damage
     * @param bot The bot that received damage
     * @param attacker The attacker entity (can be null)
     * @param damage The damage amount
     * @return true to cancel the damage
     */
    boolean onBotDamage(ServerPlayerEntity bot, Entity attacker, float damage);
}
