package org.stepan1411.pvp_bot.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handler for bot attack events
 */
@FunctionalInterface
public interface BotAttackHandler {
    /**
     * Called when a bot attacks an entity
     * @param bot The bot that attacks
     * @param target The target entity
     * @return true to cancel the attack
     */
    boolean onBotAttack(ServerPlayerEntity bot, Entity target);
}
