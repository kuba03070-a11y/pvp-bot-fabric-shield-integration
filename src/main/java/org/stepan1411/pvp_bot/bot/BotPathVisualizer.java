package org.stepan1411.pvp_bot.bot;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BotPathVisualizer {
    
    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 10;
    private static final int COLOR_GREEN = 0x00FF00;
    
    
    public static void update(MinecraftServer server) {
        tickCounter++;
        
        if (tickCounter < UPDATE_INTERVAL) {
            return;
        }
        tickCounter = 0;
        

        ServerWorld world = server.getOverworld();
        if (world == null) {
            return;
        }
        

        for (String pathName : BotPath.getVisiblePaths()) {
            BotPath.PathData path = BotPath.getPath(pathName);
            if (path != null && !path.points.isEmpty()) {
                visualizePath(world, path);
            }
        }
    }
    
    
    private static void visualizePath(ServerWorld world, BotPath.PathData path) {
        List<Vec3d> points = path.points;
        

        for (int i = 0; i < points.size(); i++) {
            Vec3d point = points.get(i);
            

            for (int angle = 0; angle < 360; angle += 30) {
                double rad = Math.toRadians(angle);
                double offsetX = Math.cos(rad) * 0.3;
                double offsetZ = Math.sin(rad) * 0.3;
                
                world.spawnParticles(
                    ParticleTypes.WAX_ON,
                    point.x + offsetX,
                    point.y + 0.5,
                    point.z + offsetZ,
                    1,
                    0, 0, 0,
                    0
                );
            }
            

            world.spawnParticles(
                ParticleTypes.WAX_ON,
                point.x,
                point.y + 1.5,
                point.z,
                3,
                0.1, 0.1, 0.1,
                0
            );
        }
        

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d start = points.get(i);
            Vec3d end = points.get(i + 1);
            drawLine(world, start, end);
        }
        

        if (!path.loop && points.size() > 1) {
            Vec3d start = points.get(points.size() - 1);
            Vec3d end = points.get(0);
            drawLine(world, start, end);
        }
    }
    
    
    private static void drawLine(ServerWorld world, Vec3d start, Vec3d end) {
        double distance = start.distanceTo(end);
        int particleCount = (int) (distance * 2);
        

        DustParticleEffect greenDust = new DustParticleEffect(COLOR_GREEN, 1.0f);
        
        for (int i = 0; i <= particleCount; i++) {
            double t = (double) i / particleCount;
            double x = start.x + (end.x - start.x) * t;
            double y = start.y + (end.y - start.y) * t + 0.5;
            double z = start.z + (end.z - start.z) * t;
            
            world.spawnParticles(
                greenDust,
                x, y, z,
                1,
                0, 0, 0,
                0
            );
        }
    }
}
