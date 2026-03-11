package org.stepan1411.pvp_bot.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;


@FunctionalInterface
public interface BotAttackHandler {
    
    boolean onBotAttack(ServerPlayerEntity bot, Entity target);
}
