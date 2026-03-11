package org.stepan1411.pvp_bot.api.event;

import net.minecraft.server.network.ServerPlayerEntity;


@FunctionalInterface
public interface BotTickHandler {
    
    void onBotTick(ServerPlayerEntity bot);
}
