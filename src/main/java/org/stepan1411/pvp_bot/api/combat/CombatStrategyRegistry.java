package org.stepan1411.pvp_bot.api.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class CombatStrategyRegistry {
    
    private static final CombatStrategyRegistry INSTANCE = new CombatStrategyRegistry();
    private final List<CombatStrategy> strategies = new ArrayList<>();
    
    private CombatStrategyRegistry() {}
    
    public static CombatStrategyRegistry getInstance() {
        return INSTANCE;
    }
    
    
    public void register(CombatStrategy strategy) {
        strategies.add(strategy);
        strategies.sort(Comparator.comparingInt(CombatStrategy::getPriority).reversed());
        System.out.println("[PVP_BOT_API] Registered combat strategy: " + strategy.getName());
    }
    
    
    public void unregister(CombatStrategy strategy) {
        strategies.remove(strategy);
    }
    
    
    public List<CombatStrategy> getStrategies() {
        return new ArrayList<>(strategies);
    }
    
    
    public void clear() {
        strategies.clear();
    }
}
