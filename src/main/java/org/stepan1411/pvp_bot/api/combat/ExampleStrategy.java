package org.stepan1411.pvp_bot.api.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.stepan1411.pvp_bot.bot.BotSettings;


public class ExampleStrategy implements CombatStrategy {
    
    @Override
    public String getName() {
        return "ExampleLowHealthBoost";
    }
    
    @Override
    public int getPriority() {
        return 150;
    }
    
    @Override
    public String getDescription() {
        return "Applies strength boost when bot health is low";
    }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {

        float healthPercent = bot.getHealth() / bot.getMaxHealth();
        

        if (bot.hasStatusEffect(StatusEffects.STRENGTH)) {
            return false;
        }
        

        return healthPercent < 0.5f && bot.distanceTo(target) < 8.0;
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
        try {

            bot.addStatusEffect(new StatusEffectInstance(
                StatusEffects.STRENGTH, 
                200,
                1
            ));
            

            bot.sendMessage(Text.literal("§c[Strategy] Low health boost activated!"));
            

            System.out.println("[PVP_BOT_API] ExampleStrategy executed for " + bot.getName().getString());
            
            return true;
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error in ExampleStrategy: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public int getCooldown() {
        return 300;
    }
}