package org.stepan1411.pvp_bot.api.event;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handler for bot tick events
 */
@FunctionalInterface
public interface BotTickHandler {
    /**
     * Called every tick for each bot
     * @param bot The bot being ticked
     */
    void onBotTick(ServerPlayerEntity bot);
}
