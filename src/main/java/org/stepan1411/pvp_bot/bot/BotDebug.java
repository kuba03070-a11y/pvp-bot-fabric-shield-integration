package org.stepan1411.pvp_bot.bot;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;


public class BotDebug {
    

    private static final int COLOR_GREEN = 0x00FF00;
    private static final int COLOR_GRAY = 0x808080;
    private static final int COLOR_RED = 0xFF0000;
    private static final int COLOR_PURPLE = 0xFF00FF;
    private static final int COLOR_BLUE = 0x0080FF;
    private static final int COLOR_YELLOW = 0xFFFF00;
    

    private static final Map<String, DebugSettings> debugSettings = new HashMap<>();
    
    
    public static class DebugSettings {
        public boolean pathVisualization = false;
        public boolean targetVisualization = false;
        public boolean combatInfo = false;
        public boolean navigationInfo = false;
        

        public int pathTickCounter = 0;
        public int targetTickCounter = 0;
        public int navigationTickCounter = 0;
        
        public boolean isAnyEnabled() {
            return pathVisualization || targetVisualization || combatInfo || navigationInfo;
        }
    }
    
    public static DebugSettings getSettings(String botName) {
        return debugSettings.computeIfAbsent(botName, k -> new DebugSettings());
    }
    
    public static void setPathVisualization(String botName, boolean enabled) {
        getSettings(botName).pathVisualization = enabled;
    }
    
    public static void setTargetVisualization(String botName, boolean enabled) {
        getSettings(botName).targetVisualization = enabled;
    }
    
    public static void setCombatInfo(String botName, boolean enabled) {
        getSettings(botName).combatInfo = enabled;
    }
    
    public static void setNavigationInfo(String botName, boolean enabled) {
        getSettings(botName).navigationInfo = enabled;
    }
    
    public static void enableAll(String botName) {
        DebugSettings settings = getSettings(botName);
        settings.pathVisualization = true;
        settings.targetVisualization = true;
        settings.combatInfo = true;
        settings.navigationInfo = true;
    }
    
    public static void disableAll(String botName) {
        debugSettings.remove(botName);
    }
    
    public static boolean isEnabled(String botName) {
        DebugSettings settings = debugSettings.get(botName);
        return settings != null && settings.isAnyEnabled();
    }
    
    
    public static void showPath(ServerPlayerEntity bot, Vec3d targetPos, java.util.LinkedList<Vec3d> pathHistory) {
        showPath(bot, targetPos, pathHistory, null);
    }
    
    
    public static void showPath(ServerPlayerEntity bot, Vec3d targetPos, java.util.LinkedList<Vec3d> pathHistory, List<Vec3d> fullPath) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.pathVisualization) {
            return;
        }
        

        settings.pathTickCounter++;
        if (settings.pathTickCounter < 5) {
            return;
        }
        settings.pathTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        

        DustParticleEffect greenDust = new DustParticleEffect(COLOR_GREEN, 1.0f);
        DustParticleEffect grayDust = new DustParticleEffect(COLOR_GRAY, 0.7f);
        DustParticleEffect blueDust = new DustParticleEffect(COLOR_BLUE, 1.0f);
        

        if (fullPath != null && !fullPath.isEmpty()) {

            for (Vec3d waypoint : fullPath) {
                world.spawnParticles(blueDust, waypoint.x, waypoint.y + 0.5, waypoint.z, 3, 0.2, 0.2, 0.2, 0);
            }
            

            for (int i = 0; i < fullPath.size() - 1; i++) {
                Vec3d pos1 = fullPath.get(i);
                Vec3d pos2 = fullPath.get(i + 1);
                
                drawLine(world, blueDust, 
                    pos1.x, pos1.y + 0.3, pos1.z, 
                    pos2.x, pos2.y + 0.3, pos2.z, 
                    0.3);
            }
            
            return;
        }
        

        List<Vec3d> allPositions = new ArrayList<>(pathHistory);
        Vec3d currentPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        
        if (allPositions.isEmpty() || currentPos.distanceTo(allPositions.get(allPositions.size() - 1)) > 0.3) {
            allPositions.add(currentPos);
        }
        
        allPositions.add(targetPos);
        
        if (allPositions.isEmpty()) {
            return;
        }
        

        Set<BlockPos> uniqueBlocks = new LinkedHashSet<>();
        for (Vec3d pos : allPositions) {
            BlockPos blockPos = new BlockPos(
                (int) Math.floor(pos.x), 
                (int) Math.floor(pos.y), 
                (int) Math.floor(pos.z)
            );
            uniqueBlocks.add(blockPos);
        }
        

        for (BlockPos blockPos : uniqueBlocks) {
            int bx = blockPos.getX();
            int by = blockPos.getY();
            int bz = blockPos.getZ();
            
            world.spawnParticles(greenDust, bx, by + 1, bz, 1, 0, 0, 0, 0);
            world.spawnParticles(greenDust, bx + 1, by + 1, bz + 1, 1, 0, 0, 0, 0);
        }
        

        if (allPositions.size() > 1) {
            for (int i = 0; i < allPositions.size() - 1; i++) {
                Vec3d pos1 = allPositions.get(i);
                Vec3d pos2 = allPositions.get(i + 1);
                
                drawLine(world, grayDust, 
                    pos1.x, pos1.y + 0.1, pos1.z, 
                    pos2.x, pos2.y + 0.1, pos2.z, 
                    0.3);
            }
        }
    }
    
    /**
     * Show target block as red cube
     */
    public static void showTargetBlock(ServerPlayerEntity bot, Vec3d targetPos) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.navigationInfo) {
            return;
        }
        
        settings.navigationTickCounter++;
        if (settings.navigationTickCounter < 5) {
            return;
        }
        settings.navigationTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        int blockX = (int) Math.floor(targetPos.x);
        int blockY = (int) Math.floor(targetPos.y);
        int blockZ = (int) Math.floor(targetPos.z);
        
        DustParticleEffect redDust = new DustParticleEffect(COLOR_RED, 1.0f);
        
        double step = 0.2;
        

        drawLine(world, redDust, blockX, blockY, blockZ, blockX + 1, blockY, blockZ, step);
        drawLine(world, redDust, blockX, blockY, blockZ + 1, blockX + 1, blockY, blockZ + 1, step);
        drawLine(world, redDust, blockX, blockY, blockZ, blockX, blockY, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ, blockX + 1, blockY, blockZ + 1, step);
        

        drawLine(world, redDust, blockX, blockY + 1, blockZ, blockX + 1, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX, blockY + 1, blockZ + 1, blockX + 1, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX, blockY + 1, blockZ, blockX, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY + 1, blockZ, blockX + 1, blockY + 1, blockZ + 1, step);
        

        drawLine(world, redDust, blockX, blockY, blockZ, blockX, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ, blockX + 1, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX, blockY, blockZ + 1, blockX, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ + 1, blockX + 1, blockY + 1, blockZ + 1, step);
    }
    
    /**
     * Show target entity hitbox in purple
     */
    public static void showTargetEntity(ServerPlayerEntity bot, net.minecraft.entity.Entity target) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.targetVisualization) {
            return;
        }
        
        settings.targetTickCounter++;
        if (settings.targetTickCounter < 3) {
            return;
        }
        settings.targetTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        var box = target.getBoundingBox();
        
        DustParticleEffect purpleDust = new DustParticleEffect(COLOR_PURPLE, 1.0f);
        double step = 0.2;
        

        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, step);
        

        drawLine(world, purpleDust, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, step);
        

        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, step);
    }
    
    /**
     * Draw line with dust particles between two points
     */
    private static void drawLine(ServerWorld world, DustParticleEffect dust, double x1, double y1, double z1, double x2, double y2, double z2, double step) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = (int) Math.ceil(length / step);
        
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double x = x1 + dx * t;
            double y = y1 + dy * t;
            double z = z1 + dz * t;
            
            world.spawnParticles(dust, x, y, z, 1, 0, 0, 0, 0);
        }
    }
    
    public static void clearAll() {
        debugSettings.clear();
    }
}
