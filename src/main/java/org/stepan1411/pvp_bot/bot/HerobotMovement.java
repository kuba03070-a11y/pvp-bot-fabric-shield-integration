package org.stepan1411.pvp_bot.bot;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Uses HeroBot mod commands for bot movement
 * Provides more natural and smooth movement than manual velocity control
 */
public class HerobotMovement {
    
    private static boolean herobotAvailable = false;
    
    static {
        try {
            // Check if HeroBot mod is loaded
            Class.forName("hero.bane.herobot.HeroBot");
            herobotAvailable = true;
        } catch (ClassNotFoundException e) {
            herobotAvailable = false;
        }
    }
    
    /**
     * Check if HeroBot mod is available
     */
    public static boolean isHerobotAvailable() {
        return herobotAvailable;
    }
    
    /**
     * Make bot walk towards a position using HeroBot commands
     */
    public static boolean walkTowards(ServerPlayerEntity bot, Vec3d targetPos) {
        if (!herobotAvailable) {
            return false;
        }
        
        try {
            MinecraftServer server = bot.getCommandSource().getServer();
            if (server == null) {
                return false;
            }
            
            String botName = bot.getName().getString();
            Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
            
            // Calculate direction
            double dx = targetPos.x - botPos.x;
            double dz = targetPos.z - botPos.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            
            if (dist < 0.5) {
                // Close enough, stop
                stopMovement(bot);
                return true;
            }
            
            // Calculate yaw angle
            double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            
            // Execute HeroBot command: /player <name> look <yaw> <pitch>
            executeCommand(server, String.format("player %s look %.1f 0", botName, yaw));
            
            // Execute HeroBot command: /player <name> move forward
            executeCommand(server, String.format("player %s move forward", botName));
            
            // Sprint if far away
            if (dist > 3.0) {
                executeCommand(server, String.format("player %s sprint", botName));
            }
            
            // Jump if needed
            double dy = targetPos.y - botPos.y;
            if (dy > 0.5 && bot.isOnGround()) {
                executeCommand(server, String.format("player %s jump", botName));
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Stop bot movement
     */
    public static void stopMovement(ServerPlayerEntity bot) {
        if (!herobotAvailable) {
            return;
        }
        
        try {
            MinecraftServer server = bot.getCommandSource().getServer();
            if (server == null) {
                return;
            }
            
            String botName = bot.getName().getString();
            
            // Stop all movement
            executeCommand(server, String.format("player %s stop", botName));
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Make bot jump
     */
    public static void jump(ServerPlayerEntity bot) {
        if (!herobotAvailable) {
            return;
        }
        
        try {
            MinecraftServer server = bot.getCommandSource().getServer();
            if (server == null) {
                return;
            }
            
            String botName = bot.getName().getString();
            executeCommand(server, String.format("player %s jump", botName));
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Make bot sprint
     */
    public static void sprint(ServerPlayerEntity bot, boolean enable) {
        if (!herobotAvailable) {
            return;
        }
        
        try {
            MinecraftServer server = bot.getCommandSource().getServer();
            if (server == null) {
                return;
            }
            
            String botName = bot.getName().getString();
            if (enable) {
                executeCommand(server, String.format("player %s sprint", botName));
            } else {
                executeCommand(server, String.format("player %s unsprint", botName));
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Make bot look at position
     */
    public static void lookAt(ServerPlayerEntity bot, Vec3d targetPos) {
        if (!herobotAvailable) {
            return;
        }
        
        try {
            MinecraftServer server = bot.getCommandSource().getServer();
            if (server == null) {
                return;
            }
            
            String botName = bot.getName().getString();
            Vec3d botPos = bot.getEyePos();
            
            // Calculate angles
            double dx = targetPos.x - botPos.x;
            double dy = targetPos.y - botPos.y;
            double dz = targetPos.z - botPos.z;
            
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            double pitch = -Math.toDegrees(Math.atan2(dy, horizontalDist));
            
            executeCommand(server, String.format("player %s look %.1f %.1f", botName, yaw, pitch));
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Execute HeroBot command (public for direct use)
     */
    public static void executeCommand(MinecraftServer server, String command) {
        try {
            server.getCommandManager().getDispatcher().execute(command, server.getCommandSource());
        } catch (Exception e) {
            // Silently fail if command doesn't work
        }
    }
}
