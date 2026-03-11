package org.stepan1411.pvp_bot.bot;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class BotTicker {

    private static int tickCounter = 0;
    private static int autoSaveCounter = 0;
    private static final int AUTO_SAVE_INTERVAL = 1200;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(BotTicker::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        autoSaveCounter++;
        
        int interval = BotSettings.get().getCheckInterval();
        

        if (tickCounter % 20 == 0) {
            BotManager.cleanupDeadBots(server);

        }
        


        if (autoSaveCounter >= AUTO_SAVE_INTERVAL) {
            BotManager.updateBotData(server);
            BotManager.saveBots();
            autoSaveCounter = 0;
        }
        

        BotPathVisualizer.update(server);
        
        for (String botName : BotManager.getAllBots()) {
            ServerPlayerEntity bot = BotManager.getBot(server, botName);
            if (bot != null && bot.isAlive()) {

                try {
                    org.stepan1411.pvp_bot.api.PvpBotAPI.getEventManager().fireTickEvent(bot);
                } catch (Exception e) {
                    System.err.println("[PVP_BOT_API] Error firing tick event: " + e.getMessage());
                }

                BotUtils.update(bot, server);
                

                boolean isFollowingWithoutAttack = BotPath.isFollowing(botName) && !BotPath.shouldAttack(botName);
                if (!isFollowingWithoutAttack) {
                    BotCombat.update(bot, server);
                }
                

                if (BotPath.isFollowing(botName)) {
                    boolean shouldAttack = BotPath.shouldAttack(botName);
                    var target = BotCombat.getTarget(botName);
                    boolean hasTarget = target != null && target.isAlive();
                    

                    if (shouldAttack && hasTarget) {
                        Vec3d nextPoint = BotPath.getNextPoint(botName);
                        

                        if (!BotPath.isInCombat(botName)) {
                            BotPath.startCombat(botName, nextPoint);
                        }
                        


                        
                    } else {

                        

                        if (BotPath.isInCombat(botName)) {
                            BotPath.endCombat(botName);
                        }
                        

                        Vec3d pausedPoint = BotPath.getPausedPoint(botName);
                        if (pausedPoint != null) {
                            Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
                            double distanceToPaused = botPos.distanceTo(pausedPoint);
                            

                            if (distanceToPaused < 1.5) {
                                BotPath.clearPausedPoint(botName);
                            } else {

                                BotNavigation.lookAtPosition(bot, pausedPoint);
                                BotNavigation.moveTowardPosition(bot, pausedPoint, 1.0);
                            }
                        } else {

                            Vec3d nextPoint = BotPath.getNextPoint(botName);
                            if (nextPoint != null) {
                                Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
                                double distance = botPos.distanceTo(nextPoint);
                                

                                if (distance < 1.5) {
                                    BotPath.advanceToNextPoint(botName);
                                } else {

                                    BotNavigation.lookAtPosition(bot, nextPoint);
                                    BotNavigation.moveTowardPosition(bot, nextPoint, 1.0);
                                }
                            }
                        }
                    }
                }
                

                if (tickCounter >= interval) {
                    var utilsState = BotUtils.getState(botName);
                    if (!utilsState.isEating) {
                        BotEquipment.autoEquip(bot);
                    }
                }
            }
        }
        
        if (tickCounter >= interval) {
            tickCounter = 0;
        }
    }
}
