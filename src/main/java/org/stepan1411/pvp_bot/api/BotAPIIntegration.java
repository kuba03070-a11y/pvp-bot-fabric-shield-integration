package org.stepan1411.pvp_bot.api;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;


public class BotAPIIntegration {
    
    
    public static void fireSpawnEvent(ServerPlayerEntity bot) {
        try {
            PvpBotAPI.getEventManager().fireSpawnEvent(bot);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing spawn event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    public static void fireDeathEvent(ServerPlayerEntity bot) {
        try {
            PvpBotAPI.getEventManager().fireDeathEvent(bot);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing death event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    public static boolean fireAttackEvent(ServerPlayerEntity bot, Entity target) {
        try {
            return PvpBotAPI.getEventManager().fireAttackEvent(bot, target);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing attack event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    public static boolean fireDamageEvent(ServerPlayerEntity bot, Entity attacker, float damage) {
        try {
            return PvpBotAPI.getEventManager().fireDamageEvent(bot, attacker, damage);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing damage event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    public static void fireTickEvent(ServerPlayerEntity bot) {
        try {
            PvpBotAPI.getEventManager().fireTickEvent(bot);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing tick event: " + e.getMessage());

        }
    }
}
