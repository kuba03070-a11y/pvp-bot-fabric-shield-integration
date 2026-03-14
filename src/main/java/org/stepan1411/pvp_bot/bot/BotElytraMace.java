package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.stepan1411.pvp_bot.bot.BotSettings;


public class BotElytraMace {
    
    
    private static class ElytraMaceState {
        int step = 0;
        int cooldownTicks = 0;
        int stuckCounter = 0;
        int lastStep = -1;
        

        boolean elytraEquipped = false;
        int takeoffTicks = 0;
        double startY = 0;
        int waitTicks = 0;
        int retryCount = 0;
        

        int elytraSlot = -1;
        int chestplateSlot = -1;
        int fireworkSlot = -1;
        int maceSlot = -1;
    }
    
    private static final java.util.Map<String, ElytraMaceState> states = new java.util.HashMap<>();
    
    
    private static ElytraMaceState getState(String botName) {
        return states.computeIfAbsent(botName, k -> new ElytraMaceState());
    }
    
    
    public static boolean canUseElytraMace(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        System.out.println("[ElytraMace] " + bot.getName().getString() + " checking canUseElytraMace...");
        
        if (!settings.isElytraMaceEnabled()) {
            return false;
        }
        
        double distance = bot.distanceTo(target);
        if (distance > 15.0) {
            return false;
        }
        
        PlayerInventory inventory = bot.getInventory();
        boolean hasAllItems = hasElytra(inventory) && hasMace(inventory) && hasFireworks(inventory) && hasChestplate(inventory);
        

        if (!hasAllItems) {
            int elytraSlot = findElytra(inventory);
            int chestSlot = findChestplate(inventory);
            String elytraInfo = elytraSlot == 38 ? "equipped" : (elytraSlot >= 0 ? "slot " + elytraSlot : "none");
            String chestInfo = chestSlot == 38 ? "equipped" : (chestSlot >= 0 ? "slot " + chestSlot : "none");
            System.out.println("[ElytraMace] " + bot.getName().getString() + " missing items - Elytra: " + hasElytra(inventory) + " (" + elytraInfo + ")" +
                             ", Mace: " + hasMace(inventory) + ", Fireworks: " + hasFireworks(inventory) + 
                             ", Chestplate: " + hasChestplate(inventory) + " (" + chestInfo + ")");
        } else {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " can use ElytraMace! Distance: " + String.format("%.1f", distance));
        }
        
        return hasAllItems;
    }
    
    
    public static boolean doElytraMace(ServerPlayerEntity bot, Entity target, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        ElytraMaceState state = getState(bot.getName().getString());
        World world = bot.getEntityWorld();
        double distance = bot.distanceTo(target);
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " doElytraMace called - step: " + state.step + ", distance: " + String.format("%.1f", distance));
        

        if (state.step == 0 && state.cooldownTicks == 0) {
            PlayerInventory inventory = bot.getInventory();
            int elytraSlot = findElytra(inventory);
            int chestSlot = findChestplate(inventory);
            String elytraInfo = elytraSlot == 38 ? "equipped" : (elytraSlot >= 0 ? "slot " + elytraSlot : "none");
            String chestInfo = chestSlot == 38 ? "equipped" : (chestSlot >= 0 ? "slot " + chestSlot : "none");
            System.out.println("[ElytraMace] " + bot.getName().getString() + " starting new cycle - Elytra: " + elytraInfo + ", Chestplate: " + chestInfo);
        }
        

        if (state.step == state.lastStep) {
            state.stuckCounter++;
            if (state.stuckCounter > 100) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " STUCK on step " + state.step + "! Resetting state.");
                resetState(state);
                return true;
            }
        } else {
            state.stuckCounter = 0;
            state.lastStep = state.step;
        }
        

        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
            return true;
        }
        
        if (state.waitTicks > 0) {
            state.waitTicks--;
            return true;
        }
        

        if (distance > 20.0) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " target too far, resetting");
            resetState(state);
            return true;
        }
        

        switch (state.step) {
            case 0:
                return stepPrepareElytra(bot, target, state, server, world, settings);
            case 1:
                return stepTakeoff(bot, target, state, server, world, settings);
            case 2:
                return stepWaitAltitude(bot, target, state, server, world, settings);
            case 3:
                return stepRemoveElytraAndWait(bot, target, state, server, world, settings);
            case 4:
                return stepGlideToTarget(bot, target, state, server, world, settings);
            case 5:
                return stepMaceAttack(bot, target, state, server, world, settings);
            default:
                resetState(state);
                return true;
        }
    }
    
    
    private static boolean stepPrepareElytra(ServerPlayerEntity bot, Entity target, ElytraMaceState state, 
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " step 0: preparing elytra...");
        

        state.elytraSlot = findElytra(inventory);
        state.chestplateSlot = findChestplate(inventory);
        state.fireworkSlot = findFireworks(inventory);
        state.maceSlot = findMace(inventory);
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " found items - Elytra: " + state.elytraSlot + ", Chestplate: " + state.chestplateSlot + ", Fireworks: " + state.fireworkSlot + ", Mace: " + state.maceSlot);
        
        if (state.elytraSlot < 0 || state.fireworkSlot < 0 || state.maceSlot < 0) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " missing required items!");
            return false;
        }
        

        System.out.println("[ElytraMace] " + bot.getName().getString() + " selecting elytra from slot " + state.elytraSlot);
        if (!selectItem(bot, state.elytraSlot)) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " failed to select elytra");
            return true;
        }
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " equipping elytra...");
        

        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            state.elytraEquipped = true;
            state.step = 1;
            state.cooldownTicks = 5;
            System.out.println("[ElytraMace] " + bot.getName().getString() + " elytra equipped, moving to step 1");
            
        } catch (Exception e) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " error equipping elytra: " + e.getMessage());
        }
        
        return true;
    }
    
    
    private static boolean stepTakeoff(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                     net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        

        if (state.takeoffTicks == 0) {
            state.startY = bot.getY();
            System.out.println("[ElytraMace] " + bot.getName().getString() + " starting takeoff sequence at Y=" + String.format("%.1f", state.startY) + "...");
        }
        
        state.takeoffTicks++;
        System.out.println("[ElytraMace] " + bot.getName().getString() + " takeoff tick " + state.takeoffTicks);
        

        if (state.takeoffTicks == 1) {

            bot.setPitch(0.0f);
            

            bot.jump();
            System.out.println("[ElytraMace] " + bot.getName().getString() + " jumping with horizontal look (direct call)...");
        }
        

        if (state.takeoffTicks == 5) {

            bot.jump();
            System.out.println("[ElytraMace] " + bot.getName().getString() + " activating elytra with direct jump...");
        }
        

        if (state.takeoffTicks == 8) {

            if (!selectItem(bot, state.fireworkSlot)) {
                return true;
            }
            
            System.out.println("[ElytraMace] " + bot.getName().getString() + " launching with fireworks...");
            

            int fireworkCount = settings.getElytraMaceFireworkCount();
            try {
                for (int i = 0; i < fireworkCount; i++) {
                    server.getCommandManager().getDispatcher().execute(
                        "player " + bot.getName().getString() + " use once", 
                        server.getCommandSource()
                    );
                }
                
                System.out.println("[ElytraMace] " + bot.getName().getString() + " launched " + fireworkCount + " fireworks");
                
            } catch (Exception e) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " error launching fireworks: " + e.getMessage());
            }
        }
        

        if (state.takeoffTicks == 12) {
            bot.setPitch(-90.0f);
            System.out.println("[ElytraMace] " + bot.getName().getString() + " looking up for altitude gain");
            
            state.step = 2;
            state.cooldownTicks = 3;
            System.out.println("[ElytraMace] " + bot.getName().getString() + " moving to step 2");
        }
        
        return true;
    }
    
    
    private static boolean stepWaitAltitude(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                          net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        state.takeoffTicks++;
        double currentHeight = bot.getY() - state.startY;
        int minAltitude = settings.getElytraMaceMinAltitude();
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " altitude: " + String.format("%.1f", currentHeight) + " blocks (tick " + state.takeoffTicks + ")");
        

        if (currentHeight >= minAltitude) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " reached altitude " + String.format("%.1f", currentHeight) + ", moving to step 3");
            state.step = 3;
            state.cooldownTicks = 0;
            return true;
        }
        

        if (state.takeoffTicks >= 80 || (state.takeoffTicks > 20 && currentHeight < 2.0)) {
            state.retryCount++;
            int maxRetries = settings.getElytraMaceMaxRetries();
            
            if (state.retryCount < maxRetries) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " takeoff failed (altitude: " + String.format("%.1f", currentHeight) + "), retry " + state.retryCount + "/" + maxRetries);

                state.step = 0;
                state.takeoffTicks = 0;
                state.elytraEquipped = false;
                state.cooldownTicks = 10;
            } else {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " takeoff failed after " + maxRetries + " attempts, giving up");
                resetState(state);
                return false;
            }
        }
        
        return true;
    }
    
    
    private static boolean stepRemoveElytraAndWait(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                                 net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        

        if (state.chestplateSlot >= 0) {

            if (state.chestplateSlot != 38) {
                if (!selectItem(bot, state.chestplateSlot)) {
                    return true;
                }
            }
            
            System.out.println("[ElytraMace] " + bot.getName().getString() + " equipping chestplate...");
            

            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
            } catch (Exception e) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " error equipping chestplate: " + e.getMessage());
            }
        } else {

            System.out.println("[ElytraMace] " + bot.getName().getString() + " no chestplate, removing elytra manually...");

        }
        
        state.elytraEquipped = false;
        state.waitTicks = 5;
        state.step = 4;
        System.out.println("[ElytraMace] " + bot.getName().getString() + " elytra removed, waiting 5 ticks");
        
        return true;
    }
    
    
    private static boolean stepGlideToTarget(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        double distance = bot.distanceTo(target);
        

        if (!state.elytraEquipped) {
            if (!selectItem(bot, state.elytraSlot)) {
                return true;
            }
            
            System.out.println("[ElytraMace] " + bot.getName().getString() + " re-equipping elytra for glide...");
            
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
                state.elytraEquipped = true;
            } catch (Exception e) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " error re-equipping elytra: " + e.getMessage());
            }
            return true;
        }
        

        lookAtEntity(bot, target);
        

        double deltaX = target.getX() - bot.getX();
        double deltaY = target.getY() - bot.getY();
        double deltaZ = target.getZ() - bot.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        

        float targetPitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDistance));

        targetPitch = Math.max(30.0f, Math.min(90.0f, targetPitch));
        bot.setPitch(targetPitch);
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " gliding to target, distance: " + String.format("%.1f", distance) + ", pitch: " + String.format("%.1f", targetPitch));
        

        double attackDistance = settings.getElytraMaceAttackDistance();
        if (distance <= attackDistance) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " close to target (" + String.format("%.1f", distance) + "), moving to step 5");
            state.step = 5;
            state.cooldownTicks = 0;
        }
        
        return true;
    }
    
    
    private static boolean stepMaceAttack(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                        net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        

        if (state.elytraEquipped && state.chestplateSlot >= 0) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " removing elytra, chestplate slot: " + state.chestplateSlot);
            

            if (state.chestplateSlot != 38) {
                if (!selectItem(bot, state.chestplateSlot)) {
                    return true;
                }
            }
            
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
                state.elytraEquipped = false;
                

                int elytraSlot = findElytra(inventory);
                String elytraInfo = elytraSlot == 38 ? "still equipped" : (elytraSlot >= 0 ? "moved to slot " + elytraSlot : "disappeared");
                System.out.println("[ElytraMace] " + bot.getName().getString() + " elytra removed for mace attack, elytra now: " + elytraInfo);
            } catch (Exception e) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " error removing elytra: " + e.getMessage());
            }
            return true;
        }
        

        if (!selectItem(bot, state.maceSlot)) {
            return true;
        }
        

        lookAtEntity(bot, target);
        

        System.out.println("[ElytraMace] " + bot.getName().getString() + " attacking with mace!");
        
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " attack once", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " error attacking with mace: " + e.getMessage());
            bot.swingHand(Hand.MAIN_HAND);
        }
        

        resetState(state);
        state.cooldownTicks = 20;
        
        return true;
    }
    
    
    private static void resetState(ElytraMaceState state) {
        state.step = 0;
        state.elytraEquipped = false;
        state.takeoffTicks = 0;
        state.startY = 0;
        state.waitTicks = 0;
        state.stuckCounter = 0;
        state.lastStep = -1;
        state.retryCount = 0;
        state.elytraSlot = -1;
        state.chestplateSlot = -1;
        state.fireworkSlot = -1;
        state.maceSlot = -1;
    }
    
    
    private static void lookAtEntity(ServerPlayerEntity bot, Entity target) {
        double dx = target.getX() - bot.getX();
        double dy = target.getY() - bot.getY();
        double dz = target.getZ() - bot.getZ();
        
        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (Math.atan2(dy, distance) * 180.0 / Math.PI);
        
        bot.setYaw(yaw);
        bot.setPitch(-pitch);
    }
    
    
    private static boolean selectItem(ServerPlayerEntity bot, int slot) {
        if (slot < 0 || slot >= 36) return false;
        
        PlayerInventory inventory = bot.getInventory();
        

        if (slot >= 9) {
            ItemStack item = inventory.getStack(slot);
            ItemStack current = inventory.getStack(8);
            inventory.setStack(slot, current);
            inventory.setStack(8, item);
            slot = 8;
        }
        
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, slot);
        return true;
    }
    
    
    private static int findElytra(PlayerInventory inventory) {

        ItemStack equippedChest = inventory.getStack(38);
        if (!equippedChest.isEmpty() && equippedChest.getItem() == Items.ELYTRA) {
            return 38;
        }
        

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }
    
    
    
    private static int findChestplate(PlayerInventory inventory) {

        ItemStack equippedChest = inventory.getStack(38);
        if (!equippedChest.isEmpty()) {
            String itemName = equippedChest.getItem().toString().toLowerCase();
            if (itemName.contains("chestplate")) {
                return 38;
            }
        }
        

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            String itemName = stack.getItem().toString().toLowerCase();
            if (itemName.contains("chestplate")) {
                return i;
            }
        }
        return -1;
    }
    
    
    private static int findFireworks(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }
        return -1;
    }
    
    
    private static int findMace(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.MACE) {
                return i;
            }
        }
        return -1;
    }
    
    
    private static boolean hasElytra(PlayerInventory inventory) {
        return findElytra(inventory) >= 0;
    }
    
    
    private static boolean hasMace(PlayerInventory inventory) {
        return findMace(inventory) >= 0;
    }
    
    
    private static boolean hasFireworks(PlayerInventory inventory) {
        return findFireworks(inventory) >= 0;
    }
    
    
    private static boolean hasChestplate(PlayerInventory inventory) {
        return findChestplate(inventory) >= 0;
    }
    
    
    public static void reset(String botName) {
        ElytraMaceState state = states.get(botName);
        if (state != null) {
            resetState(state);
        }
    }
}