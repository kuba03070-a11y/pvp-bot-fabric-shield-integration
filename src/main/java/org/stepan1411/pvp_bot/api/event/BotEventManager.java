package org.stepan1411.pvp_bot.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Event manager for bot lifecycle events
 * Register your custom handlers here
 */
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
    
    /**
     * Register a spawn event handler
     * @param handler Handler to register
     */
    public void registerSpawnHandler(BotSpawnHandler handler) {
        spawnHandlers.add(handler);
    }
    
    /**
     * Register a death event handler
     * @param handler Handler to register
     */
    public void registerDeathHandler(BotDeathHandler handler) {
        deathHandlers.add(handler);
    }
    
    /**
     * Register an attack event handler
     * @param handler Handler to register
     */
    public void registerAttackHandler(BotAttackHandler handler) {
        attackHandlers.add(handler);
    }
    
    /**
     * Register a damage event handler
     * @param handler Handler to register
     */
    public void registerDamageHandler(BotDamageHandler handler) {
        damageHandlers.add(handler);
    }
    
    /**
     * Register a tick event handler
     * @param handler Handler to register
     */
    public void registerTickHandler(BotTickHandler handler) {
        tickHandlers.add(handler);
    }
    
    /**
     * Fire bot spawn event
     * @param bot Bot that spawned
     */
    public void fireSpawnEvent(ServerPlayerEntity bot) {
        for (BotSpawnHandler handler : spawnHandlers) {
            try {
                handler.onBotSpawn(bot);
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in spawn handler: " + e.getMessage());
            }
        }
    }
    
    /**
     * Fire bot death event
     * @param bot Bot that died
     */
    public void fireDeathEvent(ServerPlayerEntity bot) {
        for (BotDeathHandler handler : deathHandlers) {
            try {
                handler.onBotDeath(bot);
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in death handler: " + e.getMessage());
            }
        }
    }
    
    /**
     * Fire bot attack event
     * @param bot Bot that attacks
     * @param target Target entity
     * @return true to cancel attack
     */
    public boolean fireAttackEvent(ServerPlayerEntity bot, Entity target) {
        for (BotAttackHandler handler : attackHandlers) {
            try {
                if (handler.onBotAttack(bot, target)) {
                    return true; // Cancel attack
                }
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in attack handler: " + e.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Fire bot damage event
     * @param bot Bot that received damage
     * @param attacker Attacker entity
     * @param damage Damage amount
     * @return true to cancel damage
     */
    public boolean fireDamageEvent(ServerPlayerEntity bot, Entity attacker, float damage) {
        for (BotDamageHandler handler : damageHandlers) {
            try {
                if (handler.onBotDamage(bot, attacker, damage)) {
                    return true; // Cancel damage
                }
            } catch (Exception e) {
                System.err.println("[PVP_BOT_API] Error in damage handler: " + e.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Fire bot tick event
     * @param bot Bot being ticked
     */
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
