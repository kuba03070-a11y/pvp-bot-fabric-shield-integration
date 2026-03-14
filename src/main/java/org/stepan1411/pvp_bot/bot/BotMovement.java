package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;


public class BotMovement {
    

    public enum MovementType {
        NONE,
        FOLLOW,
        ESCORT,
        GOTO
    }
    

    public static class MovementState {
        public MovementType type = MovementType.NONE;
        public String targetName = null;
        public Vec3d targetPos = null;
        public boolean isEscort = false;
        public long lastUpdate = 0;
        
        public void reset() {
            type = MovementType.NONE;
            targetName = null;
            targetPos = null;
            isEscort = false;
            lastUpdate = 0;
        }
    }
    

    private static final Map<String, MovementState> botStates = new ConcurrentHashMap<>();
    

    private static final double FOLLOW_DISTANCE = 3.0;
    private static final double GOTO_THRESHOLD = 2.0;
    
    
    public static void setFollow(String botName, String targetName, boolean escort) {
        MovementState state = getOrCreateState(botName);
        state.type = escort ? MovementType.ESCORT : MovementType.FOLLOW;
        state.targetName = targetName;
        state.isEscort = escort;
        state.targetPos = null;
        state.lastUpdate = System.currentTimeMillis();
        
        System.out.println("[BotMovement] " + botName + " set to " + 
            (escort ? "escort" : "follow") + " " + targetName);
    }
    
    
    public static void setGoto(String botName, Vec3d position) {
        MovementState state = getOrCreateState(botName);
        state.type = MovementType.GOTO;
        state.targetPos = position;
        state.targetName = null;
        state.isEscort = false;
        state.lastUpdate = System.currentTimeMillis();
        
        System.out.println("[BotMovement] " + botName + " set to goto " + 
            String.format("%.1f %.1f %.1f", position.x, position.y, position.z));
    }
    
    
    public static void stop(String botName) {
        MovementState state = botStates.get(botName);
        if (state != null) {
            System.out.println("[BotMovement] " + botName + " movement stopped");
            

            try {

                for (String existingBotName : BotManager.getAllBots()) {
                    if (existingBotName.equals(botName)) {
                        ServerPlayerEntity bot = BotManager.getBot(null, botName);
                        if (bot != null) {

                            if (BotBaritone.isBaritoneAvailable(bot)) {
                                BotBaritone.stop(bot);
                            }

                            HerobotMovement.stopMovement(bot);
                        }
                        break;
                    }
                }
            } catch (Exception e) {

            }
            
            state.reset();
        }
    }
    
    
    public static void updateMovement(ServerPlayerEntity bot) {
        String botName = bot.getName().getString();
        MovementState state = botStates.get(botName);
        
        if (state == null || state.type == MovementType.NONE) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - state.lastUpdate < 50) {
            return;
        }
        state.lastUpdate = currentTime;
        
        switch (state.type) {
            case FOLLOW:
            case ESCORT:
                updateFollow(bot, state);
                break;
            case GOTO:
                updateGoto(bot, state);
                break;
        }
    }
    
    
    private static void updateFollow(ServerPlayerEntity bot, MovementState state) {
        if (state.targetName == null) {
            return;
        }
        

        Entity target = findTarget(bot, state.targetName);
        if (target == null) {
            System.out.println("[BotMovement] " + bot.getName().getString() + " lost target " + state.targetName);

            if (BotBaritone.isBaritoneAvailable(bot)) {
                BotBaritone.stop(bot);
            }
            HerobotMovement.stopMovement(bot);
            return;
        }
        
        double distance = bot.distanceTo(target);
        

        if (distance > FOLLOW_DISTANCE) {
            Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
            

            if (state.isEscort && BotSettings.get().isEscortUseBaritone()) {

                moveWithPathfindingToEntity(bot, target);
            } else if (!state.isEscort && BotSettings.get().isFollowUseBaritone()) {

                moveWithPathfindingToEntity(bot, target);
            } else {

                moveTowardsTarget(bot, targetPos);
            }
        } else {

            if (BotBaritone.isBaritoneAvailable(bot)) {
                BotBaritone.stop(bot);
            }
            HerobotMovement.stopMovement(bot);
        }
        

        if (state.isEscort) {
            checkEscortDefense(bot, target);
        }
    }
    
    
    private static void updateGoto(ServerPlayerEntity bot, MovementState state) {
        if (state.targetPos == null) {
            return;
        }
        
        double distance = new Vec3d(bot.getX(), bot.getY(), bot.getZ()).distanceTo(state.targetPos);
        

        if (distance <= GOTO_THRESHOLD) {
            System.out.println("[BotMovement] " + bot.getName().getString() + " reached destination");
            if (BotBaritone.isBaritoneAvailable(bot)) {
                BotBaritone.stop(bot);
            }
            HerobotMovement.stopMovement(bot);
            stop(bot.getName().getString());
            return;
        }
        

        if (BotSettings.get().isGotoUseBaritone()) {

            moveWithPathfinding(bot, state.targetPos);
        } else {

            moveTowardsTarget(bot, state.targetPos);
        }
    }
    
    
    private static void moveTowardsTarget(ServerPlayerEntity bot, Vec3d targetPos) {

        HerobotMovement.walkTowards(bot, targetPos);
    }
    
    
    private static void moveWithPathfinding(ServerPlayerEntity bot, Vec3d targetPos) {
        moveWithPathfindingToPosition(bot, targetPos, null);
    }
    
    
    private static void moveWithPathfindingToEntity(ServerPlayerEntity bot, Entity target) {
        moveWithPathfindingToPosition(bot, null, target);
    }
    
    
    private static void moveWithPathfindingToPosition(ServerPlayerEntity bot, Vec3d targetPos, Entity targetEntity) {

        if (!BotBaritone.isBaritoneAvailable(bot)) {

            if (targetPos != null) {
                moveTowardsTarget(bot, targetPos);
            } else if (targetEntity != null) {
                moveTowardsTarget(bot, new Vec3d(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ()));
            }
            return;
        }
        

        Vec3d actualTargetPos = targetPos;
        if (targetEntity != null) {
            actualTargetPos = new Vec3d(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
        }
        
        if (actualTargetPos == null) {
            return;
        }
        

        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        double distance = botPos.distanceTo(actualTargetPos);
        String botName = bot.getName().getString();
        

        if (distance <= 2.0) {
            BotBaritone.stop(bot);
            moveTowardsTarget(bot, actualTargetPos);
            return;
        }
        

        BotBaritone.stop(bot);
        if (targetEntity != null) {
            BotBaritone.goToEntity(bot, targetEntity, FOLLOW_DISTANCE);
        } else {
            BotBaritone.goToPosition(bot, actualTargetPos);
        }
        

        if (distance > 4.0) {
            HerobotMovement.sprint(bot, true);
        } else {
            HerobotMovement.sprint(bot, false);
        }
        

        if (BotSettings.get().isBhopEnabled() && distance > 3.0 && bot.isOnGround()) {

            long currentTime = System.currentTimeMillis();
            Long lastBhop = bhopCooldowns.get(botName);
            
            if (lastBhop == null || currentTime - lastBhop > (BotSettings.get().getBhopCooldown() * 50)) {
                HerobotMovement.jump(bot);
                bhopCooldowns.put(botName, currentTime);
            }
        }
    }
    

    private static final Map<String, Long> bhopCooldowns = new ConcurrentHashMap<>();
    
    
    private static boolean shouldJump(ServerPlayerEntity bot, Vec3d targetPos) {

        return targetPos.y > bot.getY() + 0.5;
    }
    
    
    private static Entity findTarget(ServerPlayerEntity bot, String targetName) {
        try {

            var server = bot.getCommandSource().getServer();
            if (server == null) return null;
            

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getName().getString().equals(targetName)) {
                    return player;
                }
            }
            

            for (String botName : BotManager.getAllBots()) {
                if (botName.equals(targetName)) {
                    ServerPlayerEntity targetBot = BotManager.getBot(server, botName);
                    if (targetBot != null) {
                        return targetBot;
                    }
                }
            }
        } catch (Exception e) {

        }
        
        return null;
    }
    
    
    private static void checkEscortDefense(ServerPlayerEntity bot, Entity target) {


    }
    
    
    private static MovementState getOrCreateState(String botName) {
        return botStates.computeIfAbsent(botName, k -> new MovementState());
    }
    
    
    public static MovementState getState(String botName) {
        return botStates.get(botName);
    }
    
    
    public static void clearState(String botName) {

        try {
            BotBaritone.removeBaritone(botName);
        } catch (Exception e) {

        }
        

        botStates.remove(botName);
        

        bhopCooldowns.remove(botName);
    }
}