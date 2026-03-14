package org.stepan1411.pvp_bot.bot.pathfinding;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;


public class BaritonePathCalculator {
    
    private static boolean baritoneAvailable = false;
    
    static {
        try {

            Class.forName("baritone.api.BaritoneAPI");
            baritoneAvailable = true;
        } catch (ClassNotFoundException e) {
            baritoneAvailable = false;
        }
    }
    
    
    public static List<Vec3d> calculatePath(ServerPlayerEntity bot, Vec3d targetPos) {



        return AStarPathfinder.findPath(bot, targetPos);
    }
    
    
    public static boolean isBaritoneAvailable() {
        return baritoneAvailable;
    }
    
    
    private static List<Vec3d> tryBaritonePathfinding(ServerPlayerEntity bot, Vec3d targetPos) {





        

        return null;
    }
}
