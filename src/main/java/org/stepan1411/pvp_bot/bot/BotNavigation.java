package org.stepan1411.pvp_bot.bot;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class BotNavigation {
    
    private static final Map<String, NavigationState> navStates = new HashMap<>();
    
    public static class NavigationState {
        public int stuckTicks = 0;
        public Vec3d lastPosition = null;
        public int avoidDirection = 0;
        public int avoidTicks = 0;
        public int jumpCooldown = 0;
        

        public Vec3d spawnPosition = null;
        public Vec3d wanderTarget = null;
        public int wanderCooldown = 0;
        public int idleTicks = 0;
        

        public java.util.LinkedList<Vec3d> pathHistory = new java.util.LinkedList<>();
        public static final int MAX_PATH_HISTORY = 15;
    }
    
    public static NavigationState getState(String botName) {
        return navStates.computeIfAbsent(botName, k -> new NavigationState());
    }
    
    public static void removeState(String botName) {
        navStates.remove(botName);
    }
    
    
    public static void moveToward(ServerPlayerEntity bot, Entity target, double speed) {
        NavigationState state = getState(bot.getName().getString());
        

        if (state.jumpCooldown > 0) state.jumpCooldown--;
        if (state.avoidTicks > 0) state.avoidTicks--;
        
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        moveTowardPos(bot, targetPos, speed, state);
    }
    
    
    public static void moveAway(ServerPlayerEntity bot, Entity target, double speed) {
        NavigationState state = getState(bot.getName().getString());
        

        if (state.jumpCooldown > 0) state.jumpCooldown--;
        if (state.avoidTicks > 0) state.avoidTicks--;
        

        double dx = bot.getX() - target.getX();
        double dz = bot.getZ() - target.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0) {
            dx /= dist;
            dz /= dist;
        }
        

        Vec3d awayPos = new Vec3d(bot.getX() + dx * 10, bot.getY(), bot.getZ() + dz * 10);
        moveTowardPos(bot, awayPos, speed, state);
    }

    
    public static void moveTowardPosition(ServerPlayerEntity bot, Vec3d targetPos, double speed) {
        NavigationState state = getState(bot.getName().getString());


        if (state.jumpCooldown > 0) state.jumpCooldown--;
        if (state.avoidTicks > 0) state.avoidTicks--;

        moveTowardPos(bot, targetPos, speed, state);
    }

    
    
    private static void moveTowardPos(ServerPlayerEntity bot, Vec3d targetPos, double speed, NavigationState state) {
        var world = bot.getEntityWorld();
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        

        if (state.pathHistory.isEmpty() || botPos.distanceTo(state.pathHistory.getLast()) > 0.5) {
            state.pathHistory.add(botPos);
            if (state.pathHistory.size() > NavigationState.MAX_PATH_HISTORY) {
                state.pathHistory.removeFirst();
            }
        }

        

        BotDebug.showPath(bot, targetPos, state.pathHistory);
        BotDebug.showTargetBlock(bot, targetPos);

        

        checkIfStuck(bot, state);
        

        double dx = targetPos.x - botPos.x;
        double dz = targetPos.z - botPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        if (horizontalDist > 0.1) {
            dx /= horizontalDist;
            dz /= horizontalDist;
        }
        

        boolean inWater = bot.isTouchingWater() || bot.isSubmergedInWater();
        if (inWater) {
            double distanceToTarget = horizontalDist;
            boolean targetFar = distanceToTarget > 8.0;
            
            double waterLevel = bot.getY();
            double targetLevel = targetPos.y;
            if (targetLevel > waterLevel + 0.5) {
                bot.addVelocity(0, 0.08, 0);
            } else if (targetLevel < waterLevel - 0.5) {
                bot.addVelocity(0, -0.04, 0);
            }
            
            if (targetFar) {
                bot.setSprinting(false);
                bot.forwardSpeed = 0.8f;
                bot.sidewaysSpeed = 0;
                bot.addVelocity(dx * speed * 0.02, 0, dz * speed * 0.02);
                if (state.jumpCooldown <= 0) {
                    bot.jump();
                    state.jumpCooldown = 8;
                }
            } else {
                bot.setSprinting(false);
                bot.forwardSpeed = 0.6f;
                bot.sidewaysSpeed = 0;
                bot.addVelocity(dx * speed * 0.015, 0, dz * speed * 0.015);
                if (state.jumpCooldown <= 0) {
                    bot.jump();
                    state.jumpCooldown = 10;
                }
            }
            
            state.lastPosition = botPos;
            return;
        }
        

        if (state.avoidTicks > 0) {

            double tempDx = dx;
            if (state.avoidDirection > 0) {

                dx = -dz;
                dz = tempDx;
            } else {

                dx = dz;
                dz = -tempDx;
            }
        }
        

        BlockPos feetPos = new BlockPos(
            (int) Math.floor(botPos.x + dx * 0.5),
            (int) Math.floor(botPos.y),
            (int) Math.floor(botPos.z + dz * 0.5)
        );
        
        BlockPos headPos = feetPos.up();
        BlockPos aboveHeadPos = feetPos.up(2);
        

        boolean blockAtFeet = isBlockSolid(world, feetPos);

        boolean blockAtHead = isBlockSolid(world, headPos);

        boolean canJumpUp = blockAtFeet && !blockAtHead && !isBlockSolid(world, aboveHeadPos);

        boolean isWall = blockAtFeet && blockAtHead;
        

        boolean onLadder = isClimbable(world, bot.getBlockPos()) || isClimbable(world, bot.getBlockPos().up());
        

        BlockPos groundFront = new BlockPos(
            (int) Math.floor(botPos.x + dx * 1.2),
            (int) Math.floor(botPos.y - 1),
            (int) Math.floor(botPos.z + dz * 1.2)
        );
        boolean holeAhead = !isBlockSolid(world, groundFront) && !isBlockSolid(world, groundFront.down());
        

        BotSettings settings = BotSettings.get();
        double jumpBoost = settings.getJumpBoost();
        

        if (bot.isOnGround() && state.jumpCooldown <= 0) {
            if (canJumpUp) {

                bot.jump();
                if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);

                bot.addVelocity(dx * 0.2, 0, dz * 0.2);
                state.jumpCooldown = 8;
            } else if (onLadder) {

                bot.jump();
                if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
                state.jumpCooldown = 5;
            } else if (holeAhead) {

                bot.jump();
                if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
                bot.addVelocity(dx * 0.35, 0.05, dz * 0.35);
                state.jumpCooldown = 12;
            } else if (isWall && state.avoidTicks <= 0) {

                state.avoidDirection = (Math.random() > 0.5) ? 1 : -1;
                state.avoidTicks = 25;

                bot.jump();
                if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
                state.jumpCooldown = 10;
            }
        }
        

        if (onLadder) {
            bot.addVelocity(0, 0.12, 0);
            bot.setSprinting(false);
            bot.forwardSpeed = 1.0f;
            state.lastPosition = botPos;
            return;
        }
        


        boolean bhopEnabled = settings.isBhopEnabled();
        int bhopCooldown = settings.getBhopCooldown();
        boolean shouldBhop = bhopEnabled && speed >= 1.0 && !canJumpUp && !isWall && !holeAhead && state.jumpCooldown <= 0;
        if (shouldBhop && bot.isOnGround()) {
            bot.jump();
            if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
            state.jumpCooldown = bhopCooldown;
        }
        

        bot.setSprinting(true);
        bot.forwardSpeed = 1.0f;
        bot.sidewaysSpeed = 0;
        

        double moveForce = bot.isOnGround() ? 0.1 : 0.02;
        bot.addVelocity(dx * speed * moveForce, 0, dz * speed * moveForce);
        

        state.lastPosition = botPos;
    }

    
    
    private static void checkIfStuck(ServerPlayerEntity bot, NavigationState state) {
        Vec3d currentPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        
        if (state.lastPosition == null) {
            state.lastPosition = currentPos;
            return;
        }
        
        double moved = currentPos.distanceTo(state.lastPosition);
        
        if (moved < 0.05 && bot.isOnGround()) {
            state.stuckTicks++;
            
            if (state.stuckTicks > 10) {

                if (state.avoidTicks <= 0) {

                    state.avoidDirection = (state.avoidDirection == 0) ? 1 : -state.avoidDirection;
                    state.avoidTicks = 30;
                }
                

                if (state.jumpCooldown <= 0) {
                    bot.jump();
                    state.jumpCooldown = 10;
                }
                
                state.stuckTicks = 0;
            }
        } else {
            state.stuckTicks = 0;
        }
    }
    
    
    private static boolean isBlockSolid(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        return !state.isAir() && state.isSolidBlock(world, pos);
    }
    
    
    private static boolean isClimbable(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof LadderBlock || 
               state.getBlock() instanceof VineBlock ||
               state.isOf(Blocks.SCAFFOLDING) ||
               state.isOf(Blocks.TWISTING_VINES) ||
               state.isOf(Blocks.TWISTING_VINES_PLANT) ||
               state.isOf(Blocks.WEEPING_VINES) ||
               state.isOf(Blocks.WEEPING_VINES_PLANT);
    }
    
    
    public static void lookAt(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetPos.x - botPos.x;
        double dy = targetPos.y - botPos.y;
        double dz = targetPos.z - botPos.z;
        
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    
    public static void lookAtPosition(ServerPlayerEntity bot, Vec3d targetPos) {
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetPos.x - botPos.x;
        double dy = targetPos.y - botPos.y;
        double dz = targetPos.z - botPos.z;
        
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    
    public static void lookAway(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        

        double dx = botPos.x - targetPos.x;
        double dz = botPos.z - targetPos.z;
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        
        bot.setYaw(yaw);
        bot.setPitch(0);
        bot.setHeadYaw(yaw);
    }
    
    
    public static void idleWander(ServerPlayerEntity bot) {
        BotSettings settings = BotSettings.get();
        if (!settings.isIdleWanderEnabled()) {
            return;
        }
        
        NavigationState state = getState(bot.getName().getString());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        

        if (state.spawnPosition == null) {
            state.spawnPosition = botPos;
        }
        

        if (state.jumpCooldown > 0) state.jumpCooldown--;
        if (state.avoidTicks > 0) state.avoidTicks--;
        if (state.wanderCooldown > 0) state.wanderCooldown--;
        
        double radius = settings.getIdleWanderRadius();
        

        if (state.wanderTarget == null || state.wanderCooldown <= 0 || 
            botPos.distanceTo(state.wanderTarget) < 1.5) {

            double angle = Math.random() * Math.PI * 2;
            double dist = Math.random() * radius;
            state.wanderTarget = new Vec3d(
                state.spawnPosition.x + Math.cos(angle) * dist,
                state.spawnPosition.y,
                state.spawnPosition.z + Math.sin(angle) * dist
            );
            state.wanderCooldown = 60 + (int)(Math.random() * 100);
        }
        

        double dx = state.wanderTarget.x - botPos.x;
        double dz = state.wanderTarget.z - botPos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        
        if (dist > 0.5) {
            dx /= dist;
            dz /= dist;
            

            float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
            bot.setYaw(yaw);
            bot.setHeadYaw(yaw);
            bot.setPitch(0);
            

            bot.setSprinting(false);
            bot.forwardSpeed = 0.5f;
            bot.addVelocity(dx * 0.03, 0, dz * 0.03);
            

            var world = bot.getEntityWorld();
            BlockPos feetPos = new BlockPos(
                (int) Math.floor(botPos.x + dx * 0.5),
                (int) Math.floor(botPos.y),
                (int) Math.floor(botPos.z + dz * 0.5)
            );
            
            if (isBlockSolid(world, feetPos) && !isBlockSolid(world, feetPos.up()) && 
                bot.isOnGround() && state.jumpCooldown <= 0) {
                bot.jump();
                state.jumpCooldown = 10;
            }
        } else {

            bot.forwardSpeed = 0;
            bot.sidewaysSpeed = 0;
        }
        
        state.lastPosition = botPos;
    }
    
    
    public static void resetIdle(String botName) {
        NavigationState state = navStates.get(botName);
        if (state != null) {
            state.wanderTarget = null;
            state.wanderCooldown = 0;
            state.idleTicks = 0;
        }
    }
}
