package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.stepan1411.pvp_bot.bot.pathfinding.AStarPathfinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server-side pathfinding system for bots
 * 
 * NOTE: Original Baritone is designed for client-side use and requires ClientPlayerEntity.
 * This implementation uses A* pathfinding algorithm adapted for ServerPlayerEntity.
 */
public class BotBaritone {
    
    private static final Map<String, PathState> pathStates = new HashMap<>();
    
    // Group path cache: key = "targetEntityId_groupStartPos", value = cached path
    private static final Map<String, GroupPathCache> groupPathCache = new HashMap<>();
    
    // Failed path cache: remember when we couldn't find path to avoid recalculating
    private static final Map<String, Long> failedPathCache = new HashMap<>();
    private static final long FAILED_PATH_CACHE_TIME = 3000; // Cache failed paths for 3 seconds
    
    private static final double GROUP_RADIUS = 5.0; // Bots within 5 blocks share path
    
    private static class PathState {
        List<Vec3d> currentPath;
        int currentIndex;
        Vec3d targetPos;
        long lastPathTime;
        
        PathState(List<Vec3d> path, Vec3d target) {
            this.currentPath = path;
            this.currentIndex = 0;
            this.targetPos = target;
            this.lastPathTime = System.currentTimeMillis();
        }
    }
    
    private static class GroupPathCache {
        List<Vec3d> path;
        Vec3d groupStartPos;
        Vec3d targetPos;
        long cacheTime;
        
        GroupPathCache(List<Vec3d> path, Vec3d groupStartPos, Vec3d targetPos) {
            this.path = path;
            this.groupStartPos = groupStartPos;
            this.targetPos = targetPos;
            this.cacheTime = System.currentTimeMillis();
        }
        
        boolean isValid(Vec3d botPos, Vec3d currentTarget) {
            // Cache valid for 5 seconds
            if (System.currentTimeMillis() - cacheTime > 5000) {
                return false;
            }
            // Bot must be within group radius
            if (botPos.distanceTo(groupStartPos) > GROUP_RADIUS) {
                return false;
            }
            // Target must not have moved too far
            if (!targetPos.isInRange(currentTarget, 2.0)) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * Check if pathfinding is available
     * Always returns true as we use server-side A* pathfinding
     */
    public static boolean isBaritoneAvailable(ServerPlayerEntity bot) {
        return true;
    }
    
    /**
     * Move bot to a specific position using A* pathfinding
     */
    public static boolean goToPosition(ServerPlayerEntity bot, Vec3d targetPos) {
        String botName = bot.getName().getString();
        PathState state = pathStates.get(botName);
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        
        // Check if we need to recalculate path
        boolean needNewPath = state == null || 
                              state.currentPath == null || 
                              state.currentIndex >= state.currentPath.size() ||
                              !state.targetPos.isInRange(targetPos, 2.0) ||
                              System.currentTimeMillis() - state.lastPathTime > 5000; // Recalc every 5 sec
        
        if (needNewPath) {
            List<Vec3d> path = null;
            
            // Check if we recently failed to find path to this target
            String failKey = generateGroupKey(botPos, targetPos);
            Long lastFailTime = failedPathCache.get(failKey);
            if (lastFailTime != null && System.currentTimeMillis() - lastFailTime < FAILED_PATH_CACHE_TIME) {
                // Recently failed, use direct path without recalculating
                path = new java.util.ArrayList<>();
                path.add(targetPos);
            } else {
                // Try to use group cache first
                String groupKey = generateGroupKey(botPos, targetPos);
                GroupPathCache cachedGroup = groupPathCache.get(groupKey);
                
                if (cachedGroup != null && cachedGroup.isValid(botPos, targetPos)) {
                    // Use cached path from group
                    path = cachedGroup.path;
                    System.out.println("[BotBaritone] " + botName + " using cached group path with " + path.size() + " waypoints");
                } else {
                    // Calculate new path
                    path = AStarPathfinder.findPath(bot, targetPos);
                    
                    if (path == null || path.isEmpty()) {
                        // No path found - cache this failure and create simple direct path
                        failedPathCache.put(failKey, System.currentTimeMillis());
                        path = new java.util.ArrayList<>();
                        path.add(targetPos);
                        
                        // Only log once per failed path cache period
                        if (lastFailTime == null) {
                            System.out.println("[BotBaritone] No path found for " + botName + ", using direct path (cached for 3s)");
                        }
                    } else {
                        // Debug: log path calculation
                        System.out.println("[BotBaritone] Calculated new path for " + botName + " with " + path.size() + " waypoints");
                        
                        // Cache this path for other bots in the group
                        groupPathCache.put(groupKey, new GroupPathCache(path, botPos, targetPos));
                        
                        // Clean old cache entries (keep only last 20)
                        if (groupPathCache.size() > 20) {
                            cleanOldGroupCache();
                        }
                    }
                }
            }
            
            state = new PathState(path, targetPos);
            pathStates.put(botName, state);
        }
        
        // Follow current path
        if (state.currentIndex < state.currentPath.size()) {
            Vec3d nextPoint = state.currentPath.get(state.currentIndex);
            // botPos already declared above
            
            // Debug visualization - show full path
            if (BotDebug.isEnabled(botName)) {
                BotDebug.showPath(bot, targetPos, new java.util.LinkedList<>(), state.currentPath);
            }
            
            // Check if reached current waypoint (smaller radius for precision)
            double horizontalDist = Math.sqrt(
                Math.pow(nextPoint.x - botPos.x, 2) + 
                Math.pow(nextPoint.z - botPos.z, 2)
            );
            double verticalDist = Math.abs(nextPoint.y - botPos.y);
            
            // More precise waypoint checking
            if (horizontalDist < 0.5 && verticalDist < 1.0) {
                state.currentIndex++;
                if (state.currentIndex >= state.currentPath.size()) {
                    // Reached end of path
                    return true;
                }
                nextPoint = state.currentPath.get(state.currentIndex);
            }
            
            // Move toward next waypoint
            BotNavigation.NavigationState navState = BotNavigation.getState(bot.getName().getString());
            if (navState.jumpCooldown > 0) navState.jumpCooldown--;
            if (navState.avoidTicks > 0) navState.avoidTicks--;
            
            // Calculate direction to waypoint
            double dx = nextPoint.x - botPos.x;
            double dz = nextPoint.z - botPos.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            
            if (dist > 0.1) {
                double dy = nextPoint.y - botPos.y;
                
                // Normalize direction
                dx /= dist;
                dz /= dist;
                
                // Calculate yaw to look at waypoint
                double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
                float yawF = (float) yaw;
                
                // Smooth yaw rotation
                float currentYaw = bot.getYaw();
                float yawDiff = yawF - currentYaw;
                
                // Normalize yaw difference to -180 to 180
                while (yawDiff > 180) yawDiff -= 360;
                while (yawDiff < -180) yawDiff += 360;
                
                // Rotate smoothly (max 30 degrees per tick)
                if (Math.abs(yawDiff) > 30) {
                    yawF = currentYaw + Math.signum(yawDiff) * 30;
                }
                
                bot.setYaw(yawF);
                bot.setHeadYaw(yawF);
                
                // Use HeroBot commands for movement
                if (HerobotMovement.isHerobotAvailable()) {
                    MinecraftServer server = bot.getCommandSource().getServer();
                    
                    // Always move forward (bot is already looking at waypoint)
                    HerobotMovement.executeCommand(server, String.format("player %s move forward", botName));
                    
                    // Sprint if far
                    if (dist > 3.0) {
                        HerobotMovement.executeCommand(server, String.format("player %s sprint", botName));
                    }
                    
                    // Jump if needed (going up) or bhop for speed
                    BotSettings settings = BotSettings.get();
                    if (bot.isOnGround() && navState.jumpCooldown <= 0) {
                        if (dy > 0.5) {
                            // Need to jump up
                            HerobotMovement.executeCommand(server, String.format("player %s jump", botName));
                            navState.jumpCooldown = 10;
                        } else if (settings.isBhopEnabled() && dist > 2.0) {
                            // Bhop for speed on flat ground
                            HerobotMovement.executeCommand(server, String.format("player %s jump", botName));
                            navState.jumpCooldown = settings.getBhopCooldown();
                        }
                    }
                } else {
                    // Fallback to manual movement if HeroBot not available
                    // Set velocity instead of adding it
                    double speed = 0.1;
                    
                    // Get current velocity
                    double currentVelX = bot.getVelocity().x;
                    double currentVelZ = bot.getVelocity().z;
                    
                    // Smoothly adjust velocity
                    double targetVelX = dx * speed;
                    double targetVelZ = dz * speed;
                    
                    // Interpolate between current and target velocity
                    double newVelX = currentVelX * 0.8 + targetVelX * 0.2;
                    double newVelZ = currentVelZ * 0.8 + targetVelZ * 0.2;
                    
                    // Set velocity
                    bot.setVelocity(newVelX, bot.getVelocity().y, newVelZ);
                    
                    // Always sprint for speed
                    bot.setSprinting(true);
                    
                    // Jump if needed
                    BotSettings settings = BotSettings.get();
                    if (bot.isOnGround() && navState.jumpCooldown <= 0) {
                        if (dy > 0.5) {
                            bot.jump();
                            navState.jumpCooldown = 10;
                        } else if (settings.isBhopEnabled() && dist > 2.0) {
                            bot.jump();
                            navState.jumpCooldown = settings.getBhopCooldown();
                        }
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Move bot near an entity using A* pathfinding
     */
    public static boolean goToEntity(ServerPlayerEntity bot, Entity target, double distance) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        return goToPosition(bot, targetPos);
    }
    
    /**
     * Move bot away from an entity
     * Uses direct navigation instead of pathfinding for retreat
     */
    public static boolean moveAwayFrom(ServerPlayerEntity bot, Entity target, double minDistance) {
        // For retreat, direct navigation is faster than pathfinding
        return false; // Let BotNavigation handle it
    }
    
    /**
     * Stop current pathfinding
     */
    public static void stop(ServerPlayerEntity bot) {
        String botName = bot.getName().getString();
        pathStates.remove(botName);
    }
    
    /**
     * Check if bot is currently pathfinding
     */
    public static boolean isPathing(ServerPlayerEntity bot) {
        String botName = bot.getName().getString();
        PathState state = pathStates.get(botName);
        return state != null && state.currentPath != null && state.currentIndex < state.currentPath.size();
    }
    
    /**
     * Get distance to current goal
     */
    public static double getDistanceToGoal(ServerPlayerEntity bot) {
        String botName = bot.getName().getString();
        PathState state = pathStates.get(botName);
        if (state == null || state.targetPos == null) {
            return -1;
        }
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        return botPos.distanceTo(state.targetPos);
    }
    
    /**
     * Remove pathfinding state when bot is removed
     */
    public static void removeBaritone(String botName) {
        pathStates.remove(botName);
    }
    
    /**
     * Generate group cache key based on position and target
     * Rounds position to GROUP_RADIUS grid to group nearby bots
     */
    private static String generateGroupKey(Vec3d botPos, Vec3d targetPos) {
        // Round bot position to 5-block grid
        int gridX = (int) Math.floor(botPos.x / GROUP_RADIUS);
        int gridY = (int) Math.floor(botPos.y / GROUP_RADIUS);
        int gridZ = (int) Math.floor(botPos.z / GROUP_RADIUS);
        
        // Round target position
        int targetX = (int) Math.floor(targetPos.x);
        int targetY = (int) Math.floor(targetPos.y);
        int targetZ = (int) Math.floor(targetPos.z);
        
        return String.format("%d_%d_%d_to_%d_%d_%d", gridX, gridY, gridZ, targetX, targetY, targetZ);
    }
    
    /**
     * Clean old group cache entries
     */
    private static void cleanOldGroupCache() {
        long currentTime = System.currentTimeMillis();
        groupPathCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().cacheTime > 5000
        );
        
        // Also clean old failed path cache
        failedPathCache.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > FAILED_PATH_CACHE_TIME
        );
    }
}
