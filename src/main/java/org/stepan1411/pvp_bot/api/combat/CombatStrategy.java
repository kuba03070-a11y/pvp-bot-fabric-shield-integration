package org.stepan1411.pvp_bot.api.combat;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.bot.BotSettings;


public interface CombatStrategy {
    
    
    String getName();
    
    
    int getPriority();
    
    
    boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings);
    
    
    boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server);
    
    
    default int getCooldown() {
        return 20;
    }
    
    
    default String getDescription() {
        return getName();
    }
    
    
    default boolean isEnabledByDefault() {
        return true;
    }
}
