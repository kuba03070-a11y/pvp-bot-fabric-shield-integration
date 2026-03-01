# Combat Strategies

Create custom combat strategies for bots by implementing the `CombatStrategy` interface.

## CombatStrategy Interface

```java
public interface CombatStrategy {
    String getName();
    int getPriority();
    boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings);
    boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server);
    default int getCooldown() { return 20; }
}
```

## Creating a Strategy

### Example: Potion Strategy

```java
package com.example.strategies;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.api.combat.CombatStrategy;
import org.stepan1411.pvp_bot.bot.BotSettings;

public class PotionStrategy implements CombatStrategy {
    
    @Override
    public String getName() {
        return "PotionStrategy";
    }
    
    @Override
    public int getPriority() {
        return 100; // High priority
    }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        // Check if bot has potions
        return bot.getInventory().contains(Items.SPLASH_POTION.getDefaultStack());
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
        // Apply strength effect
        bot.addStatusEffect(new StatusEffectInstance(
            StatusEffects.STRENGTH, 
            200, // 10 seconds
            1    // Level 2
        ));
        
        return true;
    }
    
    @Override
    public int getCooldown() {
        return 200; // 10 seconds
    }
}
```

### Example: Teleport Strategy

```java
public class TeleportStrategy implements CombatStrategy {
    
    @Override
    public String getName() {
        return "TeleportStrategy";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        // Use when HP is low
        return bot.getHealth() < 6.0f;
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
        // Teleport 10 blocks back
        Vec3d pos = bot.getPos();
        Vec3d lookVec = bot.getRotationVector();
        Vec3d newPos = pos.subtract(lookVec.multiply(10));
        
        bot.teleport(newPos.x, newPos.y, newPos.z);
        
        return true;
    }
    
    @Override
    public int getCooldown() {
        return 600; // 30 seconds
    }
}
```

## Registering a Strategy

Register your strategy in `onInitialize`:

```java
import org.stepan1411.pvp_bot.api.combat.CombatStrategyRegistry;

public class MyAddon implements ModInitializer {
    @Override
    public void onInitialize() {
        CombatStrategyRegistry registry = CombatStrategyRegistry.getInstance();
        
        // Register strategies
        registry.register(new PotionStrategy());
        registry.register(new TeleportStrategy());
        
        System.out.println("Combat strategies registered!");
    }
}
```

## Strategy Priorities

Strategies are executed in priority order (highest to lowest):

- **200+** - Critical strategies (escape, healing)
- **100-199** - High priority (buffs, debuffs)
- **50-99** - Medium priority (special attacks)
- **1-49** - Low priority (basic attacks)

## Built-in Strategies

The mod already includes several strategies:

- **Crystal PVP** - priority 150
- **Anchor PVP** - priority 140
- **Mace Combat** - priority 100
- **Melee Combat** - priority 10

Your strategies will be executed alongside built-in ones.

## Advanced Examples

### Environment-based Strategy

```java
public class EnvironmentStrategy implements CombatStrategy {
    
    @Override
    public String getName() {
        return "EnvironmentStrategy";
    }
    
    @Override
    public int getPriority() {
        return 80;
    }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        // Use only in Nether
        return bot.getWorld().getRegistryKey() == World.NETHER;
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
        // Special logic for Nether
        bot.addStatusEffect(new StatusEffectInstance(
            StatusEffects.FIRE_RESISTANCE, 
            6000, // 5 minutes
            0
        ));
        
        return true;
    }
}
```

### Team Strategy

```java
public class TeamStrategy implements CombatStrategy {
    
    @Override
    public String getName() {
        return "TeamStrategy";
    }
    
    @Override
    public int getPriority() {
        return 120;
    }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        // Check if allies are nearby
        List<ServerPlayerEntity> nearbyBots = bot.getWorld()
            .getPlayers()
            .stream()
            .filter(p -> PvpBotAPI.isBot(p.getName().getString()))
            .filter(p -> p.distanceTo(bot) < 10)
            .collect(Collectors.toList());
        
        return nearbyBots.size() >= 2;
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
        // Coordinated attack
        List<ServerPlayerEntity> nearbyBots = bot.getWorld()
            .getPlayers()
            .stream()
            .filter(p -> PvpBotAPI.isBot(p.getName().getString()))
            .filter(p -> p.distanceTo(bot) < 10)
            .collect(Collectors.toList());
        
        // All bots attack same target
        for (ServerPlayerEntity ally : nearbyBots) {
            ally.attack(target);
        }
        
        return true;
    }
    
    @Override
    public int getCooldown() {
        return 100; // 5 seconds
    }
}
```

## Debugging Strategies

Add logging for debugging:

```java
@Override
public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
    System.out.println("[MyStrategy] Executing for bot: " + bot.getName().getString());
    System.out.println("[MyStrategy] Target: " + target.getName().getString());
    
    // Your logic
    
    System.out.println("[MyStrategy] Execution complete");
    return true;
}
```

## Best Practices

1. **Check conditions in canUse()** - don't perform heavy operations in execute() if strategy can't be used
2. **Use appropriate priority** - critical strategies should have high priority
3. **Set reasonable cooldown** - avoid strategy spam
4. **Handle errors** - use try-catch to prevent crashes
5. **Test in different conditions** - check behavior in different biomes, dimensions, and situations
