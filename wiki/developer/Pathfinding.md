# Bot Pathfinding System

The PVP Bot mod includes an advanced pathfinding system for intelligent bot navigation.

## Overview

The pathfinding system uses A* algorithm to find optimal paths for bots to reach their targets while avoiding obstacles. This is inspired by Baritone but adapted for server-side use with `ServerPlayerEntity`.

## Why Not Use Baritone Directly?

Baritone is an excellent pathfinding mod, but it's designed for client-side use and requires `ClientPlayerEntity`. Since our bots run on the server-side using `ServerPlayerEntity`, we cannot use Baritone directly. Instead, we've implemented a custom A* pathfinding system that works on the server.

## Components

### AStarPathfinder

The core pathfinding algorithm that calculates optimal paths.

**Location:** `org.stepan1411.pvp_bot.bot.pathfinding.AStarPathfinder`

**Key Features:**
- A* algorithm for optimal pathfinding
- Obstacle avoidance
- Jump detection (can navigate over 1-block obstacles)
- Diagonal movement support
- Configurable iteration limits to prevent lag

**Usage Example:**
```java
List<Vec3d> path = AStarPathfinder.findPath(bot, targetPosition);
if (path != null) {
    // Path found, follow it
    for (Vec3d waypoint : path) {
        // Navigate to each waypoint
    }
}
```

### BotBaritone

High-level wrapper that manages pathfinding state and integrates with bot navigation.

**Location:** `org.stepan1411.pvp_bot.bot.BotBaritone`

**Key Methods:**

#### `isBaritoneAvailable(ServerPlayerEntity bot)`
Check if pathfinding is available. Always returns `true` for our A* implementation.

#### `goToPosition(ServerPlayerEntity bot, Vec3d targetPos)`
Navigate bot to a specific position using pathfinding.
- Automatically recalculates path if target moves or path becomes stale
- Returns `true` if pathfinding is active, `false` if no path found

#### `goToEntity(ServerPlayerEntity bot, Entity target, double distance)`
Navigate bot near an entity.

#### `stop(ServerPlayerEntity bot)`
Stop current pathfinding and clear path state.

#### `isPathing(ServerPlayerEntity bot)`
Check if bot is currently following a path.

#### `getDistanceToGoal(ServerPlayerEntity bot)`
Get remaining distance to pathfinding goal.

**Usage Example:**
```java
// Start pathfinding to position
if (BotBaritone.goToPosition(bot, targetPos)) {
    // Bot is pathfinding
}

// Check if still pathfinding
if (BotBaritone.isPathing(bot)) {
    double distance = BotBaritone.getDistanceToGoal(bot);
    // Do something based on distance
}

// Stop pathfinding
BotBaritone.stop(bot);
```

### BotNavigation

Low-level movement system that handles direct navigation and obstacle avoidance.

**Location:** `org.stepan1411.pvp_bot.bot.BotNavigation`

**Integration:**
When `useBaritone` setting is enabled, `BotNavigation` methods will attempt to use `BotBaritone` pathfinding first, then fall back to direct navigation if pathfinding is not available or fails.

## Settings

### useBaritone

**Type:** Boolean  
**Default:** `true`  
**Command:** `/pvpbot settings usebaritone [true/false]`

When enabled, bots will use A* pathfinding for navigation. When disabled, bots use direct navigation with obstacle avoidance.

**Recommendation:** Keep enabled for better navigation in complex terrain.

## Performance Considerations

### Iteration Limits

The pathfinder has built-in limits to prevent server lag:
- **MAX_ITERATIONS:** 5000 (maximum A* iterations per path calculation)
- **MAX_PATH_LENGTH:** 256 blocks (maximum path length)

If a path cannot be found within these limits, the pathfinder returns `null` and the bot falls back to direct navigation.

### Path Recalculation

Paths are automatically recalculated when:
- Target position moves more than 2 blocks
- Path becomes stale (5 seconds old)
- Bot reaches end of current path

### Memory Management

Path states are stored per-bot and automatically cleaned up when:
- Bot is removed (`BotBaritone.removeBaritone()`)
- Pathfinding is stopped (`BotBaritone.stop()`)

## Algorithm Details

### A* Implementation

The pathfinder uses the A* algorithm with:
- **Heuristic:** Manhattan distance
- **Cost Function:** 
  - Horizontal movement: 1.0
  - Diagonal movement: 1.414 (√2)
  - Vertical movement (jump): 2.0

### Movement Rules

- Bots can move in 8 horizontal directions (cardinal + diagonal)
- Bots can jump up 1 block
- Bots can fall any distance (no fall damage check in pathfinding)
- Bots can walk through water and lava (handled by BotNavigation)

### Walkability Check

A position is considered walkable if:
1. The block at feet level is air or liquid
2. The block at head level is air or liquid
3. There is solid ground or liquid below

## Extending the System

### Custom Pathfinding Algorithms

To implement a custom pathfinding algorithm:

1. Create a new class in `org.stepan1411.pvp_bot.bot.pathfinding`
2. Implement a static method with signature:
   ```java
   public static List<Vec3d> findPath(ServerPlayerEntity bot, Vec3d targetPos)
   ```
3. Update `BotBaritone.goToPosition()` to use your algorithm

### Custom Movement Costs

To customize movement costs (e.g., prefer certain terrain):

1. Modify `AStarPathfinder.getMoveCost()` method
2. Add terrain-specific cost multipliers
3. Consider block types (e.g., avoid soul sand, prefer paths)

Example:
```java
private static double getMoveCost(BlockPos from, BlockPos to, World world) {
    double baseCost = // ... calculate base cost
    
    // Add terrain penalties
    BlockState state = world.getBlockState(to);
    if (state.isOf(Blocks.SOUL_SAND)) {
        baseCost *= 2.0; // Avoid soul sand
    }
    
    return baseCost;
}
```

## Troubleshooting

### Bot Not Using Pathfinding

**Check:**
1. Is `useBaritone` setting enabled? (`/pvpbot settings usebaritone true`)
2. Is target position valid and reachable?
3. Check server logs for pathfinding errors

### Bot Gets Stuck

**Possible Causes:**
1. Path calculation failed - bot falls back to direct navigation
2. Terrain changed after path was calculated
3. Target is unreachable

**Solutions:**
- Pathfinding automatically recalculates every 5 seconds
- Use `/pvpbot settings usebaritone false` to disable pathfinding temporarily
- Check if target position is valid

### Performance Issues

**If pathfinding causes lag:**
1. Reduce number of active bots
2. Disable pathfinding: `/pvpbot settings usebaritone false`
3. Adjust `MAX_ITERATIONS` in `AStarPathfinder` (requires code modification)

## Future Improvements

Potential enhancements for the pathfinding system:

1. **Terrain Awareness:** Prefer certain block types (paths, grass) over others (soul sand, magma)
2. **Dynamic Obstacles:** Detect and avoid moving entities in path
3. **Path Smoothing:** Reduce waypoint count for smoother movement
4. **Jump Optimization:** Better handling of parkour-style jumps
5. **Water Navigation:** Specialized pathfinding for underwater movement
6. **Elytra Support:** Pathfinding for flying bots
7. **Multi-level Pathfinding:** Better handling of vertical structures

## API Integration

The pathfinding system is fully integrated with the PVP Bot API:

```java
// In your addon
ServerPlayerEntity bot = // ... get bot
Vec3d target = // ... get target position

// Use pathfinding
if (BotBaritone.goToPosition(bot, target)) {
    // Bot is navigating to target
    
    // Monitor progress
    double distance = BotBaritone.getDistanceToGoal(bot);
    if (distance < 5.0) {
        // Bot is close to target
        BotBaritone.stop(bot);
    }
}
```

See [API Documentation](API-Overview.md) for more details on addon development.
