package org.stepan1411.pvp_bot.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;


public class BotEventManager {
    
    private static final BotEventManager INSTANCE = new BotEventManager();
    
    private final List<BotSpawnHandler> spawnHandlers = new ArrayList<>();
    private final List<BotDeathHandler> deathHandlers = new ArrayList<>();
    private final List<BotAttackHandler> attackHandlers = new ArrayList<>();
    private final List<BotDamageHandler> damageHandlers = new ArrayList<>();
    private final List<BotTickHandler> tickHandlers = new ArrayList<>();
    
    private BotEventManager() {}
    
    public static BotEventManager getInstance() {
        return INSTANCE;
    }
    
    
    public void registerSpawnHandler(BotSpawnHandler handler) {
        spawnHandlers.add(handler);
    }
    
    
    public void registerDeathHandler(BotDeathHandler handler) {
        deathHandlers.add(handler);
    }
    
    
    public void registerAttackHandler(BotAttackHandler handler) {
        attackHandlers.add(handler);
    }
    
    
    public void registerDamageHandler(BotDamageHandler handler) {
        damageHandlers.add(handler);
    }
    
    
    public void registerTickHandler(BotTickHandler handler) {
        tickHandlers.add(handler);
    }
    
    
    public void fireSpawnEvent(ServerPlayerEntity bot) {
        for (BotSpawnHandler handler : spawnHandlers) {
            try {
                handler.onBotSpawn(bot);
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in spawn handler: " + e.getMessage());
            }
        }
    }
    
    
    public void fireDeathEvent(ServerPlayerEntity bot) {
        for (BotDeathHandler handler : deathHandlers) {
            try {
                handler.onBotDeath(bot);
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in death handler: " + e.getMessage());
            }
        }
    }
    
    
    public boolean fireAttackEvent(ServerPlayerEntity bot, Entity target) {
        for (BotAttackHandler handler : attackHandlers) {
            try {
                if (handler.onBotAttack(bot, target)) {
                    return true;
                }
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in attack handler: " + e.getMessage());
            }
        }
        return false;
    }
    
    
    public boolean fireDamageEvent(ServerPlayerEntity bot, Entity attacker, float damage) {
        for (BotDamageHandler handler : damageHandlers) {
            try {
                if (handler.onBotDamage(bot, attacker, damage)) {
                    return true;
                }
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in damage handler: " + e.getMessage());
            }
        }
        return false;
    }
    
    
    public void fireTickEvent(ServerPlayerEntity bot) {
        for (BotTickHandler handler : tickHandlers) {
            try {
                handler.onBotTick(bot);
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in tick handler: " + e.getMessage());
            }
        }
    }
}
