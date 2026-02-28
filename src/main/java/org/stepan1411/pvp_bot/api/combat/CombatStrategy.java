package org.stepan1411.pvp_bot.api.combat;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.bot.BotSettings;

/**
 * Interface for custom combat strategies
 * Implement this to create your own combat behavior for bots
 */
public interface CombatStrategy {
    
    /**
     * Get the name of this combat strategy
     * @return Strategy name
     */
    String getName();
    
    /**
     * Get the priority of this strategy (higher = executed first)
     * @return Priority value
     */
    int getPriority();
    
    /**
     * Check if this strategy can be used in current situation
     * @param bot The bot entity
     * @param target The target entity
     * @param settings Bot settings
     * @return true if strategy can be used
     */
    boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings);
    
    /**
     * Execute the combat strategy
     * @param bot The bot entity
     * @param target The target entity
     * @param settings Bot settings
     * @param server Minecraft server instance
     * @return true if strategy was executed successfully
     */
    boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server);
    
    /**
     * Get cooldown in ticks before this strategy can be used again
     * @return Cooldown in ticks
     */
    default int getCooldown() {
        return 20; // 1 second default
    }
}
