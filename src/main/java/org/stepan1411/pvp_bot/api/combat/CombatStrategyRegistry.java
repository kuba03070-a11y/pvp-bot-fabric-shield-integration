package org.stepan1411.pvp_bot.api.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Registry for custom combat strategies
 * Register your strategies here to make them available to bots
 */
public class CombatStrategyRegistry {
    
    private static final CombatStrategyRegistry INSTANCE = new CombatStrategyRegistry();
    private final List<CombatStrategy> strategies = new ArrayList<>();
    
    private CombatStrategyRegistry() {}
    
    public static CombatStrategyRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a custom combat strategy
     * @param strategy Strategy to register
     */
    public void register(CombatStrategy strategy) {
        strategies.add(strategy);
        strategies.sort(Comparator.comparingInt(CombatStrategy::getPriority).reversed());
        System.out.println("[PVP_BOT_API] Registered combat strategy: " + strategy.getName());
    }
    
    /**
     * Unregister a combat strategy
     * @param strategy Strategy to unregister
     */
    public void unregister(CombatStrategy strategy) {
        strategies.remove(strategy);
    }
    
    /**
     * Get all registered strategies
     * @return List of strategies sorted by priority
     */
    public List<CombatStrategy> getStrategies() {
        return new ArrayList<>(strategies);
    }
    
    /**
     * Clear all registered strategies
     */
    public void clear() {
        strategies.clear();
    }
}
