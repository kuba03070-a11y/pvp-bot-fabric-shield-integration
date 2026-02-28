package org.stepan1411.pvp_bot.api.event;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handler for bot death events
 */
@FunctionalInterface
public interface BotDeathHandler {
    /**
     * Called when a bot dies
     * @param bot The bot that died
     */
    void onBotDeath(ServerPlayerEntity bot);
}
