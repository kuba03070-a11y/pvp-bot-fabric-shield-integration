package org.stepan1411.pvp_bot.api.event;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handler for bot spawn events
 */
@FunctionalInterface
public interface BotSpawnHandler {
    /**
     * Called when a bot spawns
     * @param bot The bot that spawned
     */
    void onBotSpawn(ServerPlayerEntity bot);
}
