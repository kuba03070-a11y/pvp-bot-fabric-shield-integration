package org.stepan1411.pvp_bot.bot;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;


public class HerobotMovement {
    
    private static boolean herobotAvailable = false;
    
    static {
        try {

            Class.forName("hero.bane.herobot.HeroBot");
            herobotAvailable = true;
        } catch (ClassNotFoundException e) {
            herobotAvailable = false;
        }
    }
    
    
    public static boolean isHerobotAvailable() {
        return herobotAvailable;
    }
    
    
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
            

            double dx = targetPos.x - botPos.x;
            double dz = targetPos.z - botPos.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            
            if (dist < 0.5) {

                stopMovement(bot);
                return true;
            }
            

            double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            

            executeCommand(server, String.format("player %s look %.1f 0", botName, yaw));
            

            executeCommand(server, String.format("player %s move forward", botName));
            

            if (dist > 3.0) {
                executeCommand(server, String.format("player %s sprint", botName));
            }
            

            double dy = targetPos.y - botPos.y;
            if (dy > 0.5 && bot.isOnGround()) {
                executeCommand(server, String.format("player %s jump", botName));
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    
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
            

            executeCommand(server, String.format("player %s stop", botName));
        } catch (Exception e) {

        }
    }
    
    
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

        }
    }
    
    
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

        }
    }
    
    
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
            

            double dx = targetPos.x - botPos.x;
            double dy = targetPos.y - botPos.y;
            double dz = targetPos.z - botPos.z;
            
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            double pitch = -Math.toDegrees(Math.atan2(dy, horizontalDist));
            
            executeCommand(server, String.format("player %s look %.1f %.1f", botName, yaw, pitch));
        } catch (Exception e) {

        }
    }
    
    
    public static void executeCommand(MinecraftServer server, String command) {
        try {
            server.getCommandManager().getDispatcher().execute(command, server.getCommandSource());
        } catch (Exception e) {

        }
    }
}
