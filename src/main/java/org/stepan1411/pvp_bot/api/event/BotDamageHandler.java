package org.stepan1411.pvp_bot.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;


@FunctionalInterface
public interface BotDamageHandler {
    
    boolean onBotDamage(ServerPlayerEntity bot, Entity attacker, float damage);
}
