package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub implementation of BotBaritone.
 * The custom A* pathfinding has been removed; all movement falls back to
 * BotNavigation's built-in direct movement which is stable and well-tested.
 */
public class BotBaritone {

    private static final Map<String, Vec3d> gotoTargets = new HashMap<>();

    public static boolean isBaritoneAvailable(ServerPlayerEntity bot) {
        return true;
    }

    public static boolean goToPosition(ServerPlayerEntity bot, Vec3d targetPos) {
        String botName = bot.getName().getString();
        gotoTargets.put(botName, targetPos);
        // Create a temporary entity-like target using direct navigation
        double dist = new Vec3d(bot.getX(), bot.getY(), bot.getZ()).distanceTo(targetPos);
        if (dist < 1.5) {
            gotoTargets.remove(botName);
            return false; // arrived
        }
        BotNavigation.moveTowardPosition(bot, targetPos, BotSettings.get().getMoveSpeed());
        return true;
    }

    public static boolean goToEntity(ServerPlayerEntity bot, Entity target, double distance) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        double dist = new Vec3d(bot.getX(), bot.getY(), bot.getZ()).distanceTo(targetPos);
        if (dist <= distance) return false;
        BotNavigation.moveToward(bot, target, BotSettings.get().getMoveSpeed());
        return true;
    }

    public static boolean moveAwayFrom(ServerPlayerEntity bot, Entity target, double minDistance) {
        double dist = new Vec3d(bot.getX(), bot.getY(), bot.getZ()).distanceTo(new Vec3d(target.getX(), target.getY(), target.getZ()));
        if (dist >= minDistance) return false;
        BotNavigation.moveAway(bot, target, BotSettings.get().getMoveSpeed());
        return true;
    }

    public static void stop(ServerPlayerEntity bot) {
        gotoTargets.remove(bot.getName().getString());
    }

    public static boolean isPathing(ServerPlayerEntity bot) {
        return gotoTargets.containsKey(bot.getName().getString());
    }

    public static double getDistanceToGoal(ServerPlayerEntity bot) {
        Vec3d goal = gotoTargets.get(bot.getName().getString());
        if (goal == null) return -1;
        return new Vec3d(bot.getX(), bot.getY(), bot.getZ()).distanceTo(goal);
    }

    public static void removeBaritone(String botName) {
        gotoTargets.remove(botName);
    }
}
